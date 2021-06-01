package com.example.drive_hassle_free;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GeoQueryEventListener {

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentUser;
    private DatabaseReference myLocationRef;
    private GeoFire geoFire;
    private List<LatLng> dangerousAreahigh;
    private List<LatLng> dangerousAreamid;
    private List<LatLng> SpeedTraps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                        buildLocationRequest();
                        buildLocationCallback();
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(MapsActivity.this);
                        initArea();
                        settingGeoFire();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MapsActivity.this, "For this application to work You must Enable Permission ", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
        Toast.makeText(getApplicationContext(),"Have A Hassle Free Ride \uD83D\uDE0E",Toast.LENGTH_SHORT);


    }

    private void initArea() {
        dangerousAreahigh= new ArrayList<>();
        dangerousAreahigh.add(new LatLng(23.275531, 77.463565));
        dangerousAreahigh.add(new LatLng(23.231361, 77.432614));
        dangerousAreahigh.add(new LatLng(23.254592, 77.398197));
        dangerousAreahigh.add(new LatLng(23.273132, 77.369333));
        dangerousAreahigh.add(new LatLng(23.284008, 77.380582));
        dangerousAreamid= new ArrayList<>();
        dangerousAreamid.add(new LatLng(23.241718, 77.440130));
        dangerousAreamid.add(new LatLng(23.252423, 77.450034));
        dangerousAreamid.add(new LatLng(23.280219, 77.455593));
        dangerousAreamid.add(new LatLng(23.212554, 77.442051));
        dangerousAreamid.add(new LatLng(23.248960, 77.471833));
        dangerousAreamid.add(new LatLng(23.231831, 77.454275));
        dangerousAreamid.add(new LatLng(23.225545, 77.386249));

        SpeedTraps= new ArrayList<>();
        SpeedTraps.add(new LatLng(23.284596, 77.441575));
        SpeedTraps.add(new LatLng(23.301455, 77.366387));
        SpeedTraps.add(new LatLng(23.230759, 77.424670));
        SpeedTraps.add(new LatLng(23.258605, 77.389259));
        SpeedTraps.add(new LatLng(23.202999, 77.446295));

        FirebaseDatabase.getInstance().getReference("DangerousArea").push().setValue(dangerousAreahigh).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(),"Updated!",Toast.LENGTH_LONG);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext() ,""+e.getMessage(),Toast.LENGTH_LONG);
            }
        });
    }

    private void settingGeoFire() {
        myLocationRef = FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire = new GeoFire(myLocationRef);


    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(final LocationResult locationResult) {
                if (mMap != null)
                {
                    geoFire.setLocation("YOU", new GeoLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                            if(currentUser!= null) currentUser.remove();
                            currentUser= mMap.addMarker(new MarkerOptions().position(new LatLng(locationResult.getLastLocation().getLatitude(),locationResult.getLastLocation().getLongitude())).title("YOU"));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUser.getPosition(),12.0f));
                        }
                    });
                }

            }
        };

    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

       mMap.getUiSettings().setZoomControlsEnabled(true);
       if(fusedLocationProviderClient != null)
           if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
               if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                   return;
               }
           }
          fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.myLooper());
           for(LatLng latLng :dangerousAreahigh)
           {
               mMap.addCircle(new CircleOptions().center(latLng).radius(200).strokeColor(Color.RED).fillColor(0x7A993B3B).strokeWidth(2.0f));
               GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude),0.2f);
                       geoQuery.addGeoQueryEventListener(MapsActivity.this);



           }
        for(LatLng latLng :dangerousAreamid)
        {
            mMap.addCircle(new CircleOptions().center(latLng).radius(100).strokeColor(Color.YELLOW).fillColor(0x4DFDD835).strokeWidth(2.0f));
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude),0.2f);
            geoQuery.addGeoQueryEventListener(MapsActivity.this);


        }
        for(LatLng latLng :SpeedTraps)
        {
            mMap.addCircle(new CircleOptions().center(latLng).radius(200).strokeColor(Color.RED).fillColor(0x7A993B3B).strokeWidth(2.0f));
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude),0.2f);
            geoQuery.addGeoQueryEventListener(MapsActivity.this);


        }
    }

    @Override
    protected void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {

        sendNotification("Drive Hassle Free",String.format("%s are Entering Probable area ",key));
    }

    @Override
    public void onKeyExited(String key) {
        sendNotification("Drive Hassle Free",String.format("%s have Exited Probable area ",key));
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {

    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
     Toast.makeText(this,""+error.getMessage(),Toast.LENGTH_LONG).show();
    }
    private void sendNotification(String title,String content)
    {
        Toast.makeText(this,""+content,Toast.LENGTH_LONG);

        String NOTIFICATION_CHANNEL_ID = "edmt_multiple_location";
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
    {
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"My Notification",NotificationManager.IMPORTANCE_DEFAULT);
      notificationChannel.setDescription("Channel description");
      notificationChannel.enableLights(true);
      notificationChannel.setLightColor(Color.RED);

      notificationManager.createNotificationChannel(notificationChannel);
    }

        NotificationCompat.Builder builder= new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
    builder.setContentTitle(title).setContentText(content).setAutoCancel(false).setSmallIcon(R.mipmap.ic_launcher_round).setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
   Notification notification =builder.build();
    notificationManager.notify(new Random().nextInt(),notification);

    }
}
