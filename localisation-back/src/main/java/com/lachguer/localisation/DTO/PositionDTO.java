package com.lachguer.localisation.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PositionDTO {
    @NotNull
    private double latitude;

    @NotNull
    private double longitude;

    @NotBlank
    private String imei;

    private float accuracy;
}