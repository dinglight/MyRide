package com.example.dingl.myride;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MyRide";
    private LocationManager mLocationManager = null;
    private String mProvider;

    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private TextView mDistanceTextView;
    private TextView mSpeedTextView;
    private TextView mDurationTextView;
    private TextView mAccuracyTextView;

    private Date mStartTime;
    private Date mStopTime;
    private float mDistance;
    private Location mLastLocation = null;

    LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.v(TAG, "onStatusChanged");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.v(TAG, "onProviderEnabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.v(TAG, "onProviderDisabled");
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.v(TAG, "location update");
            showLocation(location);
            mLastLocation = location;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToggleButton toggle = (ToggleButton) findViewById(R.id.togglebutton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                              @Override
                                              public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                  if (isChecked) {
                                                      startRide();
                                                  } else {
                                                      stopRide();
                                                  }
                                              }
                                          }
        );

        mLatitudeTextView = (TextView) findViewById(R.id.latitudeTextView);
        mLongitudeTextView = (TextView) findViewById(R.id.longitudeTextView);
        mDistanceTextView = (TextView) findViewById(R.id.distanceTextView);
        mSpeedTextView = (TextView) findViewById(R.id.speedTextView);
        mDurationTextView = (TextView) findViewById(R.id.durationTextView);
        mAccuracyTextView = (TextView) findViewById(R.id.accuracyTextView);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // get all aviable provider
        List<String> providerList = mLocationManager.getProviders(true);
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            mProvider = LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            mProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            Toast.makeText(this, "No location provider to use", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    private void startRide() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "No permisstion to use", Toast.LENGTH_SHORT).show();
            return;
        }
        mStartTime = new Date(System.currentTimeMillis());
        mDistance = 0;
        mLocationManager.requestLocationUpdates(mProvider, 5000, 1, mLocationListener);
    }

    private void stopRide() {
        mStopTime = new Date(System.currentTimeMillis());
        Long duration = mStopTime.getTime() - mStartTime.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        mDurationTextView.setText(dateFormat.format(duration));
        float speed = mDistance/duration;
        mSpeedTextView.setText(String.format(Locale.UK, "%f m/s", speed/1000));

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    private void showLocation(final Location location) {
        mLatitudeTextView.setText(String.format(Locale.UK, "%f", location.getLatitude()));
        mLongitudeTextView.setText(String.format(Locale.UK, "%f", location.getLongitude()));
        mSpeedTextView.setText(String.format(Locale.UK, "%f m/s", location.getSpeed()));
        mAccuracyTextView.setText(String.format(Locale.UK, "%f", location.getAccuracy()));
        if (mLastLocation != null) {
            mDistance += location.distanceTo(mLastLocation);
            mDistanceTextView.setText(String.format(Locale.UK, "%f m", mDistance));
        }
    }
}
