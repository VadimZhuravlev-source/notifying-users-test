package com.example.notifying_users.user.entities;

import com.example.notifying_users.period.entities.Period;
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Period> periods;

    public String getFullName() {
        return lastName + " " + firstName  + " " + patronymic;
    }

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

    public void update(User user) {
        this.lastName = user.getLastName();
        this.firstName = user.getFirstName();
        this.patronymic = user.getPatronymic();
        this.periods = user.getPeriods();
    }

}
