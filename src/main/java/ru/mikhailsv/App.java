package ru.mikhailsv;

import lombok.NoArgsConstructor;
import org.h2.jdbcx.JdbcConnectionPool;
import ru.mikhailsv.analytics.QueryManager;
import ru.mikhailsv.ci.CiJobManager;


@NoArgsConstructor
public final class App {

    public static void main(String[] args) {
        JdbcTemplate source = new JdbcTemplate(
                JdbcConnectionPool.create("jdbc:h2:mem:database;DB_CLOSE_DELAY=-1", "", ""));
        try {
            CiJobManager jobManager = new CiJobManager(new QueryManager(source), new DbInitManager(source));
            jobManager.executeJob(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
