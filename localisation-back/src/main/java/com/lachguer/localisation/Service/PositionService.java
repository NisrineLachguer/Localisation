package com.lachguer.localisation.Service;

import com.lachguer.localisation.model.Position;
import com.lachguer.localisation.Repository.PositionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PositionService {

    @Autowired
    private PositionRepository positionRepository;

    public Position createPosition(Double latitude, Double longitude, String imei, HttpServletRequest request) {
        Position position = new Position();
        position.setLatitude(latitude);
        position.setLongitude(longitude);
        position.setDate(new Date());
        position.setImei(imei);
        position.setIpAddress(getClientIpAddress(request));
        return positionRepository.save(position);
    }

    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }

    public List<Position> getPositionsByImei(String imei) {
        return positionRepository.findByImei(imei);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    public Position getLastPosition() {
        // Trier par date décroissante et limiter à 1 résultat
        return positionRepository.findTopByOrderByDateDesc();
    }
}