package com.example.notifying_users.notifying.sevices;

import com.example.notifying_users.event.entities.Event;
import com.example.notifying_users.event.services.EventService;
import com.example.notifying_users.period.entities.Period;
import com.example.notifying_users.period.entities.TimePeriod;
import com.example.notifying_users.user.entities.User;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class NotifyingService {

    private final Logger logger = Logger.getLogger(EventService.class.getName());

    public List<User> notify(Event event, List<User> users) {
        return notifyUsers(event, users);
    }

    private List<User> notifyUsers(Event event, List<User> users) {
        List<User> requiredDelayForNotifyingUsers = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (User user : users) {
            boolean notified = false;
            for (Period period : user.getPeriods()) {
                DayOfWeek eventDay = event.getNotifyingDate().getDayOfWeek();
                LocalTime eventTime = event.getNotifyingDate().toLocalTime();

                if (isEventInPeriod(eventDay, eventTime, period)) {
                    logger.info(getMessage(now, user, event));
                    notified = true;
                }
            }
            if (!notified) {
                requiredDelayForNotifyingUsers.add(user);
            }
        }
        return requiredDelayForNotifyingUsers;
    }

    private boolean isEventInPeriod(DayOfWeek eventDay, LocalTime eventTime, Period period) {

        DayOfWeek startDay = period.getStart();
        DayOfWeek endDay = period.getEnd();

        if (eventDay.compareTo(startDay) >= 0 && eventDay.compareTo(endDay) <= 0) {
            return compareTime(eventTime, period);
        } else if (startDay.compareTo(endDay) > 0
                && (eventDay.compareTo(startDay) >= 0 || eventDay.compareTo(endDay) <= 0)) {
            return compareTime(eventTime, period);
        }
        return false;
    }

    private boolean compareTime(LocalTime eventTime, Period period) {
        for (TimePeriod timePeriod: period.getTimePeriods()) {
            if (eventTime.compareTo(timePeriod.getStart()) >= 0
                    && eventTime.compareTo(timePeriod.getEnd()) <= 0) {
                return true;
            }
        }
        return false;
    }

    private String getMessage(LocalDateTime date, User user, Event event) {
        return date + " Пользователю " + user.getFullName() + " отправлено оповещение с текстом: " +
                event.getMessage();
    }

}
