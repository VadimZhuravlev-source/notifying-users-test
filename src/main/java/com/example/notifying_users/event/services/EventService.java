package com.example.notifying_users.event.services;

import com.example.notifying_users.event.entities.DelayedEvent;
import com.example.notifying_users.event.entities.Event;
import com.example.notifying_users.user.entities.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EventService {

    Event create(Event event);
    Optional<Event> update(Long id, Event updatedEvent);
    List<Event> getAll();
    Optional<Event> getById(Long id);
    void delete(Long id);
    List<Event> getEventsByIds(List<Long> ids);
    Map<Long, LocalDateTime> getNotifyingDate(List<User> users, LocalDateTime date);
    List<DelayedEvent> createEvents(List<User> users, Long eventId, LocalDateTime notifyingDate);

}
