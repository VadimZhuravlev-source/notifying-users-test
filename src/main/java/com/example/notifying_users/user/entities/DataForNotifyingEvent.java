package com.example.notifying_users.user.entities;

import java.util.List;

public record DataForNotifyingEvent(List<User> notifiedUsers, List<Long> userIdsForDelayedNotifying) {
}
