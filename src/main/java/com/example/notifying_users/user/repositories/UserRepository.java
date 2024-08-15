package com.example.notifying_users.user.repositories;

import com.example.notifying_users.user.entities.User;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {
    List<User> findAll();
    User save(User user);
    <I> void deleteById(I id);
    <I> Optional<User> findById(I id);
    <I> List<User> findByIdIn(Collection<I> ids);
}
