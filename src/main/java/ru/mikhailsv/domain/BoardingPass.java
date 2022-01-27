package ru.mikhailsv.domain;

import lombok.Data;
import lombok.NonNull;

@Data
public final class BoardingPass {
    @NonNull
    private final String ticketNumber;
    @NonNull
    private final Integer flightId;
    @NonNull
    private final Integer boardingNumber;
    @NonNull
    private final String seatNumber;

    public static final String TABLE_NAME = "boarding_passes";
    public static final int FEATURES_COUNT = 4;


}
