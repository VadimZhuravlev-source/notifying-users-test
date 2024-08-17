package com.example.notifying_users.event.services;

import com.example.notifying_users.event.entities.Event;
import com.example.notifying_users.event.repositories.EventRepository;
import com.example.notifying_users.event.user.EventUser;
import com.example.notifying_users.notifying.sevices.NotifyingService;
import com.example.notifying_users.period.entities.Period;
import com.example.notifying_users.period.entities.TimePeriod;
import com.example.notifying_users.user.entities.User;
import com.example.notifying_users.user.entities.UsersForNotifyingEvent;
import com.example.notifying_users.user.services.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        Event newEvent = createClosestToUserPeriodsEvent(savedEvent, data.userIdsForDelayedNotifying());
        if (newEvent != null) {
            List<User> usersByIds = userService.getUsersByIds(newEvent.getUsers().stream().map(EventUser::getUserId).toList());
            Map<?, User> idUsersMap = usersByIds.stream().collect(Collectors.toMap(User::getId, Function.identity()));
            LocalDateTime newDate = getNotifyingDate(newEvent, LocalDateTime.now(), idUsersMap);
            newEvent.setNotifyingDate(newDate);
            eventRepository.save(newEvent);
        }
        savedEvent.setNotified(true);
        return savedEvent;
    }

    public Event createNewEventForUsers(Event event, List<Long> userIds) {
        return createClosestToUserPeriodsEvent(event, userIds);
    }

    public Optional<Event> update(Long id, Event updatedEvent) {
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

    public void delete(Long id) {
        eventRepository.deleteById(id);
    }

    public List<Event> getEventsByIds(List<Long> ids) {
        return eventRepository.findAllById(ids);
    }

    public void saveAll(List<Event> events) {
        eventRepository.saveAll(events);
    }

    public LocalDateTime getNotifyingDate(Event event, LocalDateTime date, Map<?, User> idUserMap) {

        NotifyingDateGetting notifyingDateGetting = new NotifyingDateGetting();

        return notifyingDateGetting.get(event, date, idUserMap);

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
        return newEvent;
    }

    private static class NotifyingDateGetting {

        private DayOfWeek dayOfWeek;
        private LocalTime dateTime;
        private LocalTime closestTime;
        private int minDays;
        private int shift;

        public LocalDateTime get(Event event, LocalDateTime date, Map<?, User> idUserMap) {

            List<EventUser> eventUsers = event.getUsers();
            if (eventUsers == null || eventUsers.isEmpty()) {
                return date;
            }

            dayOfWeek = date.getDayOfWeek();
            dateTime = date.toLocalTime();
            closestTime = LocalTime.MAX;
            minDays = 7;

            for (EventUser eventUser: eventUsers) {
                User user = idUserMap.get(eventUser.getUserId());
                if (user == null || user.getPeriods() == null || user.getPeriods().isEmpty()) {
                    continue;
                }
                findClosestDay(user);
            }

            LocalDate day = date.toLocalDate();
            day = day.plusDays(minDays);

            return LocalDateTime.of(day, closestTime);

        }

        private void findClosestDay(User user) {

            for (Period period: user.getPeriods()) {
                DayOfWeek startDayOfWeek = period.getStart();
                shift = 0;
                while (!startDayOfWeek.equals(dayOfWeek)) {
                    shift++;
                    startDayOfWeek = startDayOfWeek.plus(1);
                }

                if (shift == 0) {
                    boolean dateTimeIsBefore = false;
                    for(TimePeriod timePeriod: period.getTimePeriods()) {
                        dateTimeIsBefore = dateTimeIsBefore || dateTime.compareTo(timePeriod.getEnd()) <= 0;
                    }
                    if (!dateTimeIsBefore) {
                        shift = 7;
                    }
                }

                for(TimePeriod timePeriod: period.getTimePeriods()) {
                    findClosestTime(timePeriod);
                }
            }
        }

        private void findClosestTime(TimePeriod period) {
            if (shift == 0) {
                if (minDays == 0) {
                    if (closestTime.isAfter(period.getStart())) {
                        if (dateTime.isBefore(period.getStart())) {
                            closestTime = period.getStart();
                        } else if (dateTime.isBefore(closestTime)) {
                            closestTime = dateTime;
                        }
                    }
                } else {
                    minDays = shift;
                    if (dateTime.isAfter(period.getStart())) {
                        closestTime = dateTime;
                    } else {
                        closestTime = period.getStart();
                    }
                }
            } else {
                if (minDays > shift) {
                    minDays = shift;
                    closestTime = period.getStart();
                } else if (minDays == shift && closestTime.isAfter(period.getStart())) {
                    closestTime = period.getStart();
                }
            }
        }
    }

}

