package com.example.notifying_users.notifying.sevices;

import com.example.notifying_users.event.entities.Event;
import com.example.notifying_users.event.user.EventUser;
import com.example.notifying_users.user.entities.User;
import com.example.notifying_users.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
public class ScheduledTasks {

    private final NotifyingScheduler notifyingScheduler;
    private final UserService userService;

    @Autowired
    public ScheduledTasks(NotifyingScheduler notifyingScheduler, UserService userService) {
        this.notifyingScheduler = notifyingScheduler;
        this.userService = userService;
    }

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        List<Event> events = notifyingScheduler.getEvents();
        if (events.isEmpty()) {
            return;
        }

        List<Long> userIds = events.stream()
                .flatMap(event -> event.getUsers().stream())
                .map(EventUser::getUserId)
                .distinct().toList();

        List<User> users = userService.getUsersByIds(userIds);

    }

}
