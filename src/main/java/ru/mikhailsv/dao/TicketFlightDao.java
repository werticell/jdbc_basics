package ru.mikhailsv.dao;

import ru.mikhailsv.JdbcTemplate;
import ru.mikhailsv.domain.TicketFlight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public final class TicketFlightDao extends AirTransDao<TicketFlight> {

    public TicketFlightDao(JdbcTemplate source) {
        super(source, TicketFlight.TABLE_NAME, TicketFlight.FEATURES_COUNT);
    }


    @Override
    protected TicketFlight extractObject(String[] line) {
        assert line.length == TicketFlight.FEATURES_COUNT;
        return new TicketFlight(line[0], Integer.valueOf(line[1]), line[2], Double.valueOf(line[3]));
    }

    @Override
    protected TicketFlight extractFromResultSet(ResultSet rs) throws SQLException {
        return new TicketFlight(
                rs.getString("ticket_no"),
                rs.getInt("flight_id"),
                rs.getString("fare_conditions"),
                rs.getDouble("amount"));
    }

    @Override
    protected void setPreparedStatement(PreparedStatement stmt, TicketFlight obj) throws SQLException {
        stmt.setString(1, obj.getTicketNumber());
        stmt.setInt(2, obj.getFlightId());
        stmt.setString(3, obj.getFareConditions());
        stmt.setDouble(4, obj.getAmount());
    }


    public void deleteByFlightId(Connection conn, List<Integer> values) throws SQLException {
        String qMarks = "?,".repeat(values.size()).replaceFirst(",$", "");
        String query = String.format("DELETE FROM ticket_flights\n" +
                "WHERE flight_id IN (%s);", qMarks);

        source.preparedStatementWithConnection(conn, query, stmt -> {
            for (int i = 0; i < values.size(); ++i) {
                stmt.setInt(i + 1, values.get(i));
            }
            stmt.executeUpdate();
        });
    }

    public ArrayList<String> getTicketNumbersByFlightIds(Connection conn, List<Integer> values) throws SQLException {
        String qMarks = "?,".repeat(values.size()).replaceFirst(",$", "");
        String query = String.format("SELECT ticket_no\n" +
                "FROM ticket_flights\n" +
                "WHERE flight_id IN (%s);", qMarks);
        return source.preparedStatementWithConnection(conn, query, stmt -> {
            ArrayList<String> result = new ArrayList<>();
            for (int i = 0; i < values.size(); ++i) {
                stmt.setInt(i + 1, values.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("ticket_no"));
            }
            return result;
        });
    }

    public ArrayList<String> peekTicketNumbersByFlightIds(Connection conn, List<Integer> values) throws SQLException {
        ArrayList<String> result = getTicketNumbersByFlightIds(conn, values);
        deleteByFlightId(conn, values);
        return result;
    }

}
