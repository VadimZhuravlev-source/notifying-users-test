package com.example.notifying_users.user.services;

import com.example.notifying_users.user.entities.User;
import com.example.notifying_users.user.entities.UsersForNotifyingEvent;
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
public class UserService {

    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Autowired
    public UserService(UserRepository userRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.entityManager = entityManager;
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
        savedUser.update(updatedUser);
        return Optional.of(userRepository.save(savedUser));
    }

    public <I> void delete(I id) {
        userRepository.deleteById(id);
    }

    public UsersForNotifyingEvent getNotifyingData(LocalDateTime date) {
        GettingUsersForNotifying gettingUsersForNotifying = new GettingUsersForNotifying();
        return gettingUsersForNotifying.getData(entityManager, userRepository, date);
    }

    private static class GettingUsersForNotifying {

        private final String queryText = getQueryTextGettingUsersIdForNotifying();

        UsersForNotifyingEvent getData(EntityManager entityManager, UserRepository userRepository, LocalDateTime date) {

            Query query = entityManager.createQuery(queryText);
            query.setParameter("day_of_week", date.getDayOfWeek());
            query.setParameter("time", date.toLocalTime());

            List<Object[]> rows = (List<Object[]>) query.getResultList();

            ArrayList<Long> idsForNotifying = new ArrayList<>();
            ArrayList<Long> unnotifiedUserIds = new ArrayList<>();
            for (Object[] row: rows) {
                boolean notifying = (boolean) row[1];
                long id = (long) row[0];
                if (notifying) {
                    idsForNotifying.add(id);
                } else {
                    unnotifiedUserIds.add(id);
                }
            }

            List<User> users = userRepository.findByIdIn(idsForNotifying);
            return new UsersForNotifyingEvent(users, unnotifiedUserIds);

        }

        private String getQueryTextGettingUsersIdForNotifying() {
            return """
                WITH user_id_filter_suiting_by_period AS (
                
                	SELECT
                		user_id id
                	FROM
                		periods
                	WHERE
                		:day_of_week BETWEEN start_day AND end_day
                		AND :time >= start_time
                		AND :time <= end_time
                
                	UNION
                
                	SELECT
                		user_id
                	FROM
                		periods
                	WHERE
                		start_day > end_day
                		AND :day_of_week >= start_day
                		AND :time >= start_time
                		AND :time <= end_time
                
                	UNION
                
                	SELECT
                		user_id
                	FROM
                		periods
                	WHERE
                		start_day > end_day
                		AND :day_of_week <= end_day
                		AND :time >= start_time
                		AND :time <= end_time
                
                ),
                
                another_user_id AS (
                	SELECT
                		id
                	FROM
                		users
                	LEFT JOIN user_id_filter_suiting_by_period user_filter
                		ON users.id = user_filter.id
                	WHERE
                		user_filter.id IS NULL
                )
                
                SELECT
                	id,
                	notifying true
                FROM
                	user_id_filter_suiting_by_period
                
                UNION
                
                SELECT
                	id,
                	notifying false
                FROM
                	another_user_id
                """;
        }

    }


}
