package com.lachguer.localisation.Repository;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lachguer.localisation.Model.LocationData;

import java.util.HashMap;
import java.util.Map;

public class LocationRepository {
    private static final String BASE_URL = "http://your-spring-boot-server:8080/api/";
    private final RequestQueue requestQueue;

    public LocationRepository(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public void sendLocationData(LocationData locationData) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, BASE_URL + "locations",
                response -> {
                    // Handle success
                },
                error -> {
                    // Handle error
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("latitude", String.valueOf(locationData.getLatitude()));
                params.put("longitude", String.valueOf(locationData.getLongitude()));
                params.put("timestamp", String.valueOf(locationData.getTimestamp().getTime()));
                params.put("imei", locationData.getImei());
                params.put("accuracy", String.valueOf(locationData.getAccuracy()));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
}