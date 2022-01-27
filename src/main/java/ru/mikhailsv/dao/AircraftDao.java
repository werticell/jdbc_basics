package ru.mikhailsv.dao;


import ru.mikhailsv.JdbcTemplate;
import ru.mikhailsv.domain.Aircraft;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public final class AircraftDao extends AirTransDao<Aircraft> {

    public AircraftDao(JdbcTemplate source) {
        super(source, Aircraft.TABLE_NAME, Aircraft.FEATURES_COUNT);
    }


    @Override
    protected Aircraft extractObject(String[] line) {
        assert line.length == Aircraft.FEATURES_COUNT;
        String model = extractFromJsonString(line[1], JdbcTemplate.defaultLocale);
        return new Aircraft(line[0], model, Integer.valueOf(line[2]));
    }

    @Override
    protected Aircraft extractFromResultSet(ResultSet rs) throws SQLException {
        return new Aircraft(
                rs.getString("aircraft_code"),
                rs.getString("model"),
                rs.getInt("range"));
    }

    @Override
    protected void setPreparedStatement(PreparedStatement stmt, Aircraft obj) throws SQLException {
        stmt.setString(1, obj.getAircraftCode());
        stmt.setString(2, obj.getModel());
        stmt.setInt(3, obj.getRange());
    }
}
