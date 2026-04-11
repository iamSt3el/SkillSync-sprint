package com.skillsync.userservice.controller.admin;

import com.skillsync.userservice.dto.response.UserDTO;
import com.skillsync.userservice.dto.response.UserStatsDTO;
import com.skillsync.userservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private static final String USER_NOT_FOUND = "User not found";

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.getAllUsers(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/stats")
    public ResponseEntity<UserStatsDTO> getUserStats() {
        return ResponseEntity.ok(userService.getUserStats());
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
