package ru.mikhailsv.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public final class Airport {
    @NonNull
    private final String airportCode;
    @NonNull
    private final String airportName; // English by default
    @NonNull
    private final String city; // English by default
    @NonNull
    private final String coordinates;
    @NonNull
    private final String timezone;

    public static final String TABLE_NAME = "airports";
    public static final int FEATURES_COUNT = 5;

}
