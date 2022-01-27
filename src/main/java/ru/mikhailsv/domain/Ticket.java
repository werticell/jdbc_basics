package ru.mikhailsv.domain;

import lombok.Data;
import lombok.NonNull;

@Data
public final class Ticket {
    @NonNull
    private final String ticketNumber;
    @NonNull
    private final String bookRef;
    @NonNull
    private final String passengerId;
    @NonNull
    private final String passengerName;
    private final String contactData;

    public static final String TABLE_NAME = "tickets";
    public static final int FEATURES_COUNT = 5;

}
