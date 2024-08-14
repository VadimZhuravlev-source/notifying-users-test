package com.example.notifying_users.event.repositories;

import com.example.notifying_users.event.entities.Event;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends Repository<Event, Long> {
    Event save(Event event);
    List<Event> findAll();
    <I> void deleteById(I id);
    <I> Optional<Event> findById(I id);
}
