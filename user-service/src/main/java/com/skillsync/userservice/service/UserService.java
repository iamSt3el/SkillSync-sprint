package com.skillsync.userservice.service;

import com.skillsync.userservice.dto.UserDTO;
import com.skillsync.userservice.entity.User;
import com.skillsync.userservice.exception.UserNotFoundException;
import com.skillsync.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private static final String USER_WITH_ID = "User with id ";
    private static final String NOT_FOUND = " not found";

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::new)
                .toList();
    }
    
    public List<UserDTO> getUsersByIds(List<Long> ids) {
        return userRepository.findByIdIn(ids)
                .stream()
                .map(UserDTO::new)
                .toList();
    }

    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id).map(UserDTO::new);
    }

    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(UserDTO::new);
    }

    public UserDTO updateUser(Long id, UserDTO userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_WITH_ID + id + NOT_FOUND));

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());

        User savedUser = userRepository.save(user);
        log.info("User updated: id={}", id);
        return new UserDTO(savedUser);
    }

    public UserDTO createUser(User user) {
        User savedUser = userRepository.save(user);
        log.info("User profile created: id={}", savedUser.getId());
        return new UserDTO(savedUser);
    }

    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(USER_WITH_ID + id + NOT_FOUND);
        }
        userRepository.deleteById(id);
        log.info("User deleted: id={}", id);
        return true;
    }

    public UserDTO blockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_WITH_ID + id + NOT_FOUND));
        user.setStatus(User.Status.BLOCKED);
        User savedUser = userRepository.save(user);
        log.warn("User blocked: id={}", id);
        return new UserDTO(savedUser);
    }

    public UserDTO unblockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_WITH_ID + id + NOT_FOUND));
        user.setStatus(User.Status.ACTIVE);
        User savedUser = userRepository.save(user);
        log.info("User unblocked: id={}", id);
        return new UserDTO(savedUser);
    }
}