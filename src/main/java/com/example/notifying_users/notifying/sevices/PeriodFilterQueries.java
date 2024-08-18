package com.example.notifying_users.notifying.sevices;

import org.springframework.stereotype.Component;

@Component
public class PeriodFilterQueries {

    private final String eventsByDate = getQueryTextEventsByDate();
    private final String usersByDate = getQueryTextGettingUsersIdForNotifying();

    public String getQueryEventsByDate() {
        return eventsByDate;
    }

    public String getQueryUsersByDate() {
        return usersByDate;
    }

    private String getQueryTextEventsByDate() {
        return """
                WITH event_filter AS (
                	SELECT
                		id
                	FROM
                		events
                	WHERE
                		:date >= notifying_date
                		AND NOT notified
                		AND notifying_date BETWEEN :date_week_ago AND :date --Чтобы получать события только в течении недели
                ),
                
                user_filter AS (
                	SELECT DISTINCT
                		event_users.id
                	FROM
                		event_users
                	JOIN event_filter
                		ON event_users.event_id = event_filter.id
                ),
                
                user_id_filter_suiting_by_period AS (
                
                	SELECT DISTINCT
                		user_id user_id,
                		periods.id period_id
                	FROM
                		periods
                	JOIN user_filter
                		ON periods.user_id = user_filter.id
                	WHERE
                		:day_of_week BETWEEN start_day AND end_day
                
                	UNION
                
                	SELECT
                		user_id,
                		periods.id
                	FROM
                		periods
                	JOIN user_filter
                		ON periods.user_id = user_filter.id
                	WHERE
                		start_day > end_day
                		AND :day_of_week >= start_day
                
                	UNION
                
                	SELECT
                		user_id,
                		periods.id
                	FROM
                		periods
                	JOIN user_filter
                		ON periods.user_id = user_filter.id
                	WHERE
                		start_day > end_day
                		AND :day_of_week <= end_day
                
                ),
                
                filter_by_time AS (
                    SELECT DISTINCT
                        per.user_id id
                    FROM user_id_filter_suiting_by_period per
                    JOIN time_periods
                        ON time_periods.period_id = per.period_id
                    WHERE
                		:time >= time_periods.start_time
                		AND :time <= time_periods.end_time
                ),
                
                final_filter AS (
                	SELECT DISTINCT
                		event_users.event_id
                	FROM
                		event_users
                	JOIN filter_by_time
                		ON event_users.user_id = filter_by_time.id
                )
                
                SELECT
                	events.*
                FROM
                	events
                JOIN final_filter
                	ON events.id = final_filter.event_id
                
                """;
    }

    private String getQueryTextGettingUsersIdForNotifying() {
        return """
                WITH user_id_filter_suiting_by_period AS (
                
                	SELECT DISTINCT
                		user_id user_id,
                		period.id period_id
                	FROM
                		periods
                	WHERE
                		:day_of_week BETWEEN start_day AND end_day
                
                	UNION
                
                	SELECT
                		user_id,
                		period.id
                	FROM
                		periods
                	WHERE
                		start_day > end_day
                		AND :day_of_week >= start_day
                
                	UNION
                
                	SELECT
                		user_id,
                		period.id
                	FROM
                		periods
                	WHERE
                		start_day > end_day
                		AND :day_of_week <= end_day
                
                ),
                
                filter_by_time AS (
                    SELECT DISTINCT
                        per.user_id id
                    FROM user_id_filter_suiting_by_period per
                    JOIN time_periods
                        ON time_periods.period_id = per.period_id
                    WHERE
                		:time >= time_periods.start_time
                		AND :time <= time_periods.end_time
                ),
                
                another_user_id AS (
                	SELECT
                		users.id
                	FROM
                		users
                	LEFT JOIN filter_by_time user_filter
                		ON users.id = user_filter.id
                	WHERE
                		user_filter.id IS NULL
                )
                
                SELECT
                	id,
                	true notify
                FROM
                	user_id_filter_suiting_by_period
                
                UNION
                
                SELECT
                	id,
                	false
                FROM
                	another_user_id
                """;
    }

}
