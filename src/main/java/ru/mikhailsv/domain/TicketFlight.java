package ru.mikhailsv.domain;

import lombok.Data;
import lombok.NonNull;

@Data
public final class TicketFlight {
    @NonNull
    private final String ticketNumber;
    @NonNull
    private final Integer flightId;
    @NonNull
    private final String fareConditions;
    @NonNull
    private final Double amount;

    enum SeatCondition {
        kEconomy, kComfort, kBusiness
    }

    public static final String TABLE_NAME = "ticket_flights";
    public static final int FEATURES_COUNT = 4;


}
