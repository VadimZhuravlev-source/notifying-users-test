package com.example.notifying_users.notifying.sevices;

import com.example.notifying_users.event.entities.DelayedEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface NotifyingEventService {
    List<DelayedEvent> getEvents(LocalDateTime now);
}
