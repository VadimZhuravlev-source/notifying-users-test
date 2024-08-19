package com.example.notifying_users.period.entities;

import com.example.notifying_users.period.exceptions.TimeValidationException;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "time_periods")
@Setter
@Getter
public class TimePeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startTime;
    private LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "period_id")
    @JsonBackReference
    private Period period;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimePeriod period = (TimePeriod) o;
        return Objects.equals(getId(), period.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public void validateTimes() {
        if (startTime.isAfter(endTime))
            throw new TimeValidationException("Для периода " + period.getStartDay() + " - " + period.getEndDay()
                    + " неверно указан временной интервал " + startTime + " " + endTime);
    }

    public void nullIdFields() {
        this.id = null;
    }

}
