package com.skillsync.userservice.controller;

import com.skillsync.userservice.dto.response.UserDTO;
import com.skillsync.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

    @PutMapping("/me/picture")
    public ResponseEntity<UserDTO> updateProfilePicture(
            @RequestHeader(value = "X-User-Id") Long userId,
            @RequestBody Map<String, String> body) {
        String pictureUrl = body.get("pictureUrl");
        if (pictureUrl == null || pictureUrl.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        UserDTO updated = userService.updateProfilePicture(userId, pictureUrl);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<UserDTO>> getUsersByIds(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<UserDTO> users = userService.getUsersByIds(ids);
        return ResponseEntity.ok(users);
    }
}
