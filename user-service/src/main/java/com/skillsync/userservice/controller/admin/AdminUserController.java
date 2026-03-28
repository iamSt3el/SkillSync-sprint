package com.skillsync.userservice.controller.admin;

import com.skillsync.userservice.dto.UserDTO;
import com.skillsync.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private static final String USER_NOT_FOUND = "User not found";

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok("User deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(USER_NOT_FOUND);
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<Object> blockUser(@PathVariable Long id) {
        UserDTO blockedUser = userService.blockUser(id);
        if (blockedUser != null) {
            return ResponseEntity.ok(blockedUser);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(USER_NOT_FOUND);
    }

    @PutMapping("/{id}/unblock")
    public ResponseEntity<Object> unblockUser(@PathVariable Long id) {
        UserDTO unblockedUser = userService.unblockUser(id);
        if (unblockedUser != null) {
            return ResponseEntity.ok(unblockedUser);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(USER_NOT_FOUND);
    }
}
