package com.skillsync.userservice.controller;

import com.skillsync.userservice.dto.UserDTO;
import com.skillsync.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        Optional<UserDTO> user = userService.getUserById(id);
        if (user.isPresent()) {
            if (email != null && user.get().getEmail().equalsIgnoreCase(email)) {
                return ResponseEntity.ok(user.get());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestBody UserDTO userDetails) {
        Optional<UserDTO> user = userService.getUserById(id);
        if (user.isPresent()) {
            if (email != null && user.get().getEmail().equalsIgnoreCase(email)) {
                UserDTO updatedUser = userService.updateUser(id, userDetails);
                return ResponseEntity.ok(updatedUser);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
