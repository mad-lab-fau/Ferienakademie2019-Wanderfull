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

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Texture;

import java.util.concurrent.CompletableFuture;

/**
 * Node for rendering an augmented image.l The image is framed by pacing the virtual picture frame
 * at the corners of the augmented image trackable.
 */
@SuppressWarnings({"AndroidApiChecker"})
public class AugmentedImageNode extends AnchorNode {

  private static final String TAG = "AugmentedImageNode";

  // The augmented image represented by this node.
  private AugmentedImage image;

  // Models.  We use completable futures here to simplify
  // the error handling and asynchronous loading.  The loading is started with the
  // first construction of an instance, and then used when the image is set.
  public static CompletableFuture<ModelRenderable> mapModel_satellite;
  public static CompletableFuture<ModelRenderable> mapModel_transparent;
  public static CompletableFuture<ModelRenderable> mapModel_kompass;
  public static CompletableFuture<ModelRenderable> marker;
  public static CompletableFuture<ModelRenderable> hiker;
  public static CompletableFuture<ModelRenderable> cross;
  public static CompletableFuture<ModelRenderable> binoculars;
  //private static CompletableFuture<Material> material;
  public  CompletableFuture<ModelRenderable> green_cube;
  public  CompletableFuture<ModelRenderable> red_cube;
  public  CompletableFuture<ModelRenderable> yellow_cube;
  private static CompletableFuture<Texture> texture;
  public static Node markerNode;
  public static Node mapNode1;
  public static Node mapNode2;

  public AugmentedImageNode(Context context) {
    // Upon construction, start loading the models for the corners of the frame.
    if (mapModel_satellite == null) {
      mapModel_satellite =
              ModelRenderable.builder()
                      .setSource(context, Uri.parse("kompass_all.sfb"))
                      .build();
      mapModel_transparent =
              ModelRenderable.builder()
                      .setSource(context, Uri.parse("kompass_all.sfb"))
                      .build();
      mapModel_kompass =
              ModelRenderable.builder()
                      .setSource(context, Uri.parse("kompass_all.sfb"))
                      .build();


      marker = ModelRenderable.builder().setSource(context,Uri.parse("kugel.sfb")).build();
      green_cube = ModelRenderable.builder().setSource(context,Uri.parse("cube.sfb")).build();
      hiker = ModelRenderable.builder().setSource(context,Uri.parse("hiker.sfb")).build();
      cross = ModelRenderable.builder().setSource(context,Uri.parse("Cross.sfb")).build();
      binoculars = ModelRenderable.builder().setSource(context,Uri.parse("Binoculars.sfb")).build();
      red_cube = ModelRenderable.builder().setSource(context,Uri.parse("cube_red.sfb")).build();
      yellow_cube = ModelRenderable.builder().setSource(context,Uri.parse("cube_yellow.sfb")).build();
      //MaterialFactory materialFactory= new MaterialFactory();
      //material = materialFactory.makeOpaqueWithColor(context,new Color(0,1,0,1));


    }
  }

  /**
   * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
   * created based on an Anchor created from the image. The corners are then positioned based on the
   * extents of the image. There is no need to worry about world coordinates since everything is
   * relative to the center of the image, which is the parent node of the corners.
   */
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  public void setImage(AugmentedImage image) {
    this.image = image;
    // If any of the models are not loaded, then recurse when all are loaded.
    if (!mapModel_satellite.isDone()|!marker.isDone()|!red_cube.isDone()) {
      CompletableFuture.allOf(mapModel_satellite,marker,red_cube,green_cube,yellow_cube)
              .thenAccept((Void aVoid) -> setImage(image))
              .exceptionally(
                      throwable -> {
                        Log.e(TAG, "Exception loading", throwable);
                        return null;
                      });
    }
//    ShapeFactory shapeFactory = new ShapeFactory();
//    cube = shapeFactory.makeCube(new Vector3(0.005f,0.002f,0.005f),new Vector3(0,0,0),material.getNow(null));
    // Set the anchor based on the center of the image.
    setAnchor(image.createAnchor(image.getCenterPose()));

    // Make the 3 corner nodes.
    Vector3 localPosition = new Vector3();



    localPosition = mapGPS(46.730481f,11.395109f,0f);
    mapNode1 = new Node();
    mapNode2 = new Node();

    //mapNode.setLocalScale(new Vector3(1f, 1f, 1f));
    //transform.localScale(new Vector3(image.getExtentX(), image.getExtentZ(), 1))
    mapNode1.setLocalPosition(localPosition);
    mapNode1.setLocalRotation(new Quaternion(new Vector3(0f, 1f, 0f), 180f));
    mapNode1.setRenderable(mapModel_satellite.getNow(null));

    mapNode2.setLocalPosition(localPosition);
    mapNode2.setLocalRotation(new Quaternion(new Vector3(0f, 1f, 0f), 180f));
    mapNode2.setRenderable(mapModel_kompass.getNow(null));


    markerNode = new Node();
    markerNode.setParent(this);
    //Vector3 markerLocation = new Vector3(0f,-0.01f,0f);
    //Vector3 markerLocation = mapGPS(46.745958,11.359498, (1250*0.00004)-0.03);

    //markerNode.setLocalPosition(markerLocation);
    markerNode.setRenderable(marker.getNow(null));

  }

  public Vector3 mapGPS(double lati, double longi, double zOff){
    double midN = 46.684697;
    double midE = 11.432316;
    double relLat = midN - lati;
    double relLong = midE - longi;
    double lat = (midN + lati) / 2 * 0.01745;
    double dx = (111.3 * Math.cos(lat) * (relLong));
    double dy = 111.3 * relLat;
    dx *= 0.04;
    dy *= 0.04;
    Vector3 mapped = new Vector3((float) -dx,(float) zOff,(float) dy);

    return mapped;
  }

  public AugmentedImage getImage() {
    return image;
  }
}
