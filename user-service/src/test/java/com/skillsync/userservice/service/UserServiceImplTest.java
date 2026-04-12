package com.skillsync.userservice.service;

import com.skillsync.userservice.dto.response.UserDTO;
import com.skillsync.userservice.entity.User;
import com.skillsync.userservice.exception.UserNotFoundException;
import com.skillsync.userservice.repository.UserRepository;
import com.skillsync.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("jdoe", "John Doe", "john@example.com", "hashed", "LEARNER");
        user.setId(1L);
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserById: returns DTO when user exists")
    void getUserById_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<UserDTO> result = userService.getUserById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("getUserById: returns empty when user not found")
    void getUserById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<UserDTO> result = userService.getUserById(99L);

        assertThat(result).isEmpty();
    }

    // ── getUserByEmail ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserByEmail: returns DTO for known email")
    void getUserByEmail_found() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        Optional<UserDTO> result = userService.getUserByEmail("john@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("jdoe");
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllUsers: returns list of DTOs")
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createUser: saves and returns DTO")
    void createUser_savesUser() {
        when(userRepository.save(user)).thenReturn(user);

        UserDTO result = userService.createUser(user);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(userRepository).save(user);
    }

    // ── updateUser ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateUser: updates name and email when user exists")
    void updateUser_success() {
        UserDTO update = new UserDTO();
        update.setName("Jane Doe");
        update.setEmail("jane@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO result = userService.updateUser(1L, update);

        assertThat(result).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateUser: throws UserNotFoundException when user not found")
    void updateUser_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, new UserDTO()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── updateProfilePicture ──────────────────────────────────────────────────

    @Test
    @DisplayName("updateProfilePicture: sets URL and saves")
    void updateProfilePicture_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO result = userService.updateProfilePicture(1L, "http://img.example.com/pic.jpg");

        assertThat(result).isNotNull();
        assertThat(user.getProfilePictureUrl()).isEqualTo("http://img.example.com/pic.jpg");
    }

    // ── deleteUser ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteUser: deletes and returns true when user exists")
    void deleteUser_success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        boolean result = userService.deleteUser(1L);

        assertThat(result).isTrue();
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUser: throws UserNotFoundException when user does not exist")
    void deleteUser_notFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepository, never()).deleteById(any());
    }

    // ── blockUser / unblockUser ───────────────────────────────────────────────

    @Test
    @DisplayName("blockUser: sets status to BLOCKED")
    void blockUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.blockUser(1L);

        assertThat(user.getStatus()).isEqualTo(User.Status.BLOCKED);
    }

    @Test
    @DisplayName("unblockUser: sets status back to ACTIVE")
    void unblockUser_success() {
        user.setStatus(User.Status.BLOCKED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.unblockUser(1L);

        assertThat(user.getStatus()).isEqualTo(User.Status.ACTIVE);
    }
}
