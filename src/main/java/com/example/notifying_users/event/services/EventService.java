package com.example.notifying_users.event.services;

import com.example.notifying_users.event.entities.Event;
import com.example.notifying_users.event.repositories.EventRepository;
import com.example.notifying_users.event.user.EventUser;
import com.example.notifying_users.notifying.sevices.NotifyingService;
import com.example.notifying_users.user.entities.User;
import com.example.notifying_users.user.entities.UsersForNotifyingEvent;
import com.example.notifying_users.user.services.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final NotifyingService notifyingService;

    @Autowired
    public EventService(EventRepository eventRepository, UserService userService, NotifyingService notifyingService) {
        this.eventRepository = eventRepository;
        this.userService = userService;
        this.notifyingService = notifyingService;
    }

    @Transactional
    public Event create(Event event) {
        Event savedEvent = eventRepository.save(event);
        UsersForNotifyingEvent data = userService.getNotifyingData(savedEvent.getNotifyingDate());
        List<User> notifiesUsers = data.notifiedUsers();
        List<User> unnotifiedUsers = notifyingService.notify(savedEvent, notifiesUsers);
        List<Long> ids = unnotifiedUsers.stream().map(User::getId).toList();
        data.userIdsForDelayedNotifying().addAll(ids);
        createClosestToUserPeriodsEvent(savedEvent, data.userIdsForDelayedNotifying());
        savedEvent.setNotified(true);
        return savedEvent;
    }

    public Event createNewEventForUsers(Event event, List<Long> userIds) {
        return createClosestToUserPeriodsEvent(event, userIds);
    }

    public <I> Optional<Event> update(I id, Event updatedEvent) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty())
            return eventOpt;
        Event savedEvent = eventOpt.get();
        savedEvent.update(updatedEvent);
        return Optional.of(eventRepository.save(savedEvent));
    }

    public List<Event> getAll() {
        return eventRepository.findAll();
    }

    public <I> void delete(I id) {
        eventRepository.deleteById(id);
    }

    private Event createClosestToUserPeriodsEvent(Event parentEvent, List<Long> userIds) {
        if (userIds.isEmpty())
            return null;

        Event newEvent = new Event();
        Long parentId = parentEvent.getParentId();
        if (parentId == null) {
            parentId = parentEvent.getId();
        }
        newEvent.setParentId(parentId);
//        newEvent.setMessage(parentEvent.getMessage());
        List<EventUser> users = userIds.stream().filter(Objects::nonNull).distinct()
                .map(id -> new EventUser(newEvent, id)).toList();

        newEvent.setUsers(users);
        return eventRepository.save(newEvent);
    }

}

