package com.lachguer.localisation.Repository;

import com.lachguer.localisation.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByImei(String imei);
    Position findTopByOrderByDateDesc();
}