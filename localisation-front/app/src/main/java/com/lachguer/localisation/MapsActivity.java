package com.lachguer.localisation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MAPS_DEBUG";
    private GoogleMap mMap;
    private RequestQueue requestQueue;
    private String showUrl = "http://10.0.2.2:8080/api/positions";
    private boolean isNewPosition = false;
    private double newPositionLat = 0;
    private double newPositionLon = 0;
    private MarkerViewModel markerViewModel;
    private Button btnShowMarkers;


    private BroadcastReceiver positionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
                double lat = intent.getDoubleExtra("latitude", 0);
                double lon = intent.getDoubleExtra("longitude", 0);
                String imei = intent.getStringExtra("imei");

                Log.d(TAG, "Position reçue par broadcast: lat=" + lat + ", lon=" + lon);
                addNewMarker(lat, lon, imei);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Log.d(TAG, "Démarrage de MapsActivity");

        // Initialiser le ViewModel
        markerViewModel = new ViewModelProvider(this).get(MarkerViewModel.class);

        // Récupérer les coordonnées de la nouvelle position si disponibles
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isNewPosition = extras.getBoolean("is_new_position", false);
            Log.d(TAG, "is_new_position: " + isNewPosition);
            if (isNewPosition) {
                newPositionLat = extras.getDouble("latitude", 0);
                newPositionLon = extras.getDouble("longitude", 0);
                Log.d(TAG, "Coordonnées reçues: lat=" + newPositionLat + ", lon=" + newPositionLon);
            }
        } else {
            Log.d(TAG, "Aucun extra dans l'intent");
        }

        // Initialiser la file de requêtes Volley
        requestQueue = Volley.newRequestQueue(this);
        Log.d(TAG, "RequestQueue initialisée");

        // Obtenir le fragment de carte et le configurer
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            Log.d(TAG, "Demande d'initialisation de la carte envoyée");
        } else {
            Log.e(TAG, "Erreur: MapFragment non trouvé");
        }

        // Enregistrer le receiver pour les nouvelles positions
        LocalBroadcastManager.getInstance(this).registerReceiver(
                positionReceiver, new IntentFilter("com.lachguer.localisation.NEW_POSITION"));
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(positionReceiver);
        super.onDestroy();
    }

    private void handleRealTimeUpdates() {

        markerViewModel.getMarkers().observe(this, markers -> {
            mMap.clear();
            for (MarkerOptions marker : markers) {
                mMap.addMarker(marker);
            }

            if (!markers.isEmpty()) {
                LatLng lastPosition = markers.get(markers.size()-1).getPosition();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, 15));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        handleRealTimeUpdates();

        loadPositions();
    }

    private void addNewMarker(double lat, double lon, String imei) {
        Log.d(TAG, "Ajout de la nouvelle position: lat=" + lat + ", lon=" + lon);
        LatLng newPos = new LatLng(lat, lon);

        String currentTime = new SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault()).format(new Date());
        String title = "Position du " + currentTime;
        if (imei != null) {
            title += " (IMEI: " + imei + ")";
        }

        MarkerOptions markerOptions = new MarkerOptions()
                .position(newPos)
                .title(title);

        markerViewModel.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 15));
        Log.d(TAG, "Caméra déplacée sur la nouvelle position");
    }

    private void loadPositions() {
        Log.d(TAG, "Démarrage du chargement des positions");

        String lastPositionUrl = showUrl + "/last";
        Log.d(TAG, "URL de requête: " + lastPositionUrl);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                lastPositionUrl,
                null,
                response -> {
                    try {
                        Log.d(TAG, "Réponse du serveur reçue: " + response.toString());

                        if (!response.has("latitude") || !response.has("longitude")) {
                            Log.e(TAG, "Réponse incomplète: latitude ou longitude manquante");
                            Toast.makeText(MapsActivity.this, "Données de position incomplètes", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double lat = response.getDouble("latitude");
                        double lon = response.getDouble("longitude");

                        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                            Log.e(TAG, "Coordonnées invalides: lat=" + lat + ", lon=" + lon);
                            Toast.makeText(MapsActivity.this, "Coordonnées invalides", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String imei = response.optString("imei", "inconnu");
                        String date = response.optString("date", "date inconnue");

                        LatLng loc = new LatLng(lat, lon);
                        String title = "Dernière position (" + date + ") - IMEI: " + imei;

                        MarkerOptions marker = new MarkerOptions()
                                .position(loc)
                                .title(title);

                        markerViewModel.setMarkers(Collections.singletonList(marker));

                        if (!isNewPosition) {
                            runOnUiThread(() -> {
                                if (mMap != null) {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
                                    Log.d(TAG, "Caméra déplacée vers: " + loc);
                                }
                            });
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "Erreur de parsing JSON: " + e.getMessage(), e);
                        Toast.makeText(MapsActivity.this, "Erreur de format des données", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMsg = "Erreur réseau: ";
                    if (error.networkResponse != null) {
                        errorMsg += "Code " + error.networkResponse.statusCode;
                        try {
                            errorMsg += ", " + new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur lors de la lecture de la réponse d'erreur", e);
                        }
                    } else {
                        errorMsg += error.getMessage();
                    }

                    Log.e(TAG, errorMsg, error);
                    Toast.makeText(MapsActivity.this, "Erreur de chargement des positions", Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(this::loadPositions, 5000);
                }
        );

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000, // 5 secondes timeout
                2, // 2 tentatives de réessai
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonObjectRequest);
    }

    private List<PositionData> positionCache = new ArrayList<>();

    private static class PositionData {
        double latitude;
        double longitude;
        String imei;
        String date;

        PositionData(double lat, double lon, String imei, String date) {
            this.latitude = lat;
            this.longitude = lon;
            this.imei = imei;
            this.date = date;
        }
    }
}