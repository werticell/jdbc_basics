package ru.mikhailsv.dao;

import ru.mikhailsv.JdbcTemplate;
import ru.mikhailsv.domain.Booking;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class BookingDao extends AirTransDao<Booking> {

    public BookingDao(JdbcTemplate source) {
        super(source, Booking.TABLE_NAME, Booking.FEATURES_COUNT);
    }


    @Override
    protected Booking extractObject(String[] line) {
        assert line.length == Booking.FEATURES_COUNT;
        return new Booking(line[0], line[1], Double.valueOf(line[2]));
    }

    @Override
    protected Booking extractFromResultSet(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getString("book_ref"),
                rs.getString("book_date"),
                rs.getDouble("total_amount"));
    }

    @Override
    protected void setPreparedStatement(PreparedStatement stmt, Booking obj) throws SQLException {
        stmt.setString(1, obj.getBookRef());
        stmt.setString(2, obj.getBookDate());
        stmt.setDouble(3, obj.getTotalAmount());
    }

}
