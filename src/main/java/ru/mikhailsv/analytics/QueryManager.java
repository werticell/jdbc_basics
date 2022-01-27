package ru.mikhailsv.analytics;


import ru.mikhailsv.JdbcTemplate;
import ru.mikhailsv.dao.*;
import ru.mikhailsv.domain.BoardingPass;
import ru.mikhailsv.domain.Booking;
import ru.mikhailsv.domain.Ticket;
import ru.mikhailsv.domain.TicketFlight;
import ru.mikhailsv.support.Pair;
import ru.mikhailsv.support.Route;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;


public final class QueryManager {
    private final JdbcTemplate source;

    private final FlightDao flightDao;
    private final TicketFlightDao ticketFlightDao;
    private final TicketDao ticketDao;
    private final AirportDao airportDao;
    private final BoardingPassDao boardingPassDao;
    private final SeatDao seatDao;
    private final BookingDao bookingDao;

    public QueryManager(JdbcTemplate source) {
        this.source = source;
        flightDao = new FlightDao(source);
        ticketFlightDao = new TicketFlightDao(source);
        ticketDao = new TicketDao(source);
        airportDao = new AirportDao(source);
        boardingPassDao = new BoardingPassDao(source);
        seatDao = new SeatDao(source);
        bookingDao = new BookingDao(source);
    }


    public void analyseAirTransDb(String model, String periodFrom, String periodTo,
                                  Booking booking, Ticket ticket, TicketFlight ticketFlight,
                                  BoardingPass boardingPass) throws SQLException, IOException {
        System.out.println(analyseCityAirports());
        System.out.println(analyseCancellationsCntByCity());
        System.out.println(analyseShortestRouteByCity());
        System.out.println(analyseCancellationsCntByMonth());
        System.out.println(analyseMoscowFlights());
        cancelFlightsByModel(model);
        cancelFlightDuringPeriod(periodFrom, periodTo);
        addNewTicket(booking, ticket, ticketFlight, boardingPass);
    }

    /**
     * Task B1. Prints cities which have more than 1 airport.
     */
    public HashMap<String, String> analyseCityAirports() throws SQLException {
        return airportDao.getCityWithAirportCount(2, false);
    }

    /**
     * Task B2. Prints the number of canceled flights in every city.
     */
    public LinkedHashMap<String, Integer> analyseCancellationsCntByCity() throws SQLException {
        return flightDao.getFlightsDelayedByCity(-1, false);
    }

    /**
     * Task B3. Prints the shortest route for every city.
     * (To determine the shortest it groups by every route, finds mean time for every route,
     * than groups by city of a departure airport and finds min by group).
     */
    public LinkedHashMap<Route, String> analyseShortestRouteByCity() throws SQLException, IOException {
        return flightDao.getShortestRoutesByCity(false);
    }

    /**
     * Task B4. It prints how many cancellations were in every month during the examined year.
     */
    public LinkedHashMap<String, Integer> analyseCancellationsCntByMonth() throws SQLException, IOException {
        return flightDao.getCancelledCntByMonth(false);
    }

    /**
     * Task B5. It prints flights which were to and from Moscow by every day of the week.
     * @return pair of maps from day_id to departures from and to moscow_cnt.
     * Order First - to, Second - from.
     */
    public Pair<LinkedHashMap<String, Integer>> analyseMoscowFlights() throws SQLException, IOException {
        List<LinkedHashMap<String, Integer>> mskFlights = flightDao.getMoscowFlightsByDay(false);
        Pair<LinkedHashMap<String, Integer>> result = new Pair<>();
        result.setFirst(mskFlights.get(0));
        result.setSecond(mskFlights.get(1));
        return result;
    }

    /**
     * Task B6. Cancels all flights which have model of an aircraft the same as model param.
     * Also deletes all the tickets which are associated with the canceled flights.
     *
     * @param model - model of an aircraft to delete from database
     */
    public void cancelFlightsByModel(String model) throws SQLException {
        source.statementUnderTx(stmt -> {
            // Select flightIds from `flights` table and set status to Cancelled
            ArrayList<Integer> flightIds = flightDao.peekFlightIdsByModel(stmt.getConnection(), model);

            // Select ticketNumbers from `ticket_flights` table and delete by flight_id
            ArrayList<String> ticketNumbers =
                    ticketFlightDao.peekTicketNumbersByFlightIds(stmt.getConnection(), flightIds);

            // Delete by ticket number from `tickets` table
            ticketDao.deleteByTicketNumber(stmt.getConnection(), ticketNumbers);
            // Delete by ticket number from `boarding_passes` table
            boardingPassDao.deleteByTicketNumber(stmt.getConnection(), ticketNumbers);
        });
    }

    /**
     * Task B7. Cancels all flights during the period and counts loss of money by every day of period.
     *
     * @param from - start of a period to cancel flights
     * @param to   - end of a period to cancel flights
     */
    public void cancelFlightDuringPeriod(String from, String to) throws SQLException {
        source.statementUnderTx(stmt -> {
            ArrayList<Integer> fromMsk = flightDao.peekFlightFromCityDuringPeriod(
                    stmt.getConnection(), "Moscow", from, to);
            ArrayList<Integer> toMsk = flightDao.peekFlightToCityDuringPeriod(
                    stmt.getConnection(), "Moscow", from, to);
            System.out.printf("From Msk flights cnt = %d, To Msk flights cnt = %d\n", fromMsk.size(), toMsk.size());
            HashMap<Integer, Double> losses = flightDao.countLossesByYearDay(
                    stmt.getConnection(), fromMsk, toMsk);

            System.out.printf("Losses size is %d\n", losses.size());
            for (Map.Entry<Integer, Double> entry : losses.entrySet()) {
                System.out.printf("For day = %d, loss is %d\n", entry.getKey(), entry.getValue().intValue());
            }
        });
    }

    /**
     * Task B8. Query for adding a new ticket in the database
     */
    public void addNewTicket(Booking booking, Ticket ticket,
                             TicketFlight ticketFlight, BoardingPass boardingPass) throws SQLException {
        source.statementUnderTx(stmt -> {

            assert flightDao.flightIdExists(stmt.getConnection(), boardingPass.getFlightId());
            String aircraftCode = flightDao.getAircraftCodeById(stmt.getConnection(), boardingPass.getFlightId());
            assert seatDao.seatExists(stmt.getConnection(), aircraftCode, boardingPass.getSeatNumber());

            bookingDao.insertWithConnection(stmt.getConnection(), Collections.singleton(booking));
            ticketDao.insertWithConnection(stmt.getConnection(), Collections.singleton(ticket));
            ticketFlightDao.insertWithConnection(stmt.getConnection(), Collections.singleton(ticketFlight));
            boardingPassDao.insertWithConnection(stmt.getConnection(), Collections.singleton(boardingPass));
        });
    }
}



