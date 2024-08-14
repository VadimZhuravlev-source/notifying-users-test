package com.example.notifying_users.user.services;

import com.example.notifying_users.user.entities.User;
import com.example.notifying_users.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User create(User user) {
        return userRepository.save(user);
    }

    public <I> Optional<User> update(I id, User updatedUser) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty())
            return userOpt;
        User savedUser = userOpt.get();
        savedUser.setLastName(updatedUser.getLastName());
        savedUser.setFirstName(updatedUser.getFirstName());
        savedUser.setPatronymic(updatedUser.getPatronymic());
        savedUser.setPeriods(updatedUser.getPeriods());
        return Optional.of(userRepository.save(savedUser));
    }

    public <I> void delete(I id) {
        userRepository.deleteById(id);
    }

}
