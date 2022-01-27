package ru.mikhailsv;

import com.opencsv.exceptions.CsvException;
import lombok.AllArgsConstructor;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;
import ru.mikhailsv.dao.*;

@AllArgsConstructor
public final class DbInitManager {
    private final JdbcTemplate source;

    private final String[] filenames = {
            "boarding_passes", "airports", "tickets", "aircrafts",
            "bookings", "flights", "seats", "ticket_flights"};

    private final Path pathToData = Path.of("target", "data");


    public static String getSql(String name) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        App.class.getClassLoader().getResourceAsStream(name),
                        StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    public void setupDb(boolean downloadData) throws IOException, SQLException, CsvException {
        if (downloadData) {
            downloadData();
        }
        createSchema();
        fillDb();
    }

    public void createSchema() throws SQLException, IOException {
        String ddlQuery = getSql("AirTransSchema.sql");
        source.statement(stmt -> {
            stmt.execute(ddlQuery);
        });
    }

    private void fillDb() throws IOException, CsvException, SQLException {
        List<AirTransDao<?>> daos = List.of(
                new AircraftDao(source), new AirportDao(source), new BoardingPassDao(source), new BookingDao(source),
                new FlightDao(source), new SeatDao(source), new TicketDao(source), new TicketFlightDao(source));
        for (AirTransDao<?> dao : daos) {
            String pathToCsv = String.format("target/data/%s.csv", dao.getTableName());
            try (CSVReader reader = new CSVReader(new FileReader(pathToCsv))) {
                dao.saveRaw(reader);
            }
        }
    }



    public void downloadData() throws IOException {
        Files.createDirectories(pathToData);
        for (String name : filenames) {
            Path filepath = Path.of(pathToData.toAbsolutePath().toString(), String.format("%s.csv", name));
            File file = new File(filepath.toAbsolutePath().toString());
            URL url = new URL(String.format("https://storage.yandexcloud.net/airtrans-small/%s.csv", name));
            try (InputStream in = url.openStream();
                 ReadableByteChannel rbc = Channels.newChannel(in);
                 FileOutputStream fos = new FileOutputStream(file.getAbsolutePath())) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
        }
    }

}
