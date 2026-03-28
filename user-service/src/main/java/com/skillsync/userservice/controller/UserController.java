package com.skillsync.userservice.controller;

import com.skillsync.userservice.dto.UserDTO;
import com.skillsync.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<UserDTO> getUserById(
            @RequestHeader(value = "X-User-Email", required = false) String email,
    		@RequestHeader(value = "X-User-Id") Long userId){
        Optional<UserDTO> user = userService.getUserById(userId);
        if (user.isPresent()) {
            if (email != null && user.get().getEmail().equalsIgnoreCase(email)) {
                return ResponseEntity.ok(user.get());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping()
    public ResponseEntity<UserDTO> updateUser(@RequestHeader(value = "X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestBody UserDTO userDetails) {
        Optional<UserDTO> user = userService.getUserById(userId);
        if (user.isPresent()) {
            if (email != null && user.get().getEmail().equalsIgnoreCase(email)) {
                UserDTO updatedUser = userService.updateUser(userId, userDetails);
                return ResponseEntity.ok(updatedUser);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
