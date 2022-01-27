package ru.mikhailsv.dao;

import ru.mikhailsv.JdbcTemplate;
import ru.mikhailsv.domain.Airport;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public final class AirportDao extends AirTransDao<Airport> {

    public AirportDao(JdbcTemplate source) {
        super(source, Airport.TABLE_NAME, Airport.FEATURES_COUNT);
    }


    @Override
    protected Airport extractObject(String[] line) {
        assert line.length == Airport.FEATURES_COUNT;
        String airportName = extractFromJsonString(line[1], JdbcTemplate.defaultLocale);
        String city = extractFromJsonString(line[2], JdbcTemplate.defaultLocale);
        return new Airport(line[0], airportName, city, line[3], line[4]);
    }

    @Override
    protected Airport extractFromResultSet(ResultSet rs) throws SQLException {
        return new Airport(
                rs.getString("airport_code"),
                rs.getString("airport_name"),
                rs.getString("city"),
                rs.getString("coordinates"),
                rs.getString("timezone"));
    }

    @Override
    protected void setPreparedStatement(PreparedStatement stmt, Airport obj) throws SQLException {
        stmt.setString(1, obj.getAirportCode());
        stmt.setString(2, obj.getAirportName());
        stmt.setString(3, obj.getCity());
        stmt.setString(4, obj.getCoordinates());
        stmt.setString(5, obj.getTimezone());
    }

    /**
     * Return cities that have more than threshold airports in it.
     * @param threshold - leaves cities with airports more or equals than threshold only
     * @return ArrayList of City names
     */
    public HashMap<String, String> getCityWithAirportCount(int threshold, boolean verbose) throws SQLException {
        String query = String.format(
                "SELECT city, STRING_AGG(airport_code, ',') AS airports\n" +
                "FROM %s\n" +
                "GROUP BY city\n" +
                "HAVING count(*) >= %s;", tableName, threshold);
        if (verbose) {
            System.out.println(query);
        }
        return source.statement(stmt -> {
            HashMap<String, String> result = new HashMap<>();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                result.put(rs.getString("city"), rs.getString("airports"));
            }
            return result;
        });
    }

}
