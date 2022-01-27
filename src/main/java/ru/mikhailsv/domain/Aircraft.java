package ru.mikhailsv.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;


@Data
@AllArgsConstructor
public final class Aircraft {
    @NonNull
    private final String aircraftCode;
    @NonNull
    private final String model; // In english by default
    @NonNull
    private final Integer range;

    public static final String TABLE_NAME = "aircrafts";
    public static final int FEATURES_COUNT = 3;

}
