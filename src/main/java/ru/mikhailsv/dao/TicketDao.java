package ru.mikhailsv.dao;

import ru.mikhailsv.JdbcTemplate;
import ru.mikhailsv.domain.Ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


public final class TicketDao extends AirTransDao<Ticket> {

    public TicketDao(JdbcTemplate source) {
        super(source, Ticket.TABLE_NAME, Ticket.FEATURES_COUNT);
    }


    @Override
    protected Ticket extractObject(String[] line) {
        assert line.length == Ticket.FEATURES_COUNT;
        line[4] = line[4].isEmpty() ? null : line[4];
        return new Ticket(line[0], line[1], line[2], line[3], line[4]);
    }

    @Override
    protected Ticket extractFromResultSet(ResultSet rs) throws SQLException {
        return new Ticket(
                rs.getString("ticket_no"),
                rs.getString("book_ref"),
                rs.getString("passenger_id"),
                rs.getString("passenger_name"),
                rs.getString("contact_data"));
    }

    @Override
    protected void setPreparedStatement(PreparedStatement stmt, Ticket obj) throws SQLException {
        stmt.setString(1, obj.getTicketNumber());
        stmt.setString(2, obj.getBookRef());
        stmt.setString(3, obj.getPassengerId());
        stmt.setString(4, obj.getPassengerName());
        stmt.setString(5, obj.getContactData());
    }


    public void deleteByTicketNumber(Connection conn, List<String> values) throws SQLException {
        String qMarks = "?,".repeat(values.size()).replaceFirst(",$", "");
        String query = String.format("DELETE FROM tickets\n" +
                "WHERE ticket_no IN (%s);", qMarks);

        source.preparedStatementWithConnection(conn, query, stmt -> {
            for (int i = 0; i < values.size(); ++i) {
                stmt.setString(i + 1, values.get(i));
            }
            stmt.executeUpdate();
        });
    }

}
