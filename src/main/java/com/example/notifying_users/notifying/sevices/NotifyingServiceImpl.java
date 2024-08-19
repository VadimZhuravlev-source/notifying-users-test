package com.example.notifying_users.notifying.sevices;

import com.example.notifying_users.event.entities.Event;
import com.example.notifying_users.period.entities.Period;
import com.example.notifying_users.period.entities.TimePeriod;
import com.example.notifying_users.user.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class NotifyingServiceImpl implements NotifyingService {

    @Override
    public List<User> notify(Event event, List<User> users) {
        return notifyUsers(event.getNotifyingDate(), event.getMessage(), users);
    }

    @Override
    public List<User> notify(LocalDateTime notifyingDate, String eventMessage, List<User> users) {
        return notifyUsers(notifyingDate, eventMessage, users);
    }

    private List<User> notifyUsers(LocalDateTime notifyingDate, String eventMessage, List<User> users) {
        List<User> requiredDelayForNotifyingUsers = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (User user : users) {
            boolean notified = false;
            for (Period period : user.getPeriods()) {
                DayOfWeek eventDay = notifyingDate.getDayOfWeek();
                LocalTime eventTime = notifyingDate.toLocalTime();

                if (isEventInPeriod(eventDay, eventTime, period)) {
                    log.info(getMessage(now, user, eventMessage));
                    notified = true;
                    break;
                }
            }
            if (!notified) {
                requiredDelayForNotifyingUsers.add(user);
            }
        }
        return requiredDelayForNotifyingUsers;
    }

    private boolean isEventInPeriod(DayOfWeek eventDay, LocalTime eventTime, Period period) {

        DayOfWeek startDay = period.getStartDay();
        DayOfWeek endDay = period.getEndDay();

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
            if (eventTime.compareTo(timePeriod.getStartTime()) >= 0
                    && eventTime.compareTo(timePeriod.getEndTime()) <= 0) {
                return true;
            }
        }
        return false;
    }

    private String getMessage(LocalDateTime date, User user, String eventMessage) {
        return date + " Пользователю " + user.getFullName() + " отправлено оповещение с текстом: " +
                eventMessage;
    }

}
