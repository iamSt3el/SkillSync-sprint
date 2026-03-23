package com.skillsync.userservice.controller.admin;

import com.skillsync.userservice.dto.UserDTO;
import com.skillsync.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

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
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<?> blockUser(@PathVariable Long id) {
        UserDTO blockedUser = userService.blockUser(id);
        if (blockedUser != null) {
            return ResponseEntity.ok(blockedUser);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    @PutMapping("/{id}/unblock")
    public ResponseEntity<?> unblockUser(@PathVariable Long id) {
        UserDTO unblockedUser = userService.unblockUser(id);
        if (unblockedUser != null) {
            return ResponseEntity.ok(unblockedUser);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }
}
