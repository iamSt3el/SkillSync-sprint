// Author -> Kanav Jethi

// This is the SpringBootApplication file for Mentor service. This is where it starts the execution of the program.

package com.skillsync.mentorservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class MentorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MentorServiceApplication.class, args);
        log.info("Mentor service started");
    }
}
