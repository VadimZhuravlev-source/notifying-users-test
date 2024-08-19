package com.example.notifying_users.user.entities;

import com.example.notifying_users.period.entities.Period;
import com.example.notifying_users.period.entities.TimePeriod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String lastName;
    private String firstName;
    private String patronymic;

    @JsonManagedReference
    @OneToMany(mappedBy = "user",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<Period> periods;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @JsonIgnore
    public String getFullName() {
        return lastName + " " + firstName  + " " + patronymic;
    }

    public void update(User user) {
        this.lastName = user.getLastName();
        this.firstName = user.getFirstName();
        this.patronymic = user.getPatronymic();
        this.periods = user.getPeriods();
        fillDependenceEntities();
    }

    public void fillDependenceEntities() {
        if (this.periods != null) {
            this.periods.forEach(period -> {
                period.setUser(this);
                List<TimePeriod> timePeriods = period.getTimePeriods();
                if (timePeriods != null) {
                    for (TimePeriod timePeriod: timePeriods) {
                        timePeriod.setPeriod(period);
                    }
                }
            });
        }
    }

    public void validate() {
        if (this.periods != null) {
            this.periods.forEach(period -> {
                List<TimePeriod> timePeriods = period.getTimePeriods();
                if (timePeriods != null) {
                    for (TimePeriod timePeriod: timePeriods) {
                        timePeriod.validateTimes();
                    }
                }
            });
        }
    }

    public void nullIdFields() {
        this.id = null;
        if (this.periods != null) {
            for (Period period: periods) {
                period.nullIdFields();
            }
        }
    }

}
