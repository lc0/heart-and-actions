package com.microsoft.band.sdk.sampleapp;

import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;

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
    static String toJson(String name, BandSkinTemperatureEvent event) {

        JSONObject jsonObj = new JSONObject();
        JSONObject jsonHeartRateObj = new JSONObject();
        try {
            jsonHeartRateObj.put("temperature", Float.toString(event.getTemperature()));

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
            jsonHeartRateObj.put("ax", Float.toString(event.getAccelerationX()));
            jsonHeartRateObj.put("ay", Float.toString(event.getAccelerationY()));
            jsonHeartRateObj.put("az", Float.toString(event.getAccelerationZ()));

            jsonObj.put(name, jsonHeartRateObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return jsonObj.toString();
    }

    static String toJson(String name, BandGyroscopeEvent event) {

        JSONObject jsonObj = new JSONObject();
        JSONObject jsonHeartRateObj = new JSONObject();
        try {
            jsonHeartRateObj.put("ax", Float.toString(event.getAccelerationX()));
            jsonHeartRateObj.put("ay", Float.toString(event.getAccelerationY()));
            jsonHeartRateObj.put("az", Float.toString(event.getAccelerationZ()));

            jsonHeartRateObj.put("vx", Float.toString(event.getAngularVelocityX()));
            jsonHeartRateObj.put("vy", Float.toString(event.getAngularVelocityY()));
            jsonHeartRateObj.put("vz", Float.toString(event.getAngularVelocityZ()));

            jsonObj.put(name, jsonHeartRateObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return jsonObj.toString();
    }
}
