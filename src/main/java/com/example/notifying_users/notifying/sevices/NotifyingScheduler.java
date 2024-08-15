package com.example.notifying_users.notifying.sevices;

import com.example.notifying_users.event.entities.Event;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotifyingScheduler {

    private final EntityManager entityManager;
    private final String queryText = getQueryText();

    @Autowired
    public NotifyingScheduler(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Event> getEvents() {

        LocalDateTime now = LocalDateTime.now();
        Query query = entityManager.createNativeQuery(queryText, Event.class);
        query.setParameter("date", now);
        query.setParameter("day_of_week", now.getDayOfWeek());
        query.setParameter("time", now.toLocalTime());

        return query.getResultList();

    }

    private String getQueryText() {
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

}
