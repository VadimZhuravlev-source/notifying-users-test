package com.example.notifying_users.notifying.sevices;

import com.example.notifying_users.event.entities.DelayedEvent;
import com.example.notifying_users.event.entities.Event;
import com.example.notifying_users.event.repositories.DelayedEventRepository;
import com.example.notifying_users.event.services.EventService;
import com.example.notifying_users.user.entities.User;
import com.example.notifying_users.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ScheduledTasks {

    private final NotifyingScheduler notifyingScheduler;
    private final UserService userService;
    private final NotifyingService notifyingService;
    private final EventService eventService;
    private final DelayedEventRepository delayedEventRepository;

    @Autowired
    public ScheduledTasks(NotifyingScheduler notifyingScheduler, UserService userService,
                          NotifyingService notifyingService, EventService eventService,
                          DelayedEventRepository delayedEventRepository) {
        this.notifyingScheduler = notifyingScheduler;
        this.userService = userService;
        this.notifyingService = notifyingService;
        this.eventService = eventService;
        this.delayedEventRepository = delayedEventRepository;
    }

    @Scheduled(fixedRate = 5000)
    public void sendNotifications() {

        LocalDateTime now = LocalDateTime.now();
        List<DelayedEvent> events = notifyingScheduler.getEvents(now);
        if (events.isEmpty()) {
            return;
        }

        List<Long> userIds = events.stream().map(DelayedEvent::getUserId).toList();

        Map<?, User> userMap = userService.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<Long> parentEventIds = events.stream().map(DelayedEvent::getEventId).filter(Objects::nonNull).distinct().toList();
        Map<?, Event> eventMap = eventService.getEventsByIds(parentEventIds).stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        List<DelayedEvent> newEvents = new ArrayList<>();
        List<User> tempUserList = new ArrayList<>();
        for (DelayedEvent event: events) {

            User user = userMap.get(event.getUserId());
            if (user == null) {
                continue;
            }
            tempUserList.add(user);
            String message = Optional.of(eventMap.get(event.getEventId())).map(Event::getMessage).orElse("");

            List<User> unnotifiedUsers = notifyingService.notify(event.getNotifyingDate(), message, tempUserList);

            if (!unnotifiedUsers.isEmpty()) {
                List<DelayedEvent> newDelayedEvents =
                        eventService.createEvents(unnotifiedUsers, event.getEventId(), event.getNotifyingDate());
                newEvents.addAll(newDelayedEvents);
            }

            event.setNotified(true);
            newEvents.add(event);
            tempUserList.clear();

        }

        delayedEventRepository.saveAll(newEvents);

    }

}
