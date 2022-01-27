package ru.mikhailsv.dao;

import ru.mikhailsv.JdbcTemplate;
import ru.mikhailsv.domain.Seat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public final class SeatDao extends AirTransDao<Seat> {

    public SeatDao(JdbcTemplate source) {
        super(source, Seat.TABLE_NAME, Seat.FEATURES_COUNT);
    }


    @Override
    protected Seat extractObject(String[] line) {
        assert line.length == Seat.FEATURES_COUNT;
        return new Seat(line[0], line[1], line[2]);
    }

    @Override
    protected Seat extractFromResultSet(ResultSet rs) throws SQLException {
        return new Seat(
                rs.getString("aircraft_code"),
                rs.getString("seat_no"),
                rs.getString("fare_conditions"));
    }

    @Override
    protected void setPreparedStatement(PreparedStatement stmt, Seat obj) throws SQLException {
        stmt.setString(1, obj.getAircraftCode());
        stmt.setString(2, obj.getSeatNumber());
        stmt.setString(3, obj.getFareConditions());
    }


    public boolean seatExists(Connection conn, String aircraftCode, String seatNo) throws SQLException {
        String query = "SELECT EXISTS(SELECT * FROM seats WHERE aircraft_code = ? AND seat_no = ?)";
        return source.preparedStatementWithConnection(conn, query, stmt -> {
            stmt.setString(1, aircraftCode);
            stmt.setString(2, seatNo);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getBoolean(1);
        });
    }

}
