package com.example.notifying_users.notifying.sevices;

import com.example.notifying_users.event.entities.Event;
import com.example.notifying_users.user.entities.User;

import java.time.LocalDateTime;
import java.util.List;

public interface NotifyingService {
    List<User> notify(Event event, List<User> users);
    List<User> notify(LocalDateTime notifyingDate, String eventMessage, List<User> users);
}
