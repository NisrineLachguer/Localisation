package com.lachguer.localisation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private double latitude;
    private double longitude;
    private RequestQueue requestQueue;
    private boolean showMap = false;
    private Button btnShowMap;

    private String insertUrl = "http://10.0.2.2:8080/api/positions";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnShowMap = findViewById(R.id.btnShowLocation);
        btnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShowMapClick(v);
            }
        });

        requestQueue = Volley.newRequestQueue(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE
            }, 1);
            return;
        }

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        } catch (SecurityException e) {
            Log.e("LOCATION_ERROR", "SecurityException: " + e.getMessage());
            Toast.makeText(this, "Location permission required", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        // Envoyer le broadcast
        Intent broadcastIntent = new Intent("com.lachguer.localisation.NEW_POSITION");
        broadcastIntent.putExtra("latitude", latitude);
        broadcastIntent.putExtra("longitude", longitude);
        broadcastIntent.putExtra("imei", getUniqueDeviceId());
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

        addPosition(latitude, longitude);
    }

    private void addPosition(double lat, double lon) {
        String deviceId = getUniqueDeviceId();

        Map<String, Object> params = new HashMap<>();
        params.put("latitude", lat);
        params.put("longitude", lon);
        params.put("imei", deviceId);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, insertUrl, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String successMsg = "Position saved successfully";
                            if (response.has("id")) {
                                successMsg += " (ID: " + response.getInt("id") + ")";
                            }
                            Toast.makeText(MainActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                            Log.d("SAVE_POSITION", "Response: " + response.toString());
                        } catch (JSONException e) {
                            Log.e("JSON_ERROR", "Error parsing response", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = "Error saving position";
                        if (error.networkResponse != null) {
                            errorMessage += " (Code: " + error.networkResponse.statusCode + ")";
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                errorMessage += "\nResponse: " + responseBody;
                                Log.e("NETWORK_ERROR", responseBody);
                            } catch (Exception e) {
                                Log.e("NETWORK_ERROR", "Error parsing error response", e);
                            }
                        }
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        Log.e("SAVE_POSITION", errorMessage, error);
                    }
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        requestQueue.add(request);
        Log.d("NETWORK_REQUEST", "Sending position data: " + params.toString());
    }

    private String getUniqueDeviceId() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = "unknown";

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                if (Build.FINGERPRINT.startsWith("generic")) {
                    deviceId = "emulator_" + Build.SERIAL;
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        deviceId = Settings.Secure.getString(
                                getContentResolver(),
                                Settings.Secure.ANDROID_ID
                        );
                    } else {
                        deviceId = telephonyManager.getDeviceId();
                    }

                    if (deviceId == null || deviceId.isEmpty()) {
                        deviceId = "not_available";
                    }
                }
            } else {
                Log.d("DEVICE_ID", "Permission READ_PHONE_STATE not granted");
                deviceId = "permission_required";
            }
        } catch (Exception e) {
            Log.e("DEVICE_ID", "Exception: " + e.getMessage());
            deviceId = "error";
        }

        Log.d("DEVICE_ID", "Device ID: " + deviceId);
        return deviceId;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Required permissions not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onShowMapClick(View view) {
        Log.d("NAVIGATION", "Navigating to MapsActivity");

        Intent intent = new Intent(this, MapsActivity.class);

        if (latitude != 0 && longitude != 0) {
            intent.putExtra("is_new_position", true);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
        } else {
            intent.putExtra("is_new_position", false);
            Toast.makeText(this, "En attente d'une position GPS...", Toast.LENGTH_SHORT).show();
        }


        startActivity(intent);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Toast.makeText(this, "Provider enabled: " + provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this, "Provider disabled: " + provider, Toast.LENGTH_SHORT).show();
    }
}