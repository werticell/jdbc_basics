package ru.mikhailsv.dao;

import ru.mikhailsv.DbInitManager;
import ru.mikhailsv.JdbcTemplate;
import ru.mikhailsv.domain.Flight;
import ru.mikhailsv.support.Route;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;


public final class FlightDao extends AirTransDao<Flight> {

    public FlightDao(JdbcTemplate source) {
        super(source, Flight.TABLE_NAME, Flight.FEATURES_COUNT);
    }


    @Override
    protected Flight extractObject(String[] line) {
        assert line.length == Flight.FEATURES_COUNT;
        line[8] = line[8].isEmpty() ? null : line[8];
        line[9] = line[9].isEmpty() ? null : line[9];
        return new Flight(Integer.valueOf(line[0]), line[1], line[2], line[3], line[4],
                          line[5], line[6], line[7], line[8], line[9]);
    }

    @Override
    protected Flight extractFromResultSet(ResultSet rs) throws SQLException {
        return new Flight(
                rs.getInt("flight_id"),
                rs.getString("flight_no"),
                rs.getString("scheduled_departure"),
                rs.getString("scheduled_arrival"),
                rs.getString("departure_airport"),
                rs.getString("arrival_airport"),
                rs.getString("status"),
                rs.getString("aircraft_code"),
                rs.getString("actual_departure"),
                rs.getString("actual_arrival"));
    }

    @Override
    protected void setPreparedStatement(PreparedStatement stmt, Flight obj) throws SQLException {
        stmt.setInt(1, obj.getFlightId());
        stmt.setString(2, obj.getFlightNumber());
        stmt.setString(3, obj.getScheduledDeparture());
        stmt.setString(4, obj.getScheduledArrival());
        stmt.setString(5, obj.getDepartureAirport());
        stmt.setString(6, obj.getArrivalAirport());
        stmt.setString(7, obj.getStatus());
        stmt.setString(8, obj.getAircraftCode());
        if (Objects.isNull(obj.getActualDeparture())) {
            stmt.setNull(9, Types.NULL);
        } else {
            stmt.setString(9, obj.getActualDeparture());
        }
        if (Objects.isNull(obj.getActualArrival())) {
            stmt.setNull(10, Types.NULL);
        } else {
            stmt.setString(10, obj.getActualArrival());
        }
    }

    /**
     * Returns cities where most flights were delayed
     * @param threshold - threshold for flights delayed
     * @return Map of CityName -> Number of flights that were delayed
     */
    public LinkedHashMap<String, Integer> getFlightsDelayedByCity(int threshold, boolean verbose) throws SQLException {
        String query = String.format(
                "SELECT airports.city, count(*) AS delayed_flights_cnt\n" +
                "FROM flights\n" +
                "INNER JOIN airports ON airports.airport_code = departure_airport\n" +
                "WHERE status = 'Cancelled'\n" +
                "GROUP BY airports.city\n" +
                "HAVING count(*) > %s\n" +
                "ORDER BY delayed_flights_cnt DESC;", threshold);
        if (verbose) {
            System.out.println(query);
        }
        return source.statement(stmt -> {
            LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                result.put(rs.getString("city"), rs.getInt("delayed_flights_cnt"));
            }
            return result;
        });
    }


    public LinkedHashMap<Route, String> getShortestRoutesByCity(boolean verbose) throws SQLException, IOException {
        String query = DbInitManager.getSql("shortestRouteByCity.sql");
        if (verbose) {
            System.out.println(query);
        }
        return source.statement(stmt -> {
            LinkedHashMap<Route, String> result = new LinkedHashMap<>();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                result.put(new Route(rs.getString("city"), rs.getString("arrival_airport")),
                        rs.getString("avg_by_route"));
            }
            return result;
        });
    }

    public LinkedHashMap<String, Integer> getCancelledCntByMonth(boolean verbose) throws SQLException, IOException {
        String query = DbInitManager.getSql("cancelledByMonth.sql");
        if (verbose) {
            System.out.println(query);
        }
        return source.statement(stmt -> {
            LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                result.put(rs.getString("month_id"), rs.getInt("cancelled_cnt"));
            }
            return result;
        });
    }

    public ArrayList<LinkedHashMap<String, Integer>>
    getMoscowFlightsByDay(boolean verbose) throws SQLException, IOException {
        String toQuery = DbInitManager.getSql("flightsToMoscowByDay.sql");
        String fromQuery = DbInitManager.getSql("flightsFromMoscowByDay.sql");
        return source.statementUnderTx(stmt -> {
            String[] queries = {toQuery, fromQuery};
            String[] columnLabels = {"departures_to_moscow_cnt", "departures_from_moscow_cnt"};
            ArrayList<LinkedHashMap<String, Integer>> result = new ArrayList<>();
            for (int i = 0; i < columnLabels.length; ++i) {
                LinkedHashMap<String, Integer> mskFlights = new LinkedHashMap<>();
                if (verbose) {
                    System.out.println(queries[i]);
                }
                ResultSet rs = stmt.executeQuery(queries[i]);
                while (rs.next()) {
                    mskFlights.put(rs.getString("day_id"), rs.getInt(columnLabels[i]));
                }
                result.add(mskFlights);

            }
            return result;
        });
    }

    public ArrayList<Integer> getFlightIdsByModel(Connection conn, String model) throws SQLException {
        String query = "SELECT flight_id\n" +
                "FROM flights\n" +
                "WHERE aircraft_code IN (SELECT aircraft_code FROM aircrafts WHERE model = ?)" +
                "AND status IN ('Scheduled', 'On Time', 'Delayed');";
        return source.preparedStatementWithConnection(conn, query, stmt -> {
            stmt.setString(1, model);
            ResultSet rs = stmt.executeQuery();
            ArrayList<Integer> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rs.getInt("flight_id"));
            }
            return result;
        });
    }

    public void cancelFlightsByModel(Connection conn, String model) throws SQLException {
        String query = "UPDATE flights\n" +
                "SET status = 'Canceled'\n" +
                "WHERE aircraft_code IN (SELECT aircraft_code FROM aircrafts WHERE model = ?);";
        source.preparedStatementWithConnection(conn, query, stmt -> {
            stmt.setString(1, model);
            stmt.executeUpdate();
        });
    }

    public ArrayList<Integer> peekFlightIdsByModel(Connection conn, String model) throws SQLException {
        ArrayList<Integer> result = getFlightIdsByModel(conn, model);
        cancelFlightsByModel(conn, model);
        return result;
    }

    private ArrayList<Integer>
    peekFlightForCity(Connection conn, String city, String from, String to, String state) throws SQLException {
        String selectQuery = String.format("SELECT flight_id\n" +
                "FROM flights INNER JOIN airports ON %s_airport = airport_code\n" +
                "WHERE (city = ? AND status IN ('Scheduled', 'On Time', 'Delayed') " +
                "AND scheduled_%s IS NOT NULL AND (scheduled_%s > ? and scheduled_%s < ?))",
                state, state, state, state);
        ArrayList<Integer> result = new ArrayList<>();
        source.preparedStatementWithConnection(conn, selectQuery, stmt -> {
            stmt.setString(1, city);
            stmt.setString(2, from);
            stmt.setString(3, to);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt("flight_id"));
            }
        });

        String updateQuery = String.format(
                "UPDATE flights SET status = 'Canceled'\n" +
                "WHERE flight_id IN (%s)", selectQuery);

        source.preparedStatementWithConnection(conn, updateQuery, stmt -> {
            stmt.setString(1, city);
            stmt.setString(2, from);
            stmt.setString(3, to);
            stmt.executeUpdate();
        });
        return result;
    }

    public ArrayList<Integer>
    peekFlightFromCityDuringPeriod(Connection conn, String city, String from, String to) throws SQLException {
        return peekFlightForCity(conn, city, from, to, "departure");
    }

    public ArrayList<Integer>
    peekFlightToCityDuringPeriod(Connection conn, String city, String from, String to) throws SQLException {
        return peekFlightForCity(conn, city, from, to, "arrival");
    }


    public HashMap<Integer, Double> countLossesByYearDay(
            Connection conn, ArrayList<Integer> from, ArrayList<Integer> to) throws SQLException {
        String fromIds = from.toString().replaceAll("^\\[|]$", "");
        String toIds = to.toString().replaceAll("^\\[|]$", "");
        String query = String.format("WITH cte AS (\n" +
                "    SELECT flight_id, DAY_OF_YEAR(scheduled_departure) AS date_of_loss\n" +
                "    FROM flights\n" +
                "    WHERE flight_id IN (%s)\n" +
                "    UNION\n" +
                "    SELECT flight_id, DAY_OF_YEAR(scheduled_arrival) AS date_of_loss\n" +
                "    FROM flights\n" +
                "    WHERE flight_id IN (%s)\n" +
                ")\n" +
                "SELECT date_of_loss, SUM(amount) AS loss_by_day\n" +
                "FROM cte INNER JOIN ticket_flights ON cte.flight_id = ticket_flights.flight_id\n" +
                "GROUP BY date_of_loss\n" +
                "ORDER BY loss_by_day DESC;", fromIds, toIds);

        return source.statementWithConnection(conn, stmt -> {
            HashMap<Integer, Double> result = new HashMap<>();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                result.put(rs.getInt("date_of_loss"), rs.getDouble("loss_by_day"));
            }
            return result;
        });
    }


    public boolean flightIdExists(Connection conn, Integer flightId) throws SQLException {
        String query = "SELECT EXISTS(SELECT * FROM flights WHERE flight_id = ?);";
        return source.preparedStatementWithConnection(conn, query, stmt -> {
            stmt.setInt(1, flightId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getBoolean(1);
        });
    }

    public String getAircraftCodeById(Connection conn, Integer flightId) throws SQLException {
        String query = "SELECT aircraft_code FROM flights WHERE flight_id = ?";
        return source.preparedStatementWithConnection(conn, query, stmt -> {
            stmt.setInt(1, flightId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getString(1);
        });
    }

}
