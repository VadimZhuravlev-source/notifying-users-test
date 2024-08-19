package com.example.notifying_users.user.services;

import com.example.notifying_users.user.entities.User;
import com.example.notifying_users.user.entities.DataForNotifyingEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAll();
    User create(User user);
    <I> Optional<User> update(I id, User updatedUser);
    <I> void delete(I id);
    DataForNotifyingEvent getNotifyingData(LocalDateTime date);
    <I> List<User> getUsersByIds(List<I> ids);
    Optional<User> getById(Long id);
}
