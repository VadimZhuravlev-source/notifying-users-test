package com.example.notifying_users.event.user;

import com.example.notifying_users.event.entities.DelayedEvent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "event_users")
@Getter
@Setter
@NoArgsConstructor
public class EventUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private DelayedEvent delayedEvent;

    private Long userId;

    public EventUser(DelayedEvent delayedEvent, Long userId) {
        this.delayedEvent = delayedEvent;
        this.userId = userId;
    }

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
