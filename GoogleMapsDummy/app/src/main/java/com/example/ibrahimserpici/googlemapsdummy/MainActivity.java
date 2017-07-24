package com.example.ibrahimserpici.googlemapsdummy;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Interpolator;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.security.Provider;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, android.location.LocationListener {

    GoogleMap googleMap;
    protected LocationListener locationListener;
    protected LocationManager locationManager;
    protected Context context;
    String provider;
    int x = 1;
    MarkerOptions shuttleMarkerOptions;
    String iconName = "bus_sprite_" + x;
    Marker shuttleMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isGoogleServicesAvailable()) {
            Toast.makeText(this, "Google Services are Available", Toast.LENGTH_LONG).show();
            initGoogleMaps();
        }


    }

    private void initGoogleMaps() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }


    public boolean isGoogleServicesAvailable() {

        boolean result = true;


        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            result = true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
            result = false;
        } else {
            Toast.makeText(this, "Cannot connect to the play services", Toast.LENGTH_LONG).show();
            result = false;
        }
        return result;
    }



    @Override
    public void onMapReady(final GoogleMap googleMap) {
        final Handler shuttleSpriteHandler = new Handler();
        Criteria criteria = new Criteria();

        locationManager = (LocationManager)getBaseContext().getSystemService(LOCATION_SERVICE);
        provider= locationManager.getBestProvider(criteria, true);

        locationManager.requestLocationUpdates(provider, 1L, 1f, this);

        this.googleMap = googleMap;
        double lat = 41.0082;
        double longi = 28.9784;
        goToLocation(lat, longi, 17);
        shuttleMarkerOptions = new MarkerOptions()
                .title("CQ")
                .snippet("Today We Are Going To Patel Brothers")
                .position(new LatLng(lat, longi))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(iconName, 150, 150)));

        shuttleMarker = this.googleMap.addMarker(shuttleMarkerOptions);


        shuttleSpriteHandler.post(new Runnable() {
            @Override
            public void run() {

                if(x<=4){
                    iconName = "bus_sprite_" + x;
                    x++;
                }else {
                    x=1;
                }

                shuttleMarker.setIcon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(iconName, 150, 150)));



                shuttleSpriteHandler.postDelayed(this,200);


            }
        });



    }

    private void goToLocation(double lat, double longi, float zoom) {
        LatLng latLng = new LatLng(lat, longi);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        googleMap.moveCamera(update);
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height) {

        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);


        return resizedBitmap;
    }

    @Override
    public void onLocationChanged(Location location) {
       // Toast.makeText(this, "LOCATION CHANGED ",Toast.LENGTH_LONG).show();

        animateMarker(googleMap,  shuttleMarker, new LatLng(location.getLatitude(),location.getLongitude()),false, location);



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






    public void animateMarker(final GoogleMap map, final Marker marker, final LatLng toPosition,
                              final boolean hideMarker, final Location location) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 2000;

        Location firstLocation = new Location(provider);
        firstLocation.setLatitude(marker.getPosition().latitude);
        firstLocation.setLongitude(marker.getPosition().longitude);
        Location secondLocation = new Location(provider);
        secondLocation.setLatitude(toPosition.latitude);
        secondLocation.setLongitude(toPosition.longitude);

        float rotation = firstLocation.bearingTo(secondLocation);
        shuttleMarker.setRotation(rotation);

        Log.e("ROTATION : ",String.valueOf(rotation));

        final LinearInterpolator interpolator = new LinearInterpolator();




        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;

                marker.setPosition(new LatLng(lat, lng));


                goToLocation(lat,lng, 17);

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }



}
