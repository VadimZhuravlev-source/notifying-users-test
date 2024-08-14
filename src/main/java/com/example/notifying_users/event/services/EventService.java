package com.example.notifying_users.event.services;

import com.example.notifying_users.event.entities.Event;
import com.example.notifying_users.event.repositories.EventRepository;
import com.example.notifying_users.notifying.sevices.NotifyingService;
import com.example.notifying_users.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotifyingService notifyingService;

    @Autowired
    public EventService(EventRepository eventRepository, UserRepository userRepository, NotifyingService notifyingService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.notifyingService = notifyingService;
    }

    public Event createEvent(Event event) {
        Event savedEvent = eventRepository.save(event);
        notifyingService.notify(savedEvent, userRepository.findAll());
        return savedEvent;
    }

    public <I> Optional<Event> updateEvent(I id, Event updatedEvent) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty())
            return eventOpt;
        Event savedEvent = eventOpt.get();
        savedEvent.setMessage(updatedEvent.getMessage());
        savedEvent.setNotifyingDate(updatedEvent.getNotifyingDate());
        return Optional.of(eventRepository.save(savedEvent));
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public <I> void deleteEvent(I id) {
        eventRepository.deleteById(id);
    }

}

