SELECT month_id, count(*) as cancelled_cnt
FROM (
    SELECT MONTH(scheduled_departure) AS month_id
    FROM flights
    WHERE status = 'Cancelled'
)
GROUP BY month_id
ORDER BY cancelled_cnt DESC;