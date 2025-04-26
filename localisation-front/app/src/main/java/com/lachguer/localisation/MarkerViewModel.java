// MarkerViewModel.java
package com.lachguer.localisation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.List;

public class MarkerViewModel extends ViewModel {
    private final MutableLiveData<List<MarkerOptions>> markers = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<MarkerOptions>> getMarkers() {
        return markers;
    }

    public void addMarker(MarkerOptions marker) {
        List<MarkerOptions> currentMarkers = markers.getValue();
        if (currentMarkers != null) {
            currentMarkers.add(marker);
            markers.setValue(currentMarkers);
        }
    }

    public void clearMarkers() {
        markers.setValue(new ArrayList<>());
    }

    public void setMarkers(List<MarkerOptions> newMarkers) {
        markers.setValue(new ArrayList<>(newMarkers));
    }
}