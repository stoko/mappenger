package com.stoko.mappenger;
//hello Dani
import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends AppCompatActivity implements ServiceListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
    private String provider;

    private ProgressDialog _gpsProgress;
    private List<UserMessage> _messageMarkers = new ArrayList<UserMessage>();
    private boolean _firstRun = true;

    private Double _latitude = null;
    private Double _longitude = null;
    private Float _bearing = null;

    private Timer _timer = new Timer(true);
    private SensorManager _sManager = null;

    private Circle _msgSearchRadius = null;

    private boolean _manualCameraMode = false;
    private Double _lastLat = null;
    private Double _lastLon = null;

    private MappengerService _mpService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setUpMapIfNeeded();
        // GIT VERSION CONTROL TEST
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        _mpService = MappengerService.GetInstance();
        _mpService.addListener(this);

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        try {
            Criteria criteria = new Criteria();
            List<String> providers = locationManager.getProviders(true);
            if(!locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.enable_gps_alert_title)
                        .setMessage(R.string.enable_gps_alert_message)
                        .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        } catch (Exception ex) {
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.log_out) {
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MappengerLogin", 0); // 0 - for private mode
            SharedPreferences.Editor editor = pref.edit();
            editor.remove("access_token");
            editor.remove("userName");

            editor.commit();

            // redirect to log in activity
            Intent myIntent = new Intent(this, MainActivity.class);
            startActivity(myIntent);

            return true;
        }

        if(id == R.id.quit) {
            this.finishAffinity();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void locationUpdated(Double lat, Double lon) {
        _latitude = lat;
        _longitude = lon;

        if(!_manualCameraMode) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(_latitude, _longitude))
                    .zoom(15)              // Sets the tilt of the camera to 30 degrees
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        _lastLat = _latitude;
        _lastLon = _longitude;

        new AsyncMessages().execute(getString(R.string.mappenger_ws_address), new String().valueOf(_latitude), new String().valueOf(_longitude));

        if(_firstRun && mMap != null) {
            _timer.scheduleAtFixedRate(new MessagesTask(), 0, 5000);

            _firstRun = false;
        }
    }

    class MessagesTask extends TimerTask {

        @Override
        public void run() {
            if(_latitude != null && _longitude != null) {
                new AsyncMessages().execute(getString(R.string.mappenger_ws_address), new String().valueOf(_latitude), new String().valueOf(_longitude));
            }
        }
    }

    class AsyncMessages extends AsyncTask<String, Void, List<UserMessage>> {
        @Override
        protected List<UserMessage> doInBackground(String... params) {
            try {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MappengerLogin", 0); // 0 - for private mode

                HttpClient client = new HttpClient(params[0], pref.getString("access_token", null));

                return client.sendGetMessages(getString(R.string.ws_get_messages_endpoint), params[1], params[2]);
            } catch (Exception ex) {
                return new ArrayList();
            }
        }

        protected void onPostExecute(List<UserMessage> result) {
            List<UserMessage> toRemove = new ArrayList<UserMessage>();

            SharedPreferences pref = getApplicationContext().getSharedPreferences("MappengerLogin", 0); // 0 - for private mode
            String userName = pref.getString("userName", null);

            // get markers to remove
            for (UserMessage item : _messageMarkers) {
                boolean foundMatch = false;

                for(UserMessage resultItem : result) {
                    if(item.PartitionKey.equals(resultItem.PartitionKey) && item.RowKey.equals(resultItem.RowKey)) {
                        foundMatch = true;
                    }
                }

                if(!foundMatch) {
                    toRemove.add(item);
                }
            }

            // add and update new markers
            for(UserMessage resultItem : result) {
                boolean foundMatch = false;
                for(UserMessage item : _messageMarkers) {
                    if(item.PartitionKey.equals(resultItem.PartitionKey) && item.RowKey.equals(resultItem.RowKey)) {
                        foundMatch = true;

                        item.Latitude = resultItem.Latitude;
                        item.Longitude = resultItem.Longitude;
                        item.messageMarker.setPosition(new LatLng(resultItem.Latitude, resultItem.Longitude));
                    }
                }

                if(!foundMatch) {
                    if(resultItem.PartitionKey.equals(userName)) {
                        resultItem.messageMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(resultItem.Latitude, resultItem.Longitude)).icon(BitmapDescriptorFactory.fromResource(GetResourceIcon(resultItem.IconType, true))).title(resultItem.Message));
                    } else {
                        resultItem.messageMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(resultItem.Latitude, resultItem.Longitude)).icon(BitmapDescriptorFactory.fromResource(GetResourceIcon(resultItem.IconType, false))));
                    }
                    _messageMarkers.add(resultItem);
                }
            }

            // remove not needed markers
            for(UserMessage item : toRemove) {
                _messageMarkers.remove(toRemove);
            }
        }
    }

    public int GetResourceIcon(String iconType, boolean mine) {
        if(iconType == null)
            iconType = "default";
        switch (iconType) {
            case "snail":
                if(mine)
                    return R.drawable.snail_g;
                else
                    return R.drawable.snail_p;
            case "hare":
                if(mine)
                    return R.drawable.hare_g;
                else
                    return R.drawable.hare_p;
            case "dragon":
                if(mine)
                    return R.drawable.dragon_g;
                else
                    return R.drawable.dragon_p;
            case "static":
                if(mine)
                    return R.drawable.mushroom_g;
                else
                    return R.drawable.mushroom_p;
            default:
                if(mine)
                    return R.drawable.star_3;
                else
                    return R.drawable.comment_map_icon;
        }
    }
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        /*GoogleMap.OnMyLocationChangeListener listener = new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if(_msgSearchRadius == null) {
                    _msgSearchRadius = mMap.addCircle(new CircleOptions()
                                .center(new LatLng(_latitude, _longitude))
                                .radius(20)
                                .strokeColor(Color.RED).strokeWidth(1.5f));
                } else {
                    _msgSearchRadius.setCenter(new LatLng(_latitude, _longitude));
                }
            }
        };*/
        mMap.setMyLocationEnabled(true);

        GoogleMap.OnMapClickListener listener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                _manualCameraMode = true;
            }
        };

        GoogleMap.OnMapLongClickListener longListener = new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                _manualCameraMode = true;
            }
        };

        GoogleMap.OnMyLocationButtonClickListener btnListener = new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                _manualCameraMode = false;
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(_latitude, _longitude))             // Sets the tilt of the camera to 30 degrees
                        .zoom(15)
                        .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                return true;
            }
        };

        GoogleMap.OnCameraChangeListener camListener = new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if(_firstRun == false)
                    if(cameraPosition.zoom != 15 || Math.abs(cameraPosition.target.latitude - _latitude) > 0.0005 || Math.abs(cameraPosition.target.longitude - _longitude) > 0.0005) {
                        _manualCameraMode = true;
                    }
            }
        };

        mMap.setOnCameraChangeListener(camListener);
        mMap.setOnMapLongClickListener(longListener);
        mMap.setOnMapClickListener(listener);
        mMap.setOnMyLocationButtonClickListener(btnListener);
        //mMap.setOnMyLocationChangeListener(listener);
    }

    public void BackToMessages(View v) {
        Intent intent = new Intent(this, MessagesActivity.class);
        startActivity(intent);
    }

    public void StartThrowMessage(View v) {
        Intent intent = new Intent(this, ThrowMessage.class);
        startActivity(intent);
    }

}
