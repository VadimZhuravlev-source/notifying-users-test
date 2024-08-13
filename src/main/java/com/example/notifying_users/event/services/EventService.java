package com.example.notifying_users.event.services;

import com.example.notifying_users.event.entities.Event;
import com.example.notifying_users.event.repositories.EventRepository;
import com.example.notifying_users.period.entities.Period;
import com.example.notifying_users.user.entities.User;
import com.example.notifying_users.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final Logger logger = Logger.getLogger(EventService.class.getName());

    @Autowired
    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public Event createEvent(Event event) {
        Event savedEvent = eventRepository.save(event);
        notifyUsers(savedEvent);
        return savedEvent;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public <I> void deleteEvent(I id) {
        eventRepository.deleteById(id);
    }

    private void notifyUsers(Event event) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            for (Period period : user.getPeriods()) {
                DayOfWeek eventDay = event.getCreatedAt().getDayOfWeek();
                LocalTime eventTime = event.getCreatedAt().toLocalTime();

                if (isEventInPeriod(eventDay, eventTime, period)) {
                    logger.info("User " + user.getFullName() + ": " + event.getMessage());
                }
            }
        }
    }

    private boolean isEventInPeriod(DayOfWeek eventDay, LocalTime eventTime, Period period) {
        DayOfWeek startDay = period.getStartDay();
        DayOfWeek endDay = period.getEndDay();

        if (startDay.compareTo(eventDay) <= 0 && endDay.compareTo(eventDay) >= 0) {
            return eventTime.compareTo(period.getStartTime()) >= 0 && eventTime.compareTo(period.getEndTime()) <= 0;
        }
        return false;
    }
}

