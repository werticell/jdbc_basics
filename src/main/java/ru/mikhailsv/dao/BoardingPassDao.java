package ru.mikhailsv.dao;

import ru.mikhailsv.JdbcTemplate;
import ru.mikhailsv.domain.BoardingPass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public final class BoardingPassDao extends AirTransDao<BoardingPass> {

    public BoardingPassDao(JdbcTemplate source) {
        super(source, BoardingPass.TABLE_NAME, BoardingPass.FEATURES_COUNT);
    }


    @Override
    protected BoardingPass extractObject(String[] line) {
        assert line.length == BoardingPass.FEATURES_COUNT;
        return new BoardingPass(line[0], Integer.valueOf(line[1]), Integer.valueOf(line[2]), line[3]);
    }

    @Override
    protected BoardingPass extractFromResultSet(ResultSet rs) throws SQLException {
        return new BoardingPass(
                rs.getString("ticket_no"),
                rs.getInt("flight_id"),
                rs.getInt("boarding_no"),
                rs.getString("seat_no"));
    }

    @Override
    protected void setPreparedStatement(PreparedStatement stmt, BoardingPass obj) throws SQLException {
        stmt.setString(1, obj.getTicketNumber());
        stmt.setInt(2, obj.getFlightId());
        stmt.setInt(3, obj.getBoardingNumber());
        stmt.setString(4, obj.getSeatNumber());
    }

    public void deleteByTicketNumber(Connection conn, List<String> values) throws SQLException {
        String qMarks = "?,".repeat(values.size()).replaceFirst(",$", "");
        String query = String.format("DELETE FROM boarding_passes\n" +
                "WHERE ticket_no IN (%s);", qMarks);

        source.preparedStatementWithConnection(conn, query, stmt -> {
            for (int i = 0; i < values.size(); ++i) {
                stmt.setString(i + 1, values.get(i));
            }
            stmt.executeUpdate();
        });
    }

}
