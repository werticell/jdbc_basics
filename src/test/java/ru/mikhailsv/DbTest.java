package ru.mikhailsv;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.mikhailsv.dao.BookingDao;
import ru.mikhailsv.dao.FlightDao;
import ru.mikhailsv.dao.TicketDao;
import ru.mikhailsv.domain.Booking;
import ru.mikhailsv.domain.Flight;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static ru.mikhailsv.TestData.*;
import static org.junit.jupiter.api.Assertions.*;


public class DbTest {
    private JdbcTemplate source = new JdbcTemplate(
            JdbcConnectionPool.create("jdbc:h2:mem:database;DB_CLOSE_DELAY=-1", "", ""));
    private final FlightDao flightDao = new FlightDao(source);
    private final BookingDao bookingDao = new BookingDao(source);
    private final TicketDao ticketDao = new TicketDao(source);


    @BeforeEach
    void setupDB() throws IOException, SQLException {
        new DbInitManager(source).createSchema();
    }

    @AfterEach
    void tearDownDB() throws SQLException {
        source.statement(stmt -> {
            stmt.execute("DROP ALL objects;");
        });
    }

    @Test
    void testSaveRaw() throws SQLException {
        String[] flight = {"30625", "PG0013", "2017-07-16 18:15:00+03", "2017-07-16 20:00:00+03", "AER", "SVO",
                "Arrived", "773", "2017-07-16 18:18:00+03", "2017-07-16 20:04:00+03"};
        flightDao.saveRaw(Collections.singleton(flight));
        ArrayList<Flight> res_1 = flightDao.selectAll();
        assertEquals(res_1.size(), 1);
        assertEquals(res_1.get(0), FLIGHT);

        String[] booking1 = {"AAA00F", "2017-07-05 03:12:00+03", "26500.00"};
        String[] booking2 = {"00000F", "2017-07-05 03:12:00+03", "265700.00"};
        bookingDao.saveRaw(List.of(booking1, booking2));
        ArrayList<Booking> res_2 = bookingDao.selectAll();
        res_2.sort(Comparator.comparing(Booking::getBookRef));
        assertEquals(res_2.size(), 2);
        assertEquals(res_2.get(0), BOOKING_2);
        assertEquals(res_2.get(1), BOOKING_1);
    }


    @Test
    void testInsert() throws SQLException {
        assertDoesNotThrow(ticketDao::getTableSize);
        assertEquals(ticketDao.getTableSize(), 0);
        ticketDao.insert(List.of(TICKET_1, TICKET_2));
        assertEquals(ticketDao.getTableSize(), 2);
        ticketDao.insert(List.of(TICKET_3));
        assertEquals(ticketDao.getTableSize(), 3);
        assertThrows(SQLException.class, () -> ticketDao.insert(List.of(TICKET_3)));
    }


    @Test
    void testSelect() throws SQLException {
        ticketDao.insert(List.of(TICKET_1, TICKET_2, TICKET_3));
        assertEquals(ticketDao.selectAll().size(), 3);
    }

    @Test
    void testTransaction() throws SQLException {
        ticketDao.insert(List.of(TICKET_1, TICKET_2, TICKET_3));
        JdbcTemplate.SQLConsumer<Connection> tx =
                (Connection conn) -> ticketDao.deleteByTicketNumber(conn, List.of("8885432159776"));

        source.statementUnderTx(stmt -> {
            tx.accept(stmt.getConnection());
            assertEquals(ticketDao.getTableSize(), 3); // tx haven't yet been committed
        });
        assertEquals(ticketDao.getTableSize(), 2); // tx have been committed
    }

    @Test
    void testTransactionFail() throws SQLException {
        ticketDao.insert(List.of(TICKET_1, TICKET_2));
        JdbcTemplate.SQLConsumer<Connection> tx = (Connection conn) -> {
            ticketDao.deleteByTicketNumber(conn, List.of("0005432000987"));
            throw new SQLException();
        };

        assertThrows(SQLException.class, () -> source.statementUnderTx(stmt -> {
            tx.accept(stmt.getConnection());
        }));
        assertEquals(ticketDao.getTableSize(), 2); // tx have been rollbacked
    }
}
