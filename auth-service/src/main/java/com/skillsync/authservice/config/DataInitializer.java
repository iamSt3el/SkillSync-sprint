package com.skillsync.authservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.skillsync.authservice.entity.Role;
import com.skillsync.authservice.entity.User;
import com.skillsync.authservice.entity.UserRole;
import com.skillsync.authservice.repository.RoleRepository;
import com.skillsync.authservice.repository.UserRepository;
import com.skillsync.authservice.repository.UserRoleRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           UserRoleRepository userRoleRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        // Seed roles
        if (!roleRepository.existsByName("ROLE_LEARNER")) {
            roleRepository.save(new Role("ROLE_LEARNER"));
        }
        if (!roleRepository.existsByName("ROLE_MENTOR")) {
            roleRepository.save(new Role("ROLE_MENTOR"));
        }
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
            return roleRepository.save(new Role("ROLE_ADMIN"));
        });

        // Seed default admin user
        if (!userRepository.existsByEmail("admin@skillsync.com")) {
            User admin = new User("admin@skillsync.com",
                                  passwordEncoder.encode("Admin@123"),
                                  "admin");
            User savedAdmin = userRepository.save(admin);

            // Save UserRole separately to avoid detached entity issue
            UserRole userRole = new UserRole(savedAdmin, adminRole);
            userRoleRepository.save(userRole);
        }
    }
}
