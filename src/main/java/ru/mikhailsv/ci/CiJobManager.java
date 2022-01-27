package ru.mikhailsv.ci;

import com.opencsv.exceptions.CsvException;

import lombok.AllArgsConstructor;

import ru.mikhailsv.DbInitManager;
import ru.mikhailsv.analytics.ExcelTableBuilder;
import ru.mikhailsv.analytics.QueryManager;
import ru.mikhailsv.support.Pair;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.LinkedHashMap;

@AllArgsConstructor
public final class CiJobManager {
    private final QueryManager queryManager;
    private final DbInitManager dbInitManager;
    private final ExcelTableBuilder excelTableBuilder = new ExcelTableBuilder();


    public void executeJob(String jobName) throws  NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        Method method = CiJobManager.class.getDeclaredMethod(jobName);
        method.invoke(this);
    }

    private void downloadDbFiles() throws IOException {
        dbInitManager.downloadData();
    }

    // B1
    private void analyseCitiesSeveralAirports() throws SQLException, IOException, CsvException {
        dbInitManager.setupDb(false);
        excelTableBuilder.createTwoColumnTable(
                queryManager.analyseCityAirports(),
                "Cities with several airports",
                "CitiesSeveralAirports.xlsx",
                "City",
                "Airports"
        );
    }

    // B2
    private void analyseCancellationsByCity() throws SQLException, IOException, CsvException {
        dbInitManager.setupDb(false);
        excelTableBuilder.createTwoColumnTable(
                queryManager.analyseCancellationsCntByCity(),
                "Cancellation count by city",
                "CancellationByCity.xlsx",
                "City",
                "Number of cancellations"
        );
    }

    // B3
    private void analyseShortestRouteByCity() throws SQLException, IOException, CsvException {
        dbInitManager.setupDb(false);
        excelTableBuilder.createTableForRoute(
                queryManager.analyseShortestRouteByCity(),
                "Shortest route for each city",
                "ShortestRouteByCity.xlsx",
                "Average travel time (minutes)"
        );
    }


    // B4
    private void analyseCancellationsByMonth() throws SQLException, IOException, CsvException {
        dbInitManager.setupDb(false);
        excelTableBuilder.createTwoColumnTable(
                queryManager.analyseCancellationsCntByMonth(),
                "Cancellation count by month",
                "CancellationByMonth.xlsx",
                "Month",
                "Number of cancellations"
        );
    }

    // B5
    private void analyseMoscowFlights() throws SQLException, IOException, CsvException {
        dbInitManager.setupDb(false);
        Pair<LinkedHashMap<String, Integer>> result = queryManager.analyseMoscowFlights();
        excelTableBuilder.createTwoColumnTable(
                result.getFirst(), // to flights
                "Flights to Moscow by WeekDay count",
                "FlightsToMoscowByWeekDay.xlsx",
                "WeekDay",
                "Number of flights"
        );
        excelTableBuilder.createTwoColumnTable(
                result.getSecond(), // to flights
                "Flights from Moscow by WeekDay count",
                "FlightsFromMoscowByWeekDay.xlsx",
                "WeekDay",
                "Number of flights"
        );
    }
}
