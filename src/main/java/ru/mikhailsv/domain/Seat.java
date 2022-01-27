package ru.mikhailsv.domain;

import lombok.Data;
import lombok.NonNull;

@Data
public final class Seat {
    @NonNull
    private final String aircraftCode;
    @NonNull
    private final String seatNumber;
    @NonNull
    private final String fareConditions;

    enum SeatCondition {
        kEconomy, kComfort, kBusiness
    }

    public static final String TABLE_NAME = "seats";
    public static final int FEATURES_COUNT = 3;

}
