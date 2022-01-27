package ru.mikhailsv;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ru.mikhailsv.analytics.QueryManager;

import java.sql.SQLException;

import static ru.mikhailsv.TestData.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AppTest {
    JdbcTemplate source = new JdbcTemplate(
            JdbcConnectionPool.create("jdbc:h2:mem:database;DB_CLOSE_DELAY=-1", "", ""));
    @Test
    @Disabled
    void testApp() {
        assertDoesNotThrow(() -> {
            DbInitManager manager = new DbInitManager(source);
            manager.setupDb(true);
            QueryManager queryManager = new QueryManager(source);
            queryManager.analyseAirTransDb(MODEL, PERIOD_FROM, PERIOD_TO, BOOKING, TICKET, TICKET_FLIGHT, BOARDING_PASS);
        });
    }

    @AfterAll
    void tearDownDB() throws SQLException {
        source.statement(stmt -> {
            stmt.execute("DROP ALL objects;");
        });
    }
}
