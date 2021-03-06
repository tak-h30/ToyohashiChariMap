package com.doubutsunoyakata.toyohashicharimap.input;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.doubutsunoyakata.toyohashicharimap.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    //map
    private GoogleMap mMap;
    //gps関連
    private LocationManager locationManager;
    private Location currentLocation = null;
    //タイマー処理に関する部分
    final int INTERVAL_PERIOD = 5000;//インターバル
    Timer timer = null;

    //レイアウト
    Button button;
    boolean isRecording = false;

    //データ
    ReviewData rd;

    //現在地を取得する関数
    private Location getLastKnownLocation() {
        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);

            if (l == null) continue;
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
    //位置の記録
    private void startRecording(){
        //一定時間ごとに
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                currentLocation = getLastKnownLocation();
                if(currentLocation == null){
                    //ModeButton.setText("null");
                }
                else {
                    Log.d("Timer", currentLocation.toString());
                    LatLng p = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    rd.addLatLng(p);

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 15));

                    //mMap.addMarker(new MarkerOptions().position(p).title("Start"));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 15));
                }
            }
        }, 0, INTERVAL_PERIOD);
    }
    private void stopRecording(){
        if(timer != null) {
            Log.d("StopButton", "stop!");
            timer.cancel();
            Polyline polyline = mMap.addPolyline(rd.getPolylineOptions());

            if(currentLocation != null) {
                LatLng p = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(p).title("Stop"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 15));
            }
        }
        else Log.d("StopButton", "timer null");
    }

    //ライフサイクル
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!isRecording){
                    startRecording();
                    button.setText("StopRecording");
                    isRecording = true;
                } else {
                    stopRecording();
                    button.setText("StartRecording");
                    isRecording = false;
                }
            }
        });

        rd = new ReviewData("Dummy ID");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timer != null){
            timer.cancel();
        }
        Log.d("Timer", "onDestroy");
    }

    //マップ関連
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Location l = getLastKnownLocation();
        LatLng p;

        if(l != null){
            p = new LatLng(l.getLatitude(), l.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 15));
        }else{
            //取れなかったら豊橋駅に視点を移す
            p = new LatLng(34.7628819, 137.3819014);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 15));
        }
    }
}