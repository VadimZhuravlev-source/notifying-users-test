package com.example.notifying_users.notifying.sevices;

import com.example.notifying_users.event.entities.Event;
import com.example.notifying_users.event.services.EventService;
import com.example.notifying_users.event.user.EventUser;
import com.example.notifying_users.user.entities.User;
import com.example.notifying_users.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ScheduledTasks {

    private final NotifyingScheduler notifyingScheduler;
    private final UserService userService;
    private final NotifyingService notifyingService;
    private final EventService eventService;

    @Autowired
    public ScheduledTasks(NotifyingScheduler notifyingScheduler, UserService userService,
                          NotifyingService notifyingService, EventService eventService) {
        this.notifyingScheduler = notifyingScheduler;
        this.userService = userService;
        this.notifyingService = notifyingService;
        this.eventService = eventService;
    }

    @Scheduled(fixedRate = 5000)
    public void sendNotifications() {

        List<Event> events = notifyingScheduler.getEvents();
        if (events.isEmpty()) {
            return;
        }

        List<Long> userIds = events.stream()
                .flatMap(event -> event.getUsers().stream())
                .map(EventUser::getUserId)
                .distinct().toList();

        Map<?, User> userMap = userService.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<Long> parentEventIds = events.stream().map(Event::getParentId).filter(Objects::nonNull).distinct().toList();
        Map<?, Event> eventMap = eventService.getEventsByIds(parentEventIds).stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        List<Event> newEvents = new ArrayList<>();
        List<User> tempList = new ArrayList<>();
        for (Event event: events) {
            for (EventUser eventUser: event.getUsers()) {
                User user = userMap.get(eventUser.getUserId());
                if (user != null) {
                    tempList.add(user);
                }
            }

            if (event.getMessage() == null && event.getParentId() != null) {
                Event parentEvent = eventMap.get(event.getParentId());
                if (parentEvent != null) {
                    event.setMessage(parentEvent.getMessage());
                }
            }

            List<User> unnotifiedUsers = notifyingService.notify(event, tempList);

            if (!unnotifiedUsers.isEmpty()) {
                List<Long> unnotifiedUserIds = unnotifiedUsers.stream().map(User::getId).toList();
                Event newEvent = eventService.createNewEventForUsers(event, unnotifiedUserIds);
                newEvents.add(newEvent);
            }

            tempList.clear();

        }

        eventService.saveAll(newEvents);

    }

}
