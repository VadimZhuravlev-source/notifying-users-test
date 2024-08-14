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
    public Event createEvent(Event event) {
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

    public <I> Optional<Event> updateEvent(I id, Event updatedEvent) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty())
            return eventOpt;
        Event savedEvent = eventOpt.get();
        savedEvent.update(updatedEvent);
        return Optional.of(eventRepository.save(savedEvent));
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public <I> void deleteEvent(I id) {
        eventRepository.deleteById(id);
    }

    private void createClosestToUserPeriodsEvent(Event parentEvent, List<Long> userIds) {
        if (userIds.isEmpty())
            return;

        Event newEvent = new Event();
        newEvent.setParentId(parentEvent.getId());
        newEvent.setMessage(parentEvent.getMessage());
        List<EventUser> users = userIds.stream().filter(Objects::nonNull).distinct()
                .map(id -> new EventUser(newEvent, id)).toList();

        newEvent.setUsers(users);
        eventRepository.save(newEvent);

    }

}

