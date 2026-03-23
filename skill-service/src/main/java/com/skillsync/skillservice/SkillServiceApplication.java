package com.skillsync.skillservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SkillServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkillServiceApplication.class, args);
        System.out.println("Skill Service started");
    }
}
