package com.example.rajat.safehighwayapp;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by rajat on 31/3/17.
 */
public class DirectionsJSONParser_1 {

    public List<Direction_waypoint> parse(JSONObject jObject){

        List<Direction_waypoint> instructions = new ArrayList<Direction_waypoint>() ;
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = jObject.getJSONArray("routes");
            Direction_waypoint new_dir;
            for(int i=0;i<1;i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");

                Log.d("TAGL","PLO: ");

                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");


                    for(int k=0;k<jSteps.length();k++){
                        new_dir = new Direction_waypoint();
                        new_dir.inst  = (String)((JSONObject)jSteps.get(k)).get("html_instructions");
                        new_dir.lat = (double)((JSONObject)((JSONObject)jSteps.get(k)).get("end_location")).get("lat");
                        new_dir.lng = (double)((JSONObject)((JSONObject)jSteps.get(k)).get("end_location")).get("lng");
                        new_dir.dist = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("distance")).get("text");
                        Log.e("TAGL","PK: "+new_dir.inst+" "+new_dir.lat+" "+new_dir.lng);
                        instructions.add(new_dir);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }
        return instructions;
    }
}
