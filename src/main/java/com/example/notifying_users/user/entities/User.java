package com.example.notifying_users.user.entities;

import com.example.notifying_users.period.entities.Period;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
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

}
