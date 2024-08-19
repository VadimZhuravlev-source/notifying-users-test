package com.example.notifying_users.event.repositories;

import com.example.notifying_users.event.entities.DelayedEvent;
import org.springframework.data.repository.CrudRepository;

public interface DelayedEventRepository extends CrudRepository<DelayedEvent, Long> {
}
