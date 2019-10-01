/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ARMap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.ARMap.common.helpers.SnackbarHelper;
import com.google.android.material.navigation.NavigationView;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.ArFragment;

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

import org.joda.time.format.ISODateTimeFormat;


/**
 * This application demonstrates using augmented images to place anchor nodes. app to include image
 * tracking functionality.
 *
 * <p>In this example, we assume all images are static or moving slowly with a large occupation of
 * the screen. If the target is actively moving, we recommend to check
 * ArAugmentedImage_getTrackingMethod() and render only when the tracking method equals to
 * AR_AUGMENTED_IMAGE_TRACKING_METHOD_FULL_TRACKING. See details in <a
 * href="https://developers.google.com/ar/develop/c/augmented-images/">Recognize and Augment
 * Images</a>.
 */
public class AugmentedImageActivity extends AppCompatActivity {

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

  // Augmented image and its associated center pose anchor, keyed by the augmented image in
  // the database.
  private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    tv = (TextView) findViewById(R.id.DebugTest);
    tv.setText(message);
    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
    fitToScanView = findViewById(R.id.image_view_fit_to_scan);
    arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    NavigationView navigationView = findViewById(R.id.nav_view);
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    mAppBarConfiguration = new AppBarConfiguration.Builder(
            R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
            //        R.id.nav_tools, R.id.nav_share, R.id.nav_send)
            .setDrawerLayout(drawer)
            .build();
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
    NavigationUI.setupWithNavController(navigationView, navController);


    try {
      InputStream in = getAssets().open("radlspitz.gpx");
      parsedGpx = mParser.parse(in);
    } catch (IOException | XmlPullParserException e) {
      // do something with this exception
      e.printStackTrace();
    }
    if (parsedGpx == null) {
      Log.e("GPXParse", "onCreate: No Track found" );
    } else {
      Log.d("GPXParse", "onCreate: Track loaded");
    }
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
  }

  /**
   * Registered with the Sceneform Scene object, this method is called at the start of each frame.
   *
   * @param frameTime - time since last frame.
   */
  AugmentedImageNode node;
  int i=0;
  int a =0;

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

  @Override
  public boolean onSupportNavigateUp() {
    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    return NavigationUI.navigateUp(navController, mAppBarConfiguration)
            || super.onSupportNavigateUp();
  }
}
