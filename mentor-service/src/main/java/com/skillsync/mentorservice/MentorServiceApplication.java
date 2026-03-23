// Author -> Kanav Jethi

// This is the SpringBootApplication file for Mentor service. This is where it starts the execution of the program.

package com.skillsync.mentorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication   // This annotation marks the start of a spring boot application.
@EnableDiscoveryClient  // With this annotation, the eureka server can find or discover this service.
@EnableFeignClients    // By using this annotation, the data transfer between services is possible.

public class MentorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MentorServiceApplication.class, args);
        System.out.println("Mentor service-started");
    }
}
