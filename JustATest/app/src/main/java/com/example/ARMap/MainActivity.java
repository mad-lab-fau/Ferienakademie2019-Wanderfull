package com.example.ARMap;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.ArFragment;


import com.example.ARMap.common.helpers.SnackbarHelper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.ui.AppBarConfiguration;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

public class MainActivity extends AppCompatActivity {

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            gpsLongitude = loc.getLongitude();
            gpsLatitude = loc.getLatitude();
            gpsAltitude = loc.getAltitude();
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    public double gpsLongitude = 0;
    public double gpsLatitude = 0;
    public double gpsAltitude = 0;

    private LocationManager locationManager = null;
    private LocationListener locationListener = null;
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 0;
    private String message = "loading GPS data";
    private TextView tv = null;
    private ArFragment arFragment;
    private ImageView fitToScanView;
    private AppBarConfiguration mAppBarConfiguration;

    private GPXParser mParser = new GPXParser();
    private Gpx parsedGpx = null;
    private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();





    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Deletes shared preferences
        tv = (TextView) findViewById(R.id.DebugTest);
        tv.setText(message);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            InputStream in = getAssets().open("radlspitz.gpx");
            parsedGpx = mParser.parse(in);
        } catch (IOException | XmlPullParserException e) {
            // do something with this exception
            e.printStackTrace();
        }
        if (parsedGpx == null) {
            Log.e("GPXParse", "onCreate: No Track found");
        } else {
            Log.d("GPXParse", "onCreate: Track loaded");
        }

        // Delete as soon as possible
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).
                edit().clear().apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager
                            .GPS_PROVIDER, 5000, 10, locationListener);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    gpsLongitude = 0;
                    gpsLatitude = 0;
                    gpsAltitude = 0;
                }
                return;
            }
            default:

                // other 'case' lines to check for other
                // permissions this app might request.
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_profile) {
            Intent profileIntent = new Intent(this, DisplayProfile.class);
            startActivity(profileIntent);
            overridePendingTransition(R.anim.nav_default_pop_enter_anim, R.anim.nav_default_pop_exit_anim);
            return true;
        }
        if (id == R.id.action_friends) {
            Intent friendsIntent = new Intent(this, DisplayFriends.class);
            startActivity(friendsIntent);
            overridePendingTransition(R.anim.nav_default_pop_enter_anim, R.anim.nav_default_pop_exit_anim);

            return true;
        }
        if (id == R.id.action_savedmaps) {
            Intent sacedMapsIntent = new Intent(this, DisplaySavedTracks.class);
            startActivity(sacedMapsIntent);
            overridePendingTransition(R.anim.nav_default_pop_enter_anim, R.anim.nav_default_pop_exit_anim);
            return true;
        }
        if (id == R.id.action_settings) {
            Intent settingIntent = new Intent(this, DisplaySettings.class);
            startActivity(settingIntent);
            overridePendingTransition(R.anim.nav_default_pop_enter_anim, R.anim.nav_default_pop_exit_anim);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationListener = new MyLocationListener();
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);



        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the usre
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            locationManager.requestLocationUpdates(LocationManager
                    .GPS_PROVIDER, 5000, 10, locationListener);
        }
        if (augmentedImageMap.isEmpty()) {
            fitToScanView.setVisibility(View.VISIBLE);
        }


        /*
        * Get GPS Tracks from activity_saved_tracks
        * Here the Track ID is requested.
        * You will get the value with the key "trackID"
        * The value is the same as the track name, but that can be changed in DisplaySavedTracks.java if you want
        * The preferences are deleted onCreate()
        * !!! NOT SURE WHERE TO  PUT THIS !!!!
        * */
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String data = prefs.getString("trackID", "no id"); //no id: default value
        if(!"no id".equals(data)) {
            //popup to see something
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(true);
            builder.setTitle("Track was selected");
            builder.setMessage(data);
            builder.setPositiveButton("Confirm",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    AugmentedImageNode node;
    int i=0;
    int a =0;
    boolean friendsDrawn= false;
    boolean peaksDrawn = false;

    private void onUpdateFrame(FrameTime frameTime) {

        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame, just return.
        if (frame == null) {
            return;
        }

        message = "GPS latidude = " + gpsLatitude;
        tv.setText(message);

        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    String text = "Detected Image " + augmentedImage.getIndex();
                    SnackbarHelper.getInstance().showMessage(this, text);
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    fitToScanView.setVisibility(View.GONE);

                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        //if(mapView){

                        //} case
                        node = new AugmentedImageNode(this, "kompass_all.sfb");
                        node.setImage(augmentedImage);
                        augmentedImageMap.put(augmentedImage, node);
                        arFragment.getArSceneView().getScene().addChild(node);
                    }
                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }

            if(node != null) {
                Vector3 markerLocation = node.mapGPS(gpsLatitude, gpsLongitude, (gpsAltitude * 0.00004) -0.03);
                Log.d("mapgps", "vector: " + markerLocation.toString());
                node.markerNode.setLocalPosition(markerLocation);

                // fake friends
                Vector3 friendLocation1 = node.mapGPS(46.713344, 11.381113, (2422 * 0.00004) -0.03);
                Vector3 friendLocation2 = node.mapGPS(46.688172, 11.420636, (1570 * 0.00004) -0.03);
                Vector3 friendLocation3 = node.mapGPS(46.688700, 11.420630, (1570 * 0.00004) -0.03);
                if(node.marker.isDone()&&!friendsDrawn) {
                    setFakeFriends(friendLocation1);
                    setFakeFriends(friendLocation2);
                    setFakeFriends(friendLocation3);
                    friendsDrawn=true;
                }

                // mountain peaks
                Vector3 peakLocation1 = node.mapGPS(46.713344, 11.381113, (2422 * 0.00004) -0.03);
                Vector3 peakLocation2 = node.mapGPS(46.688172, 11.420636, (1570 * 0.00004) -0.03);
                Vector3 peakLocation3 = node.mapGPS(46.688700, 11.420630, (1570 * 0.00004) -0.03);

                /*if(node.marker.isDone() && !peaksDrawn) {

                }*/


                List<Track> tracks = parsedGpx.getTracks();
                if(i==50){
                    for (int i = 0; i < tracks.size(); i++) {
                        Track track = tracks.get(i);
                        Log.d("GPX", "track " + i + ":");
                        List<TrackSegment> segments = track.getTrackSegments();
                        for (int j = 0; j < segments.size(); j++) {
                            TrackSegment segment = segments.get(j);
                            Log.d("GPX", "  segment " + j + ":");
                            for (TrackPoint trackPoint : segment.getTrackPoints()) {
                                if(a%6==0){
                                    Log.d("GPX", "    point: lat " + trackPoint.getLatitude() + ", lon " + trackPoint.getLongitude()+"alt "+trackPoint.getElevation());
                                    Node trackNode = new Node();
                                    trackNode.setParent(node);
                                    if (node.cube.isDone()){
                                        Log.d("GPX", "onUpdateFrame: Done");
                                        trackNode.setLocalPosition(node.mapGPS(trackPoint.getLatitude(),trackPoint.getLongitude(),(trackPoint.getElevation()*0.00004)-0.025));
                                        trackNode.setLocalScale(new Vector3(0.2f,0.2f,0.2f));
                                        trackNode.setLocalRotation(new Quaternion(new Vector3(1f, 0f, 0f), 90f));
                                        trackNode.setRenderable(node.cube.getNow(null));
                                    }}
                                a++;
                            }
                        }
                    }}
                i++;
            }
        }


    }

    private void setFakeFriends(Vector3 friendLocation) {
        Node trackNode = new Node();
        trackNode.setParent(node);
        if (node.hiker.isDone()) {
            Log.d("GPX", "onUpdateFrame: Friends Done");
            trackNode.setLocalPosition(friendLocation);
            trackNode.setRenderable(node.hiker.getNow(null));
        }
    }

    private void setPeaks(Vector3 peakLocation) {
        Node trackNode = new Node();
        trackNode.setParent(node);
        if (node.cross.isDone()) {
            Log.d("GPX", "onUpdateFrame: Friends Done");
            trackNode.setLocalPosition(peakLocation);
            trackNode.setRenderable(node.cross.getNow(null));
        }
    }
}
