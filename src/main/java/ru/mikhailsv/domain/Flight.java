package ru.mikhailsv.domain;

import lombok.Data;
import lombok.NonNull;

@Data
public final class Flight {
    @NonNull
    private final Integer flightId;
    @NonNull
    private final String flightNumber;
    @NonNull
    private final String scheduledDeparture;
    @NonNull
    private final String scheduledArrival;
    @NonNull
    private final String departureAirport;
    @NonNull
    private final String arrivalAirport;
    @NonNull
    private final String status;
    @NonNull
    private final String aircraftCode;
    private final String actualDeparture;
    private final String actualArrival;

    enum FlightStatus {
        kScheduled, kOnTime, kDelayed, kDeparted, kArrived, kCancelled
    }

    public static final String TABLE_NAME = "flights";
    public static final int FEATURES_COUNT = 10;


}
