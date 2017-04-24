package com.example.rajat.safehighwayapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PathMeasure;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.internal.PlaceEntity;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.example.rajat.safehighwayapp.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private LatLng[] waypoints;
    private double max_lat, max_long;
    private Polyline pllne;
    private int PROXIMITY_RADIUS = 10000;
    Marker[] mWaypoints = new Marker[8];
    List<Polyline> polylines = new ArrayList<Polyline>();
    private boolean route_found = true;
    private boolean clicked;
    private GoogleApiClient mGoogleApiClient;
    private LatLng Start_location = null, End_location = null, Start_location_current = null;
    private AutoCompleteTextView mToAutocompleteView;
    private AutoCompleteTextView mToAutocompleteView_1;
    private String Start_location_text = "";
    private String End_location_text = "";
    private boolean exit = false;
    private PlaceAutocompleteAdapter mToAdapter;
    private PlaceAutocompleteAdapter mToAdapter_1;
    private Marker mStartmarker, mEndmarker;
    private LatLngBounds BOUNDS;
    private boolean visible = false;
    private Location mLastLocation;
    private LatLng current_pos;
    private LocationRequest mLocationRequest;

    private class PostTask extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String... params) {
            if (Start_location!=null && End_location!=null)
            {
                markPoints();
            }
            return null;
        }
    }

    private void markPoints()
    {
        Log.d("TAGL","START_END");

    }

    public void gotonavigation(View view) {
        Intent intent = new Intent(MapsActivity.this,Navigation.class);
        double[] latitudes = new double[8];
        double[] longitudes = new double[8];
        for (int i=0; i<8; i++)
        {
            latitudes[i] = waypoints[i].latitude;
            longitudes[i] = waypoints[i].longitude;
        }
        intent.putExtra("waypoints_lat",latitudes);
        intent.putExtra("waypoints_long",longitudes);
        intent.putExtra("route_found",route_found);
        if (clicked) {
            intent.putExtra("start_location_lat", Start_location_current.latitude);
            intent.putExtra("start_location_long", Start_location_current.longitude);
            intent.putExtra("start_location_text", "Current");
        }
        else
        {
            intent.putExtra("start_location_lat", Start_location.latitude);
            intent.putExtra("start_location_long", Start_location.longitude);
            intent.putExtra("start_location_text", Start_location_text);
        }
        intent.putExtra("end_location_lat", End_location.latitude);
        intent.putExtra("end_location_long", End_location.longitude);
        intent.putExtra("end_location_text", End_location_text);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (exit) {
            moveTaskToBack(true);
            System.exit(0);
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }

    private void showpopup() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.settings_popup_layout);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        final EditText edit = (EditText) dialog.findViewById(R.id.number);
        try {
            InputStream inputStream = openFileInput("number.txt");
            if (inputStream != null)
            {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                try {
                    edit.setText(bufferedReader.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        dialog.setTitle("");
        Button sv = (Button) dialog.findViewById(R.id.save);
        sv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("number.txt", Context.MODE_PRIVATE));
                    try {
                        if (edit.getText().toString().length() != 10)
                        {
                            Toast.makeText(getBaseContext(), "Invalid number!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            outputStreamWriter.write(edit.getText().toString());
                            outputStreamWriter.close();
                            dialog.dismiss();
                            Toast.makeText(getBaseContext(), "Number saved!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        clicked = false;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) checkLocationPermission();
        new PostTask().execute();
        ImageButton ab = (ImageButton) findViewById(R.id.settings);
        ab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showpopup();
            }
        });
    }
    private AdapterView.OnItemClickListener mToAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */

            AutoCompleteTextView st_loc = (AutoCompleteTextView) findViewById(R.id.Start_location);
            Start_location_text = st_loc.getText().toString();
            final AutocompletePrediction item = mToAdapter.getItem(position);
            final String placeId = String.valueOf(item.getPlaceId());
            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mToUpdatePlaceDetailsCallback);
            Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(PlaceBuffer places) {
                            if (places.getStatus().isSuccess()) {
                                final Place myPlace = places.get(0);
                                LatLng queriedLocation = myPlace.getLatLng();
                                if (!clicked) {
                                    Start_location = new LatLng(queriedLocation.latitude, queriedLocation.longitude);
                                }
                                MapFunction();
                            }
                            places.release();
                        }
                    });

        }
    };
    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
    private AdapterView.OnItemClickListener mToAutocompleteClickListener_1 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
            AutoCompleteTextView ed_loc = (AutoCompleteTextView) findViewById(R.id.End_location);
            End_location_text = ed_loc.getText().toString();
            final AutocompletePrediction item = mToAdapter_1.getItem(position);
            final String placeId = String.valueOf(item.getPlaceId());

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mToUpdatePlaceDetailsCallback);
            Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(PlaceBuffer places) {
                            if (places.getStatus().isSuccess()) {
                                final Place myPlace = places.get(0);
                                LatLng queriedLocation_1 = myPlace.getLatLng();
                                End_location = new LatLng(queriedLocation_1.latitude, queriedLocation_1.longitude);
                                MapFunction();
                            }
                            places.release();
                        }
                    });

        }
    };

    private void MapFunction()
    {
        if (((!clicked && Start_location!=null) || (clicked)) && End_location!=null)
        {
            for(Polyline line : polylines)
            {
                line.remove();
            }
            if (!visible) {
                findViewById(R.id.loader).setVisibility(View.VISIBLE);
                findViewById(R.id.navigation).setVisibility(View.INVISIBLE);
                visible = true;
            }
            AutoCompleteTextView st_location = (AutoCompleteTextView) findViewById(R.id.Start_location);
            AutoCompleteTextView ed_location = (AutoCompleteTextView) findViewById(R.id.End_location);
            RelativeLayout abc = (RelativeLayout) findViewById(R.id.layout);
            abc.requestFocus();
            hideSoftKeyboard();
            MarkerOptions markerOptions = new MarkerOptions();
            if (!clicked)
            {
                markerOptions.position(Start_location);
                st_location.setText(Start_location_text);
            }
            else {
                 markerOptions.position(Start_location_current);
            }
            ed_location.setText(End_location_text);
            if (clicked)
                markerOptions.title("Current Location (Start Location)");
            else
                markerOptions.title(Start_location_text+" (Start Location)");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            if (mStartmarker != null) {
                mStartmarker.remove();
            }
            mStartmarker = mMap.addMarker(markerOptions);
            MarkerOptions markerOptions_1 = new MarkerOptions();
            markerOptions_1.position(End_location);
            markerOptions_1.title(End_location_text+" (End Location)");
            markerOptions_1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            if (mEndmarker != null) {
                mEndmarker.remove();
            }
            mEndmarker = mMap.addMarker(markerOptions_1);
            if (!clicked) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds.builder()
                                .include(Start_location)
                                .include(End_location)
                                .build(),
                        300));
            }
            else
            {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds.builder()
                                .include(Start_location_current)
                                .include(End_location)
                                .build(),
                        300));
            }
            String url;
            if (!clicked) {
                url = getDirectionsUrl(Start_location, End_location);
            }
            else {
                url = getDirectionsUrl(Start_location_current, End_location);
            }
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);
        }
    }
    private ResultCallback<PlaceBuffer> mToUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e("TAGL", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }

            places.release();
        }
    };

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();
    }

    public void currentToggle(View view) {
        ImageButton button_current = (ImageButton) findViewById(R.id.current_location);
        AutoCompleteTextView start_location = (AutoCompleteTextView) findViewById(R.id.Start_location);
        if (clicked) {
            button_current.setBackgroundResource(R.drawable.current_loc_non_selected);
            start_location.setText(Start_location_text);
            start_location.setFocusable(true);
            start_location.setFocusableInTouchMode(true);
            if (Start_location_text=="")
                start_location.requestFocus();
        } else {
            button_current.setBackgroundResource(R.drawable.current_loc_hover);
            start_location.setFocusable(false);
            start_location.setFocusableInTouchMode(false);
            start_location.setText("Current Location");
            Start_location_current = current_pos;
        }
        clicked = !clicked;
        MapFunction();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0,450,0,0);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    public static String getUserCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toUpperCase(Locale.US);
            }
            else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toUpperCase(Locale.US);
                }
            }
        }
        catch (Exception e) { }
        return null;
    }

    private String getUrl_nearby(double latitude, double longitude) {
        String lat = "lt="+latitude;
        String lng = "lng="+longitude;
        String parameters = lat+"&"+lng;
        String url = "http://10.1.1.19/~2015CSB1026/safe_highway/safe_points.php?"+parameters;
        return url;
    }

    @Override
    public void onLocationChanged(Location location) {
        BOUNDS = new LatLngBounds(new LatLng(-85,-180), new LatLng(85,180));
        mLastLocation = location;
        AutocompleteFilter filter = new AutocompleteFilter.Builder().
                setTypeFilter(Place.TYPE_COUNTRY).setCountry(getUserCountry(getApplicationContext())).build();
        mToAutocompleteView = (AutoCompleteTextView)findViewById(R.id.Start_location);
        mToAutocompleteView.setOnItemClickListener(mToAutocompleteClickListener);
        mToAdapter = new PlaceAutocompleteAdapter(this,mGoogleApiClient, BOUNDS, filter);
        mToAutocompleteView.setAdapter(mToAdapter);
        if (!clicked) {
            //Start_location_text = mToAutocompleteView.getText().toString();
            Log.d("TAGL","AB"+Start_location_text);
        }
        mToAutocompleteView_1 = (AutoCompleteTextView)findViewById(R.id.End_location);
        mToAutocompleteView_1.setOnItemClickListener(mToAutocompleteClickListener_1);
        mToAdapter_1 = new PlaceAutocompleteAdapter(this,mGoogleApiClient, BOUNDS, filter);
        mToAutocompleteView_1.setAdapter(mToAdapter_1);
        //End_location_text = mToAutocompleteView_1.getText().toString();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        current_pos = new LatLng(location.getLatitude(), location.getLongitude());

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        MapFunction();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    private String getDirectionsUrl_1(LatLng origin,LatLng dest, LatLng[] waypoints){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        String waypoint_string = "waypoints=";
        for (int i=0; i<7; i++)
        {
            waypoint_string += (waypoints[i].latitude+","+waypoints[i].longitude)+"|";
        }
        waypoint_string += (waypoints[7].latitude+","+waypoints[7].longitude);

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+waypoint_string+"&"+sensor;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    private String getUrl(double lat, double longi, double radius)
    {
        String url = "http://10.1.1.19/~2015CSB1026/safe_highway/query.php?lat=" + lat+ "&long="+longi+"&rad="+radius;
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Except while download", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        protected LatLng nearestWayPoint(LatLng point, double radius)
        {
            String url = getUrl(point.latitude, point.longitude, radius);
            DownloadTask_2 download_2 = new DownloadTask_2();
            try {
                String result = download_2.execute(url).get();
                String [] values = result.split("\\s+");
                max_lat = Double.parseDouble(values[0]);
                max_long = Double.parseDouble(values[1]);
                return new LatLng(max_lat, max_long);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return point;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            if (result.size() == 0)
            {
                Toast.makeText(getBaseContext(), "No route found.",
                        Toast.LENGTH_SHORT).show();
                route_found = false;
            }
            else {
                for (int i = 0; i < 1; i++) {
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }
                    LatLng[] eq_dist_points = new LatLng[8];
                    waypoints = new LatLng[8];
                    for (int j = 0; j < 8; j++) {
                        eq_dist_points[j] = points.get(points.size() / 9 * (j + 1));
                    }
                    for (int j = 0; j < 8; j+=2) {
                        DownloadTask_nearby task_nearby = new DownloadTask_nearby();
                        task_nearby.execute(getUrl_nearby(eq_dist_points[j].latitude, eq_dist_points[j].longitude));
                    }
                    double radius = Math.pow(Math.pow(eq_dist_points[0].latitude - eq_dist_points[1].latitude, 2) + Math.pow(eq_dist_points[0].longitude - eq_dist_points[1].longitude, 2), 0.5)/5.0;
                    for (int j = 0; j < 8; j++) {
                        waypoints[j] = nearestWayPoint(eq_dist_points[j], radius);
                        MarkerOptions markerOptions_1 = new MarkerOptions();
                        markerOptions_1.position(waypoints[j]);
                        markerOptions_1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        if (mWaypoints[j] != null) {
                            mWaypoints[j].remove();
                        }
                        mWaypoints[j] = mMap.addMarker(markerOptions_1);
                    }
                    String url;
                    if (!clicked) {
                        url = getDirectionsUrl_1(Start_location, End_location, waypoints);
                    } else {
                        url = getDirectionsUrl_1(Start_location_current, End_location, waypoints);
                    }
                    lineOptions.addAll(points);
                    lineOptions.width(7);
                    lineOptions.color(Color.RED);
                    // Drawing polyline in the Google Map for the i-th route
                    pllne = mMap.addPolyline(lineOptions);
                    polylines.add(pllne);
                    DownloadTask_1 downloadTask = new DownloadTask_1();
                    downloadTask.execute(url);
                    // Adding all the points in the route to LineOptions
                }
            }
        }
    }

    private class DownloadTask_nearby extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask_nearby parserTask = new ParserTask_nearby();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask_nearby extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }
    }

    private class DownloadTask_1 extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask_1 parserTask = new ParserTask_1();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }
    private class DownloadTask_2 extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        LatLng point()
        {
            return new LatLng(max_lat, max_long);
        }
    }

    private class ParserTask_1 extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            Log.e ("TAGL","BD: "+jsonData[0]);
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            if (result.size() == 0)
            {
                if (route_found && polylines.size()!=0) {
                    lineOptions = new PolylineOptions();
                    lineOptions.addAll(polylines.get(polylines.size() - 1).getPoints());
                    lineOptions.width(7);
                    lineOptions.color(Color.rgb(0, 0x7F, 0xFF));
                    pllne = mMap.addPolyline(lineOptions);
                    polylines.add(pllne);
                    visible = false;
                    findViewById(R.id.loader).setVisibility(View.INVISIBLE);
                    findViewById(R.id.navigation).setVisibility(View.VISIBLE);
                }
            }
            else {
                // Traversing through all the routes
                for (int i = 0; i < 1; i++) {
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }
                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(7);
                    lineOptions.color(Color.rgb(0, 0x7F, 0xFF));
                }
                // Drawing polyline in the Google Map for the i-th route
                pllne = mMap.addPolyline(lineOptions);
                polylines.add(pllne);
                visible = false;
                findViewById(R.id.loader).setVisibility(View.INVISIBLE);
                findViewById(R.id.navigation).setVisibility(View.VISIBLE);
            }
        }
    }
}
