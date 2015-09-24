package com.stoko.mappenger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MappengerService extends Service {

    private Timer _timer = null;
    private static Double _lat = null;
    private static Double _long = null;
    private static MappengerService _sInstance = null;
    private List<ServiceListener> _listeners = new ArrayList<ServiceListener>();

    public MappengerService() {
    }

    public static Double GetLat() {
        return _lat;
    }

    public static Double GetLong() {
        return _long;
    }

    public static MappengerService GetInstance() {
        return _sInstance;
    }

    public static boolean LocationReady() {
        return _lat != null && _long != null;
    }

    public void addListener(ServiceListener listener) {
        _listeners.add(listener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _sInstance = this;

        _timer = new Timer(true);
        _timer.scheduleAtFixedRate(new MessagesTask(), 0, 5000);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                _lat = location.getLatitude();
                _long = location.getLongitude();

                for(ServiceListener listener : _listeners) {
                    listener.locationUpdated(_lat, _long);
                }

                new AsyncMessageAtPoint().execute(getString(R.string.mappenger_ws_address), new String().valueOf(_lat), new String().valueOf(_long));
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        try{
            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            //_lat = 42.5915004;
            //_long = 22.983065;
        } catch (Exception ex) {

        }

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void NotifyUser() {
        Intent intent = new Intent(this, MessageDetailsActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, new Notification.Builder(this)
                .setContentTitle(getString(R.string.service_notification_title))
                .setContentText(getString(R.string.service_notification_text))
                .setSmallIcon(R.drawable.email)
                .setContentIntent(pIntent)
                .setAutoCancel(true).build());
    }

    class MessagesTask extends TimerTask {

        @Override
        public void run() {
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MappengerData", 0);
            if(_lat != null && _long != null) {
                new AsyncMessageAtPoint().execute(getString(R.string.mappenger_ws_address), new String().valueOf(_lat), new String().valueOf(_long));
            }
        }
    }

    class AsyncMessageAtPoint extends AsyncTask<String, Void, UserMessage> {
        @Override
        protected UserMessage doInBackground(String... params) {
            try {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MappengerLogin", 0); // 0 - for private mode

                HttpClient client = new HttpClient(params[0], pref.getString("access_token", null));

                return client.sendGetMessageAtPoint(getString(R.string.ws_get_message_at_point_endpoint), params[1], params[2]);
            } catch (Exception ex) {
                return null;
            }
        }

        protected void onPostExecute(UserMessage msg) {
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MappengerData", 0);
            if(msg != null) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("MessageFoundPK", msg.PartitionKey);
                editor.putString("MessageFoundRK", msg.RowKey);
                editor.commit();

                NotifyUser();
            }
        }
    }
}
