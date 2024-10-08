package com.example.notifying_users.user.services;

import com.example.notifying_users.notifying.sevices.NotifyingQueries;
import com.example.notifying_users.user.entities.User;
import com.example.notifying_users.user.entities.DataForNotifyingEvent;
import com.example.notifying_users.user.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final String queryTextGettingUsersIdForNotifying;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, EntityManager entityManager, NotifyingQueries periodFilterQueries) {
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        this.queryTextGettingUsersIdForNotifying = periodFilterQueries.getQueryUsersByDate();
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User create(User user) {
        user.validate();
        user.nullIdFields();
        user.fillDependenceEntities();
        return userRepository.save(user);
    }

    @Override
    public <I> Optional<User> update(I id, User updatedUser) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty())
            return userOpt;
        User savedUser = userOpt.get();
        savedUser.update(updatedUser);
        savedUser.validate();
        return Optional.of(userRepository.save(savedUser));
    }

    @Override
    public <I> void delete(I id) {
        userRepository.deleteById(id);
    }

    @Override
    public DataForNotifyingEvent getNotifyingData(LocalDateTime date) {
        GettingUsersForNotifying gettingUsersForNotifying = new GettingUsersForNotifying();
        return gettingUsersForNotifying.getData(entityManager, userRepository, date);
    }

    @Override
    public <I> List<User> getUsersByIds(List<I> ids) {
        return userRepository.findByIdIn(ids);
    }

    @Override
    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    private class GettingUsersForNotifying {

        DataForNotifyingEvent getData(EntityManager entityManager, UserRepository userRepository, LocalDateTime date) {

            Query query = entityManager.createNativeQuery(queryTextGettingUsersIdForNotifying);
            query.setParameter("day_of_week", date.getDayOfWeek().getValue());
            query.setParameter("time", date.toLocalTime());

            List<Object[]> rows = (List<Object[]>) query.getResultList();

            ArrayList<Long> idsForNotifying = new ArrayList<>();
            ArrayList<Long> unnotifiedUserIds = new ArrayList<>();
            for (Object[] row: rows) {
                boolean notifying = (boolean) row[1];
                long id = (int) row[0];
                if (notifying) {
                    idsForNotifying.add(id);
                } else {
                    unnotifiedUserIds.add(id);
                }
            }

            List<User> users = userRepository.findByIdIn(idsForNotifying);
            return new DataForNotifyingEvent(users, unnotifiedUserIds);

        }

    }


}
