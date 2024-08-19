package com.example.notifying_users.event.services;

import com.example.notifying_users.event.entities.DelayedEvent;
import com.example.notifying_users.event.entities.Event;
import com.example.notifying_users.event.repositories.DelayedEventRepository;
import com.example.notifying_users.event.repositories.EventRepository;
import com.example.notifying_users.notifying.sevices.NotifyingService;
import com.example.notifying_users.period.entities.Period;
import com.example.notifying_users.period.entities.TimePeriod;
import com.example.notifying_users.user.entities.User;
import com.example.notifying_users.user.entities.DataForNotifyingEvent;
import com.example.notifying_users.user.services.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final NotifyingService notifyingService;
    private final DelayedEventRepository delayedEventRepository;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, UserService userService, NotifyingService notifyingService,
                            DelayedEventRepository delayedEventRepository) {
        this.eventRepository = eventRepository;
        this.userService = userService;
        this.notifyingService = notifyingService;
        this.delayedEventRepository = delayedEventRepository;
    }

    @Transactional
    @Override
    public Event create(Event event) {
        Event savedEvent = eventRepository.save(event);
        DataForNotifyingEvent data = userService.getNotifyingData(savedEvent.getNotifyingDate());
        List<User> notifiesUsers = data.notifiedUsers();
        List<User> unnotifiedUsers = notifyingService.notify(savedEvent, notifiesUsers);
        List<Long> ids = unnotifiedUsers.stream().map(User::getId).toList();
        data.userIdsForDelayedNotifying().addAll(ids);

        createDelayedEvents(savedEvent, data);

        return savedEvent;
    }

    @Override
    public Optional<Event> update(Long id, Event updatedEvent) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isEmpty())
            return eventOpt;
        Event savedEvent = eventOpt.get();
        savedEvent.update(updatedEvent);
        return Optional.of(eventRepository.save(savedEvent));
    }

    @Override
    public List<Event> getAll() {
        return eventRepository.findAll();
    }

    @Override
    public Optional<Event> getById(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        eventRepository.deleteById(id);
    }

    @Override
    public List<Event> getEventsByIds(List<Long> ids) {
        return eventRepository.findAllById(ids);
    }

    @Override
    public Map<Long, LocalDateTime> getNotifyingDate(List<User> users, LocalDateTime date) {

        NotifyingDateGetting notifyingDateGetting = new NotifyingDateGetting();
        return notifyingDateGetting.get(users, date);

    }

    @Override
    public List<DelayedEvent> createEvents(List<User> users, Long eventId, LocalDateTime notifyingDate) {
        Map<?, LocalDateTime> userNotifyingDate = getNotifyingDate(users, notifyingDate);
        List<DelayedEvent> newEvents = new ArrayList<>();
        for (User user: users) {
            LocalDateTime newDate = userNotifyingDate.get(user.getId());
            if (newDate == null) {
                continue;
            }
            DelayedEvent delayedEvent = new DelayedEvent();
            delayedEvent.setEventId(eventId);
            delayedEvent.setNotifyingDate(newDate);
            delayedEvent.setUserId(user.getId());
            newEvents.add(delayedEvent);
        }
        return newEvents;
    }

    private void createDelayedEvents(Event savedEvent, DataForNotifyingEvent data) {

        List<Long> userIds = data.userIdsForDelayedNotifying().stream().distinct().toList();
        List<User> usersByIds = userService.getUsersByIds(userIds);

        List<DelayedEvent> newEvents = createEvents(usersByIds, savedEvent.getId(), savedEvent.getNotifyingDate());

        delayedEventRepository.saveAll(newEvents);

    }

    private static class NotifyingDateGetting {

        private LocalDateTime date;
        private DayOfWeek dayOfWeek;
        private LocalTime dateTime;
        private int minDays;

        public Map<Long, LocalDateTime> get(List<User> users, LocalDateTime date) {

            this.date = date;
            dayOfWeek = date.getDayOfWeek();
            dateTime = date.toLocalTime();
            Map<Long, LocalDateTime> map = new HashMap<>();

            for (User user: users) {
                LocalDateTime newDate = findClosestDay(user);
                map.put(user.getId(), newDate);
            }

            return map;

        }

        private LocalDateTime findClosestDay(User user) {

            if (user.getPeriods().isEmpty()) {
                return null;
            }

            minDays = 7;
            LocalTime minTime = LocalTime.MAX;
            for (Period period: user.getPeriods()) {
                int shift = getShift(period);
                minDays = Math.min(shift, minDays);

                // If we are in day that goes after current min day, we don't hove to compare time periods
                if (minDays < shift) {
                    continue;
                }

                LocalTime minTimePeriods = LocalTime.MAX;
                for(TimePeriod timePeriod: period.getTimePeriods()) {
                    LocalTime time = findClosestTime(timePeriod);
                    if (minTimePeriods.isAfter(time)) {
                        minTimePeriods = time;
                    }
                }

                if (minTime.isAfter(minTimePeriods)) {
                    minTime = minTimePeriods;
                }

            }

            LocalDate day = date.toLocalDate().plusDays(minDays);
            return LocalDateTime.of(day, minTime);

        }

        private int getShift(Period period) {
            DayOfWeek startDayOfWeek = period.getStartDay();
            DayOfWeek currentDayOfWeek = dayOfWeek;
            int shift = 0;
            boolean proceedCycle;

            do {
                proceedCycle = !currentDayOfWeek.equals(startDayOfWeek);
                if (!proceedCycle && shift == 0) {
                    boolean dateTimeBefore = true;
                    for (TimePeriod timePeriod : period.getTimePeriods()) {
                        boolean dateTimeBeforeInner = !dateTime.isAfter(timePeriod.getEndTime());
                        if (dateTimeBeforeInner) {
                            dateTimeBefore = false;
                            break;
                        }
                    }
                    proceedCycle = dateTimeBefore;
                }
                if (proceedCycle) {
                    shift++;
                    currentDayOfWeek = currentDayOfWeek.plus(1);
                }
            } while (proceedCycle);
            return shift;
        }

        private LocalTime findClosestTime(TimePeriod timePeriod) {

            LocalTime start = timePeriod.getStartTime();
            if (minDays != 0 || dateTime.isBefore(start)) {
                return start;
            }

            LocalTime end = timePeriod.getEndTime();
            if (dateTime.isBefore(end)) {
                return dateTime;
            }

            return LocalTime.MAX;

        }
    }

}

