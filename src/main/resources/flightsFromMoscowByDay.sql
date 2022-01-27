SELECT day_id, count(*) AS departures_from_moscow_cnt
FROM (
         SELECT day_of_week(actual_departure) AS day_id,
         FROM flights
         INNER JOIN airports ON airport_code = departure_airport
         WHERE actual_departure IS NOT NULL AND city = 'Moscow'
     )
GROUP BY day_id
ORDER BY departures_from_moscow_cnt;