package com.skillsync.userservice.controller.internal;

import com.skillsync.userservice.dto.UserDTO;
import com.skillsync.userservice.dto.UserEmailDTO;
import com.skillsync.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        Optional<UserDTO> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/email")
    public ResponseEntity<UserEmailDTO> getUserEmail(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(u -> ResponseEntity.ok(new UserEmailDTO(u.getEmail())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserDTO> getUserByEmail(@RequestParam String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/batch")
    public ResponseEntity<List<UserDTO>> getUsersByIds(@RequestBody List<Long> ids) {
        List<UserDTO> users = ids.stream()
                .flatMap(id -> userService.getUserById(id).stream())
                .toList();
        return ResponseEntity.ok(users);
    }
}
