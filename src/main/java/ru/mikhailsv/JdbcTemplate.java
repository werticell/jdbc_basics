package ru.mikhailsv;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;


@AllArgsConstructor
public final class JdbcTemplate {
    @Accessors
    public static String defaultLocale = "en";


    @FunctionalInterface
    public interface SQLFunction<T, R> {
        R apply(T object) throws SQLException;
    }

    @FunctionalInterface
    public interface SQLConsumer<T> {
        void accept(T object) throws SQLException;
    }

    private final DataSource connectionPool;

    public void connection(SQLConsumer<? super Connection> consumer) throws SQLException {
        Objects.requireNonNull(consumer);
        try (Connection conn = connectionPool.getConnection()) {
            consumer.accept(conn);
        }
    }

    public <R> R connection(SQLFunction<? super Connection, ? extends R> function) throws SQLException {
        Objects.requireNonNull(function);
        try (Connection conn = connectionPool.getConnection()) {
            return function.apply(conn);
        }
    }


    public <R> R statement(SQLFunction<? super Statement, ? extends R> function) throws SQLException {
        Objects.requireNonNull(function);
        return connection(conn -> {
            try (Statement stmt = conn.createStatement()) {
                return function.apply(stmt);
            }
        });
    }

    public void statement(SQLConsumer<? super Statement> consumer) throws SQLException {
        Objects.requireNonNull(consumer);
        connection(conn -> {
            try (Statement stmt = conn.createStatement()) {
                consumer.accept(stmt);
            }
        });
    }

    public <R> R statementUnderTx(SQLFunction<? super Statement, ? extends R> function) throws SQLException {
        Objects.requireNonNull(function);
        return connection(conn -> {
            try (Statement stmt = conn.createStatement()) {
                try {
                    conn.setAutoCommit(false);
                    R result = function.apply(stmt);
                    conn.commit();
                    return result;
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        });
    }

    public void statementUnderTx(SQLConsumer<? super Statement> consumer) throws SQLException {
        Objects.requireNonNull(consumer);
        connection(conn -> {
            try (Statement stmt = conn.createStatement()) {
                try {
                    conn.setAutoCommit(false);
                    consumer.accept(stmt);
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        });
    }

    public <R> R preparedStatement(String sql,
                                   SQLFunction<? super PreparedStatement, ? extends R> function) throws SQLException {
        Objects.requireNonNull(function);
        return connection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                return function.apply(stmt);
            }
        });
    }

    public void preparedStatement(String sql, SQLConsumer<? super PreparedStatement> consumer) throws SQLException {
        Objects.requireNonNull(consumer);
        connection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                consumer.accept(stmt);
            }
        });
    }


    public void statementWithConnection(Connection conn, SQLConsumer<? super Statement> consumer) throws SQLException {
        Objects.requireNonNull(consumer);
        try (Statement stmt = conn.createStatement()) {
            consumer.accept(stmt);
        }
    }

    public <R> R
    statementWithConnection(Connection conn, SQLFunction<? super Statement, ? extends R> function) throws SQLException {
        Objects.requireNonNull(function);
        try (Statement stmt = conn.createStatement()) {
            return function.apply(stmt);
        }
    }

    public <R> R preparedStatementWithConnection(
            Connection conn, String sql,
            SQLFunction<? super PreparedStatement, ? extends R> function) throws SQLException {
        Objects.requireNonNull(function);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            return function.apply(stmt);
        }
    }

    public void preparedStatementWithConnection(
            Connection conn, String sql, SQLConsumer<? super PreparedStatement> consumer) throws SQLException {
        Objects.requireNonNull(consumer);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            consumer.accept(stmt);
        }
    }

}
