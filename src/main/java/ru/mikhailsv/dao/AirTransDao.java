package ru.mikhailsv.dao;

import lombok.Getter;
import ru.mikhailsv.JdbcTemplate;

import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


@AllArgsConstructor
public abstract class AirTransDao<T> {
    protected final JdbcTemplate source;
    @Getter
    protected final String tableName;
    @Getter
    protected final Integer featuresCount;



    public final void saveRaw(Iterable<String[]> raws) throws SQLException {
        insert(parse(raws));
    }

    public final void insert(Collection<T> collection) throws SQLException {
        String qMarks = "?,".repeat(featuresCount).replaceFirst(",$", "");
        String query = String.format("INSERT INTO %s VALUES(%s)", tableName, qMarks);
        source.preparedStatement(query, stmt -> {
            for (T obj : collection) {
                setPreparedStatement(stmt, obj);
                stmt.execute();
            }
        });
    }

    public final void insertWithConnection(Connection conn, Collection<T> collection) throws SQLException {
        String qMarks = "?,".repeat(featuresCount).replaceFirst(",$", "");
        String query = String.format("INSERT INTO %s VALUES(%s)", tableName, qMarks);
        source.preparedStatementWithConnection(conn, query, stmt -> {
            for (T obj : collection) {
                setPreparedStatement(stmt, obj);
                stmt.execute();
            }
        });
    }

    public final ArrayList<T> selectAll() throws SQLException {
        return source.statement(stmt -> {
            ArrayList<T> result = new ArrayList<>();
            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM %s", tableName));
            while (rs.next()) {
                result.add(extractFromResultSet(rs));
            }
            return result;
        });
    }

    public final int getTableSize() throws SQLException {
        return source.statement(stmt -> {
            ResultSet resultSet = stmt.executeQuery(String.format("SELECT COUNT(*) FROM %s", tableName));
            resultSet.next();
            return resultSet.getInt(1);
        });
    }


    protected abstract T extractObject(String[] line);

    protected final ArrayList<T> parse(Iterable<String[]> raws) {
        ArrayList<T> result = new ArrayList<>();
        for (String[] line : raws) {
            result.add(extractObject(line));
        }
        return result;
    }

    protected abstract T extractFromResultSet(ResultSet resultSet) throws SQLException;
    protected abstract void setPreparedStatement(PreparedStatement stmt, T obj) throws SQLException;



    protected static String extractFromJsonString(String jsonStr, String locale) {
        // expected format "{k1 : v1, k2 : v2, ..., kN : vN}"
        String trimmed = jsonStr.replaceAll("^\\{|}$|\"", "");
        return Arrays.stream(trimmed.split(","))
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(e -> e[0], e -> e[1])).get(locale).trim();
    }
}
