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
                		AND !notified
                		AND DATEDIFF(day, :date, notifying_date) <= 7 --Чтобы получать события только в течении недели
                ),
                
                WITH user_filter AS (
                	SELECT DISTINCT
                		event_users.user_id
                	FROM
                		event_users
                	JOIN event_filter
                		ON event_users.event_id = event_filter.id
                ),
                
                WITH user_id_filter_suiting_by_period AS (
                
                	SELECT DISTINCT
                		user_id id
                	FROM
                		periods
                	JOIN user_filter
                		periods.user_id = user_filter.user_id
                	WHERE
                		:day_of_week BETWEEN start_day AND end_day
                		AND :time >= start_time
                		AND :time <= end_time
                
                	UNION
                
                	SELECT
                		user_id
                	FROM
                		periods
                	JOIN user_filter
                		periods.user_id = user_filter.user_id
                	WHERE
                		start_day > end_day
                		AND :day_of_week >= start_day
                		AND :time >= start_time
                		AND :time <= end_time
                
                	UNION
                
                	SELECT
                		user_id
                	FROM
                		periods
                	JOIN user_filter
                		periods.user_id = user_filter.user_id
                	WHERE
                		start_day > end_day
                		AND :day_of_week <= end_day
                		AND :time >= start_time
                		AND :time <= end_time
                
                ),
                
                WITH final_filter AS (
                	SELECT DISTINCT
                		event_users.event_id
                	FROM
                		event_users
                	JOIN user_id_filter_suiting_by_period
                		ON event_users.user_id = user_id_filter_suiting_by_period.id
                ),
                
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
                		user_id id
                	FROM
                		periods
                	WHERE
                		:day_of_week BETWEEN start_day AND end_day
                		AND :time >= start_time
                		AND :time <= end_time
                
                	UNION
                
                	SELECT
                		user_id
                	FROM
                		periods
                	WHERE
                		start_day > end_day
                		AND :day_of_week >= start_day
                		AND :time >= start_time
                		AND :time <= end_time
                
                	UNION
                
                	SELECT
                		user_id
                	FROM
                		periods
                	WHERE
                		start_day > end_day
                		AND :day_of_week <= end_day
                		AND :time >= start_time
                		AND :time <= end_time
                
                ),
                
                another_user_id AS (
                	SELECT
                		id
                	FROM
                		users
                	LEFT JOIN user_id_filter_suiting_by_period user_filter
                		ON users.id = user_filter.id
                	WHERE
                		user_filter.id IS NULL
                )
                
                SELECT
                	id,
                	notifying true
                FROM
                	user_id_filter_suiting_by_period
                
                UNION
                
                SELECT
                	id,
                	notifying false
                FROM
                	another_user_id
                """;
    }

}
