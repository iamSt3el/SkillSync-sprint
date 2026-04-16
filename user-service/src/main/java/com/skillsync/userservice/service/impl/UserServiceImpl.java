package com.skillsync.userservice.service.impl;

import com.skillsync.userservice.dto.response.UserDTO;
import com.skillsync.userservice.dto.response.UserStatsDTO;
import com.skillsync.userservice.entity.User;
import com.skillsync.userservice.exception.UserNotFoundException;
import com.skillsync.userservice.repository.UserRepository;
import com.skillsync.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final String USER_WITH_ID = "User with id ";
    private static final String NOT_FOUND = " not found";

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::new)
                .toList();
    }

    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserDTO::new);
    }

    @Override
    public List<UserDTO> getUsersByIds(List<Long> ids) {
        return userRepository.findByIdIn(ids)
                .stream()
                .map(UserDTO::new)
                .toList();
    }

    @Override
    @Cacheable(value = "users", key = "#id", unless = "#result == null")
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id).map(UserDTO::new);
    }

    @Override
    @Cacheable(value = "users", key = "'email:' + #email", unless = "#result == null")
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(UserDTO::new);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public UserDTO updateUser(Long id, UserDTO userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_WITH_ID + id + NOT_FOUND));

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());

        User savedUser = userRepository.save(user);
        log.info("User updated: id={}", id);
        return new UserDTO(savedUser);
    }

    @Override
    public UserDTO createUser(User user) {
        User savedUser = userRepository.save(user);
        log.info("User profile created: id={}", savedUser.getId());
        return new UserDTO(savedUser);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public UserDTO updateProfilePicture(Long id, String pictureUrl) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_WITH_ID + id + NOT_FOUND));
        user.setProfilePictureUrl(pictureUrl);
        User savedUser = userRepository.save(user);
        log.info("Profile picture updated: id={}", id);
        return new UserDTO(savedUser);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(USER_WITH_ID + id + NOT_FOUND);
        }
        userRepository.deleteById(id);
        log.info("User deleted: id={}", id);
        return true;
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public UserDTO blockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_WITH_ID + id + NOT_FOUND));
        user.setStatus(User.Status.BLOCKED);
        User savedUser = userRepository.save(user);
        log.warn("User blocked: id={}", id);
        return new UserDTO(savedUser);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public UserDTO unblockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_WITH_ID + id + NOT_FOUND));
        user.setStatus(User.Status.ACTIVE);
        User savedUser = userRepository.save(user);
        log.info("User unblocked: id={}", id);
        return new UserDTO(savedUser);
    }

    @Override
    public UserStatsDTO getUserStats() {
        long total    = userRepository.count();
        long learners = userRepository.countByRole("LEARNER");
        long mentors  = userRepository.countByRole("MENTOR");
        long admins   = userRepository.countByRole("ADMIN");
        return new UserStatsDTO(total, learners, mentors, admins);
    }
}
