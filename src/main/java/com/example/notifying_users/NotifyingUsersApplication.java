package com.example.notifying_users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotifyingUsersApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotifyingUsersApplication.class, args);
	}

}
