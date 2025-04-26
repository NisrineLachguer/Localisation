package com.lachguer.localisation.Controller;

import com.lachguer.localisation.Service.PositionService;
import com.lachguer.localisation.model.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @PostMapping
    public Position createPosition(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        Double latitude = Double.parseDouble(payload.get("latitude").toString());
        Double longitude = Double.parseDouble(payload.get("longitude").toString());
        String imei = payload.get("imei").toString();

        return positionService.createPosition(latitude, longitude, imei, request);
    }

    @GetMapping
    public List<Position> getAllPositions() {
        return positionService.getAllPositions();
    }

    @GetMapping("/{imei}")
    public List<Position> getPositionsByImei(@PathVariable String imei) {
        return positionService.getPositionsByImei(imei);
    }

    @GetMapping("/last")
    public Position getLastPosition() {
        return positionService.getLastPosition();
    }
}