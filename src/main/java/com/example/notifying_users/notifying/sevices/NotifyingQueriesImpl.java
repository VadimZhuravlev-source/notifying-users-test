package com.example.notifying_users.notifying.sevices;

import org.springframework.stereotype.Component;

@Component
public class NotifyingQueriesImpl implements NotifyingQueries {

    private final String eventsByDate = getQueryTextEventsByDate();
    private final String usersByDate = getQueryTextGettingUsersIdForNotifying();

    @Override
    public String getQueryEventsByDate() {
        return eventsByDate;
    }

    @Override
    public String getQueryUsersByDate() {
        return usersByDate;
    }

    private String getQueryTextEventsByDate() {
        return """
               
                SELECT
                    *
                FROM
                    delayed_events
                WHERE
                    NOT notified
                    AND notifying_date BETWEEN :date_week_ago AND :date --Чтобы получать события только в течении недели
                
                """;
    }

    private String getQueryTextGettingUsersIdForNotifying() {
        return """
                WITH user_id_filter_suiting_by_period AS (
                
                	SELECT DISTINCT
                		user_id user_id,
                		periods.id period_id
                	FROM
                		periods
                	WHERE
                		:day_of_week BETWEEN start_day AND end_day
                
                	UNION
                
                	SELECT
                		user_id,
                		periods.id
                	FROM
                		periods
                	WHERE
                		start_day > end_day
                		AND :day_of_week >= start_day
                
                	UNION
                
                	SELECT
                		user_id,
                		periods.id
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
                	filter_by_time
                
                UNION
                
                SELECT
                	id,
                	false
                FROM
                	another_user_id
                """;
    }

}
