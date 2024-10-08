package com.example.notifying_users.notifying.sevices;

import com.example.notifying_users.event.entities.DelayedEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotifyingEventServiceImpl implements NotifyingEventService {

    private final EntityManager entityManager;
    private final String queryText;

    @Autowired
    public NotifyingEventServiceImpl(EntityManager entityManager, NotifyingQueries periodFilterQueries) {
        this.entityManager = entityManager;
        this.queryText = periodFilterQueries.getQueryEventsByDate();
    }

    @Override
    public List<DelayedEvent> getEvents(LocalDateTime now) {

        Query query = entityManager.createNativeQuery(queryText, DelayedEvent.class);
        query.setParameter("date", now);
        query.setParameter("date_week_ago", now.minusWeeks(1));

        return query.getResultList();

    }

}
