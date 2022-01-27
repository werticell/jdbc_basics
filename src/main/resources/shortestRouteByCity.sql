WITH route_avg AS (
    SELECT departure_airport, arrival_airport, AVG(datediff(MINUTE, actual_departure, actual_arrival)) AS avg_by_route
    FROM flights
    WHERE actual_departure IS NOT NULL and actual_arrival IS NOT NULL
    GROUP BY departure_airport, arrival_airport
),
short_route_tops_by_city AS (
    SELECT city, arrival_airport, avg_by_route, ROW_NUMBER() OVER(PARTITION BY city ORDER BY avg_by_route) as rank
    FROM (SELECT city, arrival_airport, avg_by_route
            FROM route_avg
            INNER JOIN airports ON airport_code = departure_airport)
)
SELECT *
FROM short_route_tops_by_city
WHERE rank <= 1
ORDER BY avg_by_route;