package com.example.tmha.square.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tmha.square.R;
import com.example.tmha.square.fragment.DatePickerFragment;
import com.example.tmha.square.listener.ListenerDatePicker;
import com.example.tmha.square.model.Project;
import com.example.tmha.square.utils.TimeUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
/*
 * Classname: CreateProjectAcivity
 *
 * Date:06/09/2017
 *
 * Copyright
 *
 * Created by tmha on 06/09/2017
 */


public class CreateProjectAcivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener,
                   View.OnClickListener{

    private TextView mTxtProgress;
    private EditText mEdtNameProject, mEdtContent, mEdtLocation;
    private EditText mEdtStartTime, mEdtEndTime, mEdtAddress;
    private ImageView mImgProject, mImgSave, mImgCancel;
    private ImageView mImgStartTime, mImgEndTime;
    private Button mBtnSelectFile, mBtnCapture, mBtnGetLocation;
    private final int REQUEST_CODE_FOLDER = 111;
    private final int REQUEST_CODE_CAPTURE = 112;
    private final int PERMISSIONS_REQUEST_READ_EXTERNAL = 123;
    private int mId = -1;
    private int mPosition;
    private Bitmap mBitmap;
    private String mCurrentPhotoPath;
    private Project mProject;
    private String TAG = "log";
    private boolean isSaveImage = false;
    private SeekBar mSeekBarProgress;
    private int mProgress = 0;

    private LocationManager mLocationManager;
    private Location mLocation;
    private Geocoder mGeocoder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);
        addControls();
        addEvents();
    }

    /**
     *add events
     */
    private void addEvents() {
        //set data report
        setDataReport();

        mSeekBarProgress.setOnSeekBarChangeListener(this);

        mBtnSelectFile.setOnClickListener(this);
        mBtnCapture.setOnClickListener(this);

        mImgSave.setOnClickListener(this);
        mImgCancel.setOnClickListener(this);
        mImgStartTime.setOnClickListener(this);
        mImgEndTime.setOnClickListener(this);

        mEdtStartTime.setOnClickListener(this);
        mEdtEndTime.setOnClickListener(this);


        mBtnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

        mImgCancel.setOnClickListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL ){
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, REQUEST_CODE_FOLDER);
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
            return;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        Calendar calendar = Calendar.getInstance();
        String timeStamp
                = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(calendar.getTime());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


    private void insertUpdateReport(Project project) {
        if(mId != -1){
            //get result update
            boolean result
                    = MainActivity.database.updateProject(project);
            if(result) {
                Toast.makeText(CreateProjectAcivity.this,
                        "Update success",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("project", project);
                bundle.putInt("position", mPosition);
                intent.putExtra("bundle", bundle);
                setResult(RESULT_OK, intent);
                finish();
            }else {
                Toast.makeText(this,
                        "Update fail!!!",
                        Toast.LENGTH_SHORT).show();
            }

        }else {
            //get result insert
            boolean result =  MainActivity.database
                    .insertProject(project);
            if(result) {
                Toast.makeText(CreateProjectAcivity.this,
                        "Add success",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                //get new project
                ArrayList<Project> listProject
                        = MainActivity.database.getLimitproject(1, 0);
                bundle.putSerializable("project", listProject.get(0));
                intent.putExtra("bundle", bundle);
                setResult(RESULT_OK, intent);
                finish();
            }else {
                Toast.makeText(CreateProjectAcivity.this,
                        "Add fail!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setDataReport() {
        Bundle bundle = getIntent().getBundleExtra("bundle");
        if (bundle != null) {
            mProject = (Project) bundle.getSerializable("project");
            mPosition = bundle.getInt("position");
            //report = (Project) getIntent().getSerializableExtra("report");
            if (mProject != null) {
//                mReport = MainActivity.database.getProject(String.valueOf(mId));
                mId = mProject.getmID();
                mEdtNameProject.setText(mProject.getmProjectName());
                mEdtNameProject.setSelection(mProject.getmProjectName().length());
                mCurrentPhotoPath = mProject.getmProjectPhoto();
//                try {
//                    mBitmap = MediaStore.Images.Media
//                            .getBitmap(getContentResolver(),
//                                    Uri.parse(mCurrentPhotoPath));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                mImgProject.setImageBitmap(mBitmap);

                Picasso.with(this).load(mCurrentPhotoPath)
                        .error(android.R.drawable.stat_notify_error)
                        .into(mImgProject);

                mEdtStartTime.setText(mProject.getmStartTime());
                mEdtEndTime.setText(mProject.getmEndTime());
                mEdtContent.setText(mProject.getmProjectContent());
                mEdtAddress.setText(mProject.getmAddress());
                mTxtProgress.setText(mProject.getmProgess() + "%");
                mSeekBarProgress.setProgress(mProject.getmProgess());
                mEdtLocation.setText(mProject.getmLocation());
            }

        }
    }


    private void addControls() {
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mGeocoder = new Geocoder(this, Locale.getDefault());

        mImgStartTime   = (ImageView) findViewById(R.id.img_start_time);
        mImgEndTime     = (ImageView) findViewById(R.id.img_end_time);

        mEdtNameProject = (EditText) findViewById(R.id.edtNameProject);
        mEdtContent     = (EditText) findViewById(R.id.edtContentProject);
        mEdtStartTime   = (EditText) findViewById(R.id.edtStartTime);
        mEdtEndTime     = (EditText) findViewById(R.id.edtEndTime);
        mImgProject     = (ImageView) findViewById(R.id.imgProject);
        mBtnSelectFile  = (Button) findViewById(R.id.btnSelectPhoto);
        mBtnCapture     = (Button) findViewById(R.id.btnCapture);
        mImgSave        = (ImageView) findViewById(R.id.imgSave);
        mImgCancel      = (ImageView) findViewById(R.id.imgCancel);
        mEdtAddress     = (EditText) findViewById(R.id.edtAddress);
        mSeekBarProgress = (SeekBar) findViewById(R.id.seekBarProgress);
        mTxtProgress    = (TextView) findViewById(R.id.txtProgress);
        mEdtLocation    = (EditText) findViewById(R.id.edtLocation);
        mBtnGetLocation = (Button) findViewById(R.id.btnGetLocation);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create project");
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //result folder image
        if(requestCode == REQUEST_CODE_FOLDER
                && resultCode == RESULT_OK
                && data != null){
            Uri uri = data.getData();
            //get real path from uri
            mCurrentPhotoPath = "file:" + getRealPathFromURI(uri);
            try {
//                InputStream inputStream = getContentResolver()
//                        .openInputStream(uri);
//                mBitmap  = BitmapFactory.decodeStream(inputStream);
//                mImgProject.setImageBitmap(mBitmap);
                mBitmap = MediaStore.Images.Media
                        .getBitmap(getContentResolver(),
                                Uri.parse(mCurrentPhotoPath));
                mImgProject.setImageBitmap(mBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //result capture image
        if(requestCode == REQUEST_CODE_CAPTURE
                && resultCode == RESULT_OK){
            try {
                mBitmap = MediaStore.Images.Media
                        .getBitmap(getContentResolver(),
                                Uri.parse(mCurrentPhotoPath));
                mImgProject.setImageBitmap(mBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            overridePendingTransition(R.anim.right_in, R.anim.left_out);

        }
        return super.onOptionsItemSelected(item);
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver()
                .query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor
                    .getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Override
    protected void onStop() {
        String path = mCurrentPhotoPath;
        super.onStop();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mTxtProgress.setText(progress + "%");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mProgress = seekBar.getProgress();
    }


    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    public void getLocation() {
        if (!checkLocation()){
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Toast.makeText(this, "Locating, please wait...", Toast.LENGTH_SHORT).show();
        mLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 60 * 1000, 10, locationListener);
    }



    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            mLocation = location;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    double latitude = mLocation.getLatitude();
                    double longitude = mLocation.getLongitude();
                        mEdtLocation.setText(latitude + ", "
                                + longitude );
                    List<Address> listAddress = null;
                    String mAddress = "";
                    try {
                        listAddress = mGeocoder.getFromLocation(latitude,
                                                longitude, 1);
                        for (int i=0; i<5; i++){
                            String address = listAddress.get(0)
                                                 .getAddressLine(i);
                            if (address != null){
                                if (listAddress != null){
                                    mAddress +=  address + " ";
                                }else {
                                    mAddress =  address + " ";
                                }
                            }
                        }
                        mEdtAddress.setText(mAddress);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(CreateProjectAcivity.this,
                            "Updating location...",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private boolean isLocationEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Open location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSelectPhoto:
                    selectFile();
                break;
            case R.id.btnCapture:
                    captureImage();
                break;
            case R.id.imgCancel:
                    onBackPressed();
                break;
            case R.id.imgSave:
                    saveProject();
                break;
            case R.id.img_start_time:
                selectStartTime();
                break;
            case R.id.img_end_time:
                selectEndTime();
                break;
            case R.id.edtStartTime:
                selectStartTime();
                break;
            case R.id.edtEndTime:
                selectEndTime();
                break;
        }
    }

    private void selectFile(){
        if (Build.VERSION.SDK_INT >= 23) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(CreateProjectAcivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(CreateProjectAcivity.this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(CreateProjectAcivity.this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_READ_EXTERNAL);

                    // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                ActivityCompat.requestPermissions(CreateProjectAcivity.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_READ_EXTERNAL);
            }
        } else {

            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, REQUEST_CODE_FOLDER);
        }
    }

    private void captureImage(){
        Intent cameraIntent
                = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i(TAG, "IOException");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(cameraIntent,
                        REQUEST_CODE_CAPTURE);
            }
        }
    }

    private void saveProject(){

        //get name project
        String name = mEdtNameProject.getText().toString().trim();
        //check name
        if (TextUtils.isEmpty(name)){
            Toast.makeText(CreateProjectAcivity.this,
                    " Tên dự án không được để trống!!!"
                    , Toast.LENGTH_SHORT).show();
            return;
        }
        //get content project
        String content = mEdtContent.getText().toString().trim();
        //check content
        if (TextUtils.isEmpty(name)){
            Toast.makeText(CreateProjectAcivity.this,
                    " Nội dung dự án không được để trống!!!"
                    , Toast.LENGTH_SHORT).show();
            return;
        }

        String address = mEdtAddress.getText().toString().trim();
        if(TextUtils.isEmpty(address)){
            Toast.makeText(CreateProjectAcivity.this,
                    "Xin vui long nhap dia chi",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //check image
        if(mCurrentPhotoPath == null){
            Toast.makeText(CreateProjectAcivity.this,
                    "Please, select picture",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String createBy = "User";
        String timeCreate = TimeUtils.getCurrentTime();
        String startTime = mEdtStartTime.getText().toString().trim();
        String endTime   = mEdtEndTime.getText().toString().trim();
        String location  = mEdtLocation.getText().toString().trim();
        int progess = mProgress;
        Project project = new Project(mId, name, mCurrentPhotoPath,
                progess, startTime, endTime, content,
                address, location, createBy, timeCreate );
        //insert or update project
        insertUpdateReport(project);

    }

    private void selectStartTime(){
        final DatePickerFragment pickerFragment
                = new DatePickerFragment();
        DatePickerFragment fragment
                = (DatePickerFragment) getFragmentManager()
                .findFragmentByTag("datePicker");
        if(fragment != null) {
            return;
        }else {
            pickerFragment.setListenerDatePicker(new ListenerDatePicker() {
                @Override
                public void onChangeTime(String date) {
                    mEdtStartTime.setText(date);
                }
            });
            pickerFragment.show(getFragmentManager(), "datePicker");
        }
    }

    private void selectEndTime(){
        final DatePickerFragment pickerFragment = new DatePickerFragment();
        DatePickerFragment fragment
                = (DatePickerFragment) getFragmentManager()
                .findFragmentByTag("datePicker");
        if(fragment != null) {
            return;
        }else {
            pickerFragment.setListenerDatePicker(new ListenerDatePicker() {
                @Override
                public void onChangeTime(String date) {
                    mEdtEndTime.setText(date);
                }
            });
            pickerFragment.show(getFragmentManager(), "datePicker");
        }
    }
}
