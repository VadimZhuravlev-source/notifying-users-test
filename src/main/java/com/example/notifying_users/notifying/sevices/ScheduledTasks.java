package com.example.notifying_users.notifying.sevices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private final NotifyingScheduler notifyingScheduler;

    @Autowired
    public ScheduledTasks(NotifyingScheduler notifyingScheduler) {
        this.notifyingScheduler = notifyingScheduler;
    }

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {

    }

}
