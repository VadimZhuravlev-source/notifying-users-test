package com.example.notifying_users.period.entities;

import com.example.notifying_users.user.entities.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "periods")
@Getter
@Setter
public class Period {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private DayOfWeek startDay;
    private DayOfWeek endDay;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @JsonManagedReference
    @OneToMany(mappedBy = "period",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.EAGER)
    private List<TimePeriod> timePeriods = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Period period = (Period) o;
        return Objects.equals(getId(), period.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public void nullIdFields() {
        this.id = null;
        if (timePeriods != null) {
            for (TimePeriod timePeriod : timePeriods) {
                timePeriod.nullIdFields();
            }
        }
    }

}
