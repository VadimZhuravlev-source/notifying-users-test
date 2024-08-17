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
    private final String queryText;

    @Autowired
    public NotifyingScheduler(EntityManager entityManager, PeriodFilterQueries periodFilterQueries) {
        this.entityManager = entityManager;
        this.queryText = periodFilterQueries.getQueryEventsByDate();
    }

    public List<Event> getEvents(LocalDateTime now) {

        Query query = entityManager.createNativeQuery(queryText, Event.class);
        query.setParameter("date", now);
        query.setParameter("day_of_week", now.getDayOfWeek());
        query.setParameter("time", now.toLocalTime());

        return query.getResultList();

    }

}
