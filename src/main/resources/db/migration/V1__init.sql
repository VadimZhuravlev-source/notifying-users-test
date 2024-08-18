CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    last_name VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    patronymic VARCHAR(255) NOT NULL
);

CREATE TABLE periods (
    id SERIAL,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    start_day INT NOT NULL,
    end_day INT NOT NULL,
    CONSTRAINT periods_pkey PRIMARY KEY (user_id, id)
);

CREATE INDEX ON periods(start_day, end_day, user_id);

CREATE TABLE time_periods (
    id SERIAL,
    period_id INT REFERENCES periods(id) ON DELETE CASCADE,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CONSTRAINT time_periods_pkey PRIMARY KEY (period_id, id)
);

CREATE INDEX ON time_periods(start_time, end_time, period_id);

CREATE TABLE events (
    id SERIAL PRIMARY KEY,
    parent_id INT REFERENCES events(id),
    notifying_date TIMESTAMP NOT NULL DEFAULT NOW(),
    message TEXT NOT NULL,
    notify_all BOOLEAN NOT NULL DEFAULT TRUE,
    notified BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX ON events(notifying_date, notified, id);

CREATE TABLE event_users (
    id SERIAL,
    event_id INT REFERENCES events(id) ON DELETE CASCADE,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT event_users_pkey PRIMARY KEY (event_id, id)
);