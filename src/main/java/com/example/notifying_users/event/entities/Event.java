package com.example.notifying_users.event.entities;

import com.example.notifying_users.event.user.EventUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long parentId;
    private LocalDateTime notifyingDate;
    private String message;
    private boolean notifyAll;
    private boolean notified;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<EventUser> users = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(getId(), event.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public void update(Event event) {
        this.message = event.getMessage();
        this.notifyingDate = event.getNotifyingDate();
        this.notifyAll = event.isNotifyAll();
        this.notified = event.isNotified();
    }

}
