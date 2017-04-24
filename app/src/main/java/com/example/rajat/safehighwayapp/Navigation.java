package com.example.rajat.safehighwayapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Navigation extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnCameraIdleListener, TextToSpeech.OnInitListener {

    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    private boolean increment = false;
    private boolean correction = false;
    private boolean send = false;
    private List<Direction_waypoint> dir_data;
    private boolean data_set = false;
    private int point_pointer = 0;
    int val = 0;
    private int point_pointer_json = 0;
    private LatLng Current_location;
    private LatLngBounds bounds;
    private Polyline pllne;
    private String Start_location_text, End_location_text;
    private Marker mStartmarker, mEndmarker;
    private GoogleApiClient mGoogleApiClient;
    private LatLng[] waypoints_1 = new LatLng[8];
    LatLng Start_location, End_location;
    List<Polyline> polylines = new ArrayList<Polyline>();
    ArrayList<LatLng> all_points = new ArrayList<LatLng>();
    private boolean route_found;
    private TextToSpeech tts;
    private boolean disable = false;
    private void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void func(Dialog dialog, int x)
    {
        ImageButton s1 = (ImageButton) dialog.findViewById(R.id.star_1);
        ImageButton s2 = (ImageButton) dialog.findViewById(R.id.star_2);
        ImageButton s3 = (ImageButton) dialog.findViewById(R.id.star_3);
        ImageButton s4 = (ImageButton) dialog.findViewById(R.id.star_4);
        ImageButton s5 = (ImageButton) dialog.findViewById(R.id.star_5);
        s1.setBackgroundResource(R.drawable.empty_star);
        s2.setBackgroundResource(R.drawable.empty_star);
        s3.setBackgroundResource(R.drawable.empty_star);
        s4.setBackgroundResource(R.drawable.empty_star);
        s5.setBackgroundResource(R.drawable.empty_star);
        if (x>=1)
            s1.setBackgroundResource(R.drawable.filled_star);
        if (x>=2)
            s2.setBackgroundResource(R.drawable.filled_star);
        if (x>=3)
            s3.setBackgroundResource(R.drawable.filled_star);
        if (x>=4)
            s4.setBackgroundResource(R.drawable.filled_star);
        if (x==5)
            s5.setBackgroundResource(R.drawable.filled_star);
    }
    private void showPopup() {
        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.popup_layout);
        dialog.setTitle("");
        ImageButton s1 = (ImageButton) dialog.findViewById(R.id.star_1);
        ImageButton s2 = (ImageButton) dialog.findViewById(R.id.star_2);
        ImageButton s3 = (ImageButton) dialog.findViewById(R.id.star_3);
        ImageButton s4 = (ImageButton) dialog.findViewById(R.id.star_4);
        ImageButton s5 = (ImageButton) dialog.findViewById(R.id.star_5);
        Button rate = (Button) dialog.findViewById(R.id.rate);
        s1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                func(dialog,1);
                val = 1;
            }
        });
        s2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                func(dialog,2);
                val = 2;
            }
        });
        s3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                func(dialog,3);
                val = 3;
            }
        });
        s4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                func(dialog,4);
                val = 4;
            }
        });
        s5.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                func(dialog,5);
                val = 5;
            }
        });
        rate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Thanks for the review!",
                        Toast.LENGTH_SHORT).show();
                for (int j=0; j<8; j++) {
                    DownloadTask_3 task_nearby = new DownloadTask_3();
                    task_nearby.execute(getUrl_add(waypoints_1[j].latitude, waypoints_1[j].longitude, val));
                }
                Intent intent = new Intent(Navigation.this,MapsActivity.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }


    private void showPopup_1() {
        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.directions);
        if (data_set) {
            ListView lv = (ListView) dialog.findViewById(R.id.list);
            List<String> your_array_list = new ArrayList<String>();
            for (int i = 0; i < dir_data.size(); i++) {
                try {
                    // Convert from Unicode to UTF-8
                    String string = dir_data.get(i).inst;
                    byte[] utf8 = string.getBytes("UTF-8");

                    // Convert from UTF-8 to Unicode
                    string = new String(utf8, "UTF-8");
                    String np = "";
                    boolean lp = false;
                    for (int j=0; j<string.length(); j++)
                    {
                        if (string.charAt(j)=='<' || string.charAt(j) == '&')
                        {
                            lp = true;
                        }
                        else if (string.charAt(j)=='>' || string.charAt(j)==';')
                        {
                            lp = false;
                        }
                        else if (!lp)
                        {
                            np = np+ string.charAt(j);
                        }

                    }
                    your_array_list.add(np);
                } catch (UnsupportedEncodingException e) {
                }
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this,
                    R.layout.list,
                    your_array_list);

            lv.setAdapter(arrayAdapter);
            dialog.setTitle("");
            dialog.show();
        }
    }
    private void showPopup_2() {
        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.safety_popup_layout);
        dialog.setTitle("");
        Button safe = (Button) dialog.findViewById(R.id.safe);
        Button unsafe = (Button) dialog.findViewById(R.id.unsafe);
        safe.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Thanks for the review!",
                        Toast.LENGTH_SHORT).show();
                DownloadTask_3 task_nearby = new DownloadTask_3();
                task_nearby.execute(getUrl_add(Current_location.latitude, Current_location.longitude, 3.0));
                bc.setBackgroundResource(R.drawable.disable);
                disable = true;
                dialog.dismiss();
            }
        });
        unsafe.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Thanks for the review!",
                        Toast.LENGTH_SHORT).show();
                DownloadTask_3 task_nearby = new DownloadTask_3();
                task_nearby.execute(getUrl_add(Current_location.latitude, Current_location.longitude, -3.0));
                bc.setBackgroundResource(R.drawable.disable);
                disable = true;
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void alert_function()
    {
        String num = "";
        if (send) {
            try {
                InputStream inputStream = openFileInput("number.txt");
                if (inputStream != null)
                {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    try {
                        num = bufferedReader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String add = getAddress(Current_location.latitude, Current_location.longitude);
            SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(num, null, "HELP!\n\nI'm in danger. My current location is -\n\n"+add+"\n\nLat: "+Current_location.latitude+"\nLng: "+Current_location.longitude, null, null);
                Toast.makeText(getApplicationContext(), "Help is on the way",
                        Toast.LENGTH_LONG).show();
            DownloadTask_3 task_nearby = new DownloadTask_3();
            task_nearby.execute(getUrl_add(Current_location.latitude, Current_location.longitude, -7.0));

        } else {
            Toast.makeText(this, "Press again to send red alert.",
                    Toast.LENGTH_SHORT).show();
            send = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    send = false;
                }
            }, 3 * 1000);
        }
    }
    public String getAddress(double lat, double lng)
    {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + ", " + obj.getLocality() + ", " + obj.getAdminArea();
            return add;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    private String getUrl_add(double lat, double longi, double safety)
    {
        String url = "http://10.1.1.19/~2015CSB1026/safe_highway/addition.php?lat=" + lat+ "&long="+longi+"&safety="+safety;
        return url;
    }
    
    private class DownloadTask_3 extends AsyncTask<String, Void, String>{

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
            Navigation.ParserTask_3 parserTask = new Navigation.ParserTask_3();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask_3 extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            Log.e ("TAGL","BD: "+jsonData[0]);
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {

        }
    }
    ImageButton bc = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        route_found = getIntent().getExtras().getBoolean("route_found");
        Start_location = new LatLng(getIntent().getExtras().getDouble("start_location_lat"),getIntent().getExtras().getDouble("start_location_long"));
        if (route_found) {
            double[] waypoint_lat = getIntent().getDoubleArrayExtra("waypoints_lat");
            double[] waypoint_long = getIntent().getDoubleArrayExtra("waypoints_long");
            for (int i = 0; i < 8; i++) {
                waypoints_1[i] = new LatLng(waypoint_lat[i], waypoint_long[i]);
            }
        }
        ImageButton ab = (ImageButton) findViewById(R.id.img);
        ab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showPopup_1();
            }
        });
        ImageButton alert = (ImageButton) findViewById(R.id.alert);
        alert.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alert_function();
            }
        });

        bc = (ImageButton) findViewById(R.id.unsafe);
        bc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!disable)
                    showPopup_2();
            }
        });
        Current_location = new LatLng(getIntent().getExtras().getDouble("start_location_lat"),getIntent().getExtras().getDouble("start_location_long"));
        End_location = new LatLng(getIntent().getExtras().getDouble("end_location_lat"),getIntent().getExtras().getDouble("end_location_long"));
        Start_location_text = getIntent().getStringExtra("start_location_text");
        End_location_text = getIntent().getStringExtra("end_location_text");
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    speak("Let's start navigating. Drive Straight");

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });

    }

    private void mapfunction()
    {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(Start_location);
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
        String url;
        if (route_found) {
            url = getDirectionsUrl_1(Start_location, End_location, waypoints_1);
        }
        else {
            url = getDirectionsUrl(Start_location, End_location);
        }
        Navigation.DownloadTask downloadTask = new Navigation.DownloadTask();
        downloadTask.execute(url);
        Navigation.DownloadTask_1 downloadTask_1 = new Navigation.DownloadTask_1();
        downloadTask_1.execute(url);
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

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(this);
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
        mapfunction();

    }


    @Override
    public void onLocationChanged(Location location) {
        Current_location = new LatLng(location.getLatitude(), location.getLongitude());
        bc.setBackgroundResource(R.drawable.safety);
        disable = false;
        Log.e("TAGL","CL: "+location.getLatitude()+" "+location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(Current_location)       // Sets the center of the map to Mountain View
                .zoom(17)
                .build();                   // Creates a CameraPosition from the builder
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        correction = false;

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        if (data_set)
        {
            if ((End_location.latitude-location.getLatitude())<0.00001 && Math.abs(End_location.longitude-location.getLongitude())<0.00001)
            {
                TextView dir = (TextView)findViewById(R.id.direction);
                tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            int result = tts.setLanguage(Locale.US);
                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "This Language is not supported");
                            }
                            speak("You've reached your destination");

                        } else {
                            Log.e("TTS", "Initilization Failed!");
                        }
                    }
                });
                dir.setText("You've reached your destination");
                showPopup();
            }
            else {
                if (Math.abs(dir_data.get(point_pointer_json).lat - location.getLatitude()) < 0.00001 && Math.abs(dir_data.get(point_pointer_json).lng - location.getLongitude()) < 0.00001) {
                    if (!increment) {
                        TextView dir = (TextView) findViewById(R.id.direction);
                        try {
                            // Convert from Unicode to UTF-8
                            String string = dir_data.get(point_pointer_json).inst;
                            byte[] utf8 = string.getBytes("UTF-8");

                            // Convert from UTF-8 to Unicode
                            string = new String(utf8, "UTF-8");
                            final String finalString = string;
                            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                                @Override
                                public void onInit(int status) {
                                    if (status == TextToSpeech.SUCCESS) {
                                        int result = tts.setLanguage(Locale.US);
                                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                            Log.e("TTS", "This Language is not supported");
                                        }
                                        speak(finalString);

                                    } else {
                                        Log.e("TTS", "Initilization Failed!");
                                    }
                                }
                            });
                            dir.setText(string);

                        } catch (UnsupportedEncodingException e) {
                        }
                        point_pointer_json++;
                        increment = true;
                    }

                } else {
                    increment = false;
                    TextView dir = (TextView) findViewById(R.id.direction);
                    tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status == TextToSpeech.SUCCESS) {
                                int result = tts.setLanguage(Locale.US);
                                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                    Log.e("TTS", "This Language is not supported");
                                }
                                speak("Drive Straight");

                            } else {
                                Log.e("TTS", "Initilization Failed!");
                            }
                        }
                    });
                    dir.setText("Drive Straight");
                }
            }
        }
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onCameraIdle() {

    }

    @Override
    public void onInit(int status) {

    }

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

            Navigation.ParserTask parserTask = new Navigation.ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
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

            Navigation.ParserTask_1 parserTask = new Navigation.ParserTask_1();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            Log.e("TAGL", "BD: " + jsonData[0]);
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
            if (result.size() == 0) {
                if (route_found && polylines.size() != 0) {
                    lineOptions = new PolylineOptions();
                    lineOptions.addAll(polylines.get(polylines.size() - 1).getPoints());
                    lineOptions.width(15);
                    lineOptions.color(Color.rgb(0, 0x7F, 0xFF));
                    pllne = mMap.addPolyline(lineOptions);
                    polylines.add(pllne);
                }
            } else {
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
                        all_points.add(position);
                    }
                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(15);
                    lineOptions.color(Color.rgb(0, 0x7F, 0xFF));
                }
                // Drawing polyline in the Google Map for the i-th route
                pllne = mMap.addPolyline(lineOptions);
                polylines.add(pllne);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(Current_location)
                        .zoom(17)
                        .build();                   // Creates a CameraPosition from the builder
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }
    private class ParserTask_1 extends AsyncTask<String, Integer, List<Direction_waypoint>> {

        @Override
        protected List<Direction_waypoint> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<Direction_waypoint> directions = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser_1 parser = new DirectionsJSONParser_1();

                // Starts parsing data
                directions = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dir_data = directions;
            Log.d("TAGL","HJPL: "+dir_data.size());
            return directions;
        }

        @Override
        protected void onPostExecute(List<Direction_waypoint> result) {
            data_set = true;
        }
    }
}
