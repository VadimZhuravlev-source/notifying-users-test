package com.example.notifying_users.event.repositories;

import com.example.notifying_users.event.entities.Event;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRepository extends CrudRepository<Event, Long> {
    List<Event> findAll();
    List<Event> findAllById(Iterable<Long> ids);
}
