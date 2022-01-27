package ru.mikhailsv.domain;

import lombok.Data;
import lombok.NonNull;

@Data
public final class Booking {
    @NonNull
    private final String bookRef;
    @NonNull
    private final String bookDate;
    @NonNull
    private final Double totalAmount;

    public static final String TABLE_NAME = "bookings";
    public static final int FEATURES_COUNT = 3;

}
