package com.example.tmha.square.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tmha.square.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

/**
 * Created by tmha on 6/8/2017.
 */

public class SettingFragment extends Fragment implements OnMapReadyCallback{
    MapFragment mapFragment;
    GoogleMap mMap;
    private View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_setting, container, false);
        addControls();
        return mView;
    }

    private void addControls() {
//        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragMap);
//        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        // Add a marker in Sydney and move the camera
//        LatLng toi = new LatLng(10.862357, 106.619517);
//        mMap.addMarker(new MarkerOptions().position(toi)
//                .title("Cho tao dang o ne may")
//                .snippet("To hop anh hung vo lam"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toi, 16));
    }
}
