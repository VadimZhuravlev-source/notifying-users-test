CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    last_name VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    patronymic VARCHAR(255) NOT NULL
);

CREATE TABLE periods (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    start_day INT NOT NULL,
    end_day INT NOT NULL
);

CREATE INDEX ON periods(user_id, start_day, end_day);

CREATE TABLE time_periods (
    id SERIAL PRIMARY KEY,
    period_id INT REFERENCES periods(id) ON DELETE CASCADE,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

CREATE INDEX ON time_periods(period_id, start_time, end_time);

CREATE TABLE events (
    id SERIAL PRIMARY KEY,
    notifying_date TIMESTAMP NOT NULL DEFAULT NOW(),
    message TEXT
);

CREATE TABLE delayed_events (
    id SERIAL PRIMARY KEY,
    event_id INT REFERENCES events(id) ON DELETE CASCADE,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    notifying_date TIMESTAMP NOT NULL DEFAULT NOW(),
    notified BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX ON delayed_events(notifying_date, notified, id);

--CREATE TABLE event_users (
--    id SERIAL PRIMARY KEY,
--    event_id INT REFERENCES delayed_events(id) ON DELETE CASCADE,
--    user_id INT REFERENCES users(id) ON DELETE CASCADE
--);
--
--CREATE INDEX ON event_users(event_id, user_id);