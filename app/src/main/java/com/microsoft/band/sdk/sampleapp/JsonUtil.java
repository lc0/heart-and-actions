package com.microsoft.band.sdk.sampleapp;

import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandHeartRateEvent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by khomenkos on 03/10/15.
 */
public class JsonUtil {
    static String toJson(String name, BandHeartRateEvent event) {

        JSONObject jsonObj = new JSONObject();
        JSONObject jsonHeartRateObj = new JSONObject();
        try {
            jsonHeartRateObj.put("heartrate", Integer.toString(event.getHeartRate()));
            jsonHeartRateObj.put("quality", event.getQuality().toString());

            jsonObj.put(name, jsonHeartRateObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return jsonObj.toString();
    }
    static String toJson(String name, BandAccelerometerEvent event) {

        JSONObject jsonObj = new JSONObject();
        JSONObject jsonHeartRateObj = new JSONObject();
        try {
            jsonHeartRateObj.put("x", Float.toString(event.getAccelerationX()));
            jsonHeartRateObj.put("y", Float.toString(event.getAccelerationY()));
            jsonHeartRateObj.put("z", Float.toString(event.getAccelerationZ()));

            jsonObj.put(name, jsonHeartRateObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return jsonObj.toString();
    }
}
