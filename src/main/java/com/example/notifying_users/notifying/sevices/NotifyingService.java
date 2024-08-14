package com.example.notifying_users.notifying.sevices;

import com.example.notifying_users.event.entities.Event;
import com.example.notifying_users.event.services.EventService;
import com.example.notifying_users.period.entities.Period;
import com.example.notifying_users.user.entities.User;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class NotifyingService {

    private final Logger logger = Logger.getLogger(EventService.class.getName());

    public void notify(Event event, List<User> users) {
        notifyUsers(event, users);
    }

    private void notifyUsers(Event event, List<User> users) {
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
