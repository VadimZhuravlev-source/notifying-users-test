package com.example.notifying_users.event.repositories;

import com.example.notifying_users.event.entities.Event;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends Repository<Event, Long> {
    Event save(Event event);
    List<Event> findAll();
    void deleteById(Long id);
    Optional<Event> findById(Long id);
    List<Event> findByIdIn(Collection<Long> ids);
    List<Event> saveAll(List<Event> events);
}
