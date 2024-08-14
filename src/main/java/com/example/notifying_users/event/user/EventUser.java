package com.example.notifying_users.event.user;

import com.example.notifying_users.event.entities.Event;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class EventUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    private Long user_id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventUser eventUser = (EventUser) o;
        return Objects.equals(id, eventUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
