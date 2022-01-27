package ru.mikhailsv;

import ru.mikhailsv.domain.*;

public class TestData {
    public final static Booking BOOKING_1 = new Booking("AAA00F", "2017-07-05 03:12:00+03", 26500.00d);
    public final static Booking BOOKING_2 = new Booking("00000F", "2017-07-05 03:12:00+03", 265700.00);
    public final static Booking BOOKING_3 = new Booking("000012", "2017-07-14 09:02:00+03", 37900.00);


    public final static Ticket TICKET_1 = new Ticket("8885432159776", "00000F",
            "1708 262537", "ANNA ANTONOVA", "");
    public final static Ticket TICKET_2 = new Ticket("0005432000987", "06B046",
            "8149 604011", "VALERIY TIKHONOV", "");
    public final static Ticket TICKET_3 = new Ticket("0005432000988", "06B046",
            "8499 420203", "EVGENIYA ALEKSEEVA", "");


    public final static TicketFlight TICKET_FLIGHT_1 = new TicketFlight("8885432159776", 30625,
            "Economy", 26500.00d);
    public final static TicketFlight TICKET_FLIGHT_2 = new TicketFlight("0005432159776", 30625,
            "Business", 42100.00);
    public final static TicketFlight TICKET_FLIGHT_3 = new TicketFlight("0005435212357", 30625,
            "Comfort", 23900.00);


    public final static BoardingPass BOARDING_PASS_1 = new BoardingPass("8885432159776", 30625,
            24, "2A");
    public final static BoardingPass BOARDING_PASS_2 = new BoardingPass("0005435212351", 30625,
            1, "2D");
    public final static BoardingPass BOARDING_PASS_3 = new BoardingPass("0005435212386", 30625,
            2, "3G");

    public final static Flight FLIGHT = new Flight(30625, "PG0013", "2017-07-16 18:15:00+03",
            "2017-07-16 20:00:00+03", "AER", "SVO", "Arrived",
            "773", "2017-07-16 18:18:00+03", "2017-07-16 20:04:00+03");



    // App test data
    public final static String MODEL = "Boeing 777-300";

    public final static String PERIOD_FROM = "2017-08-01";
    public final static String PERIOD_TO = "2017-08-30";

    public final static Booking BOOKING = new Booking("AAA00F", "2017-07-05 03:12:00+03", 26500.00d);
    public final static Ticket TICKET = new Ticket("8885432159776", "00000F",
            "1708 262537", "ANNA ANTONOVA", "");
    public final static TicketFlight TICKET_FLIGHT = new TicketFlight("8885432159776", 30625,
            "Economy", 26500.00d);
    public final static BoardingPass BOARDING_PASS = new BoardingPass("8885432159776",
            30625, 24, "2A");
}
