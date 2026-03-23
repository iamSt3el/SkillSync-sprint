package com.skillsync.userservice.service;

import com.skillsync.userservice.dto.UserDTO;
import com.skillsync.userservice.entity.User;
import com.skillsync.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
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
                .orElseThrow(() -> new com.skillsync.userservice.exception.UserNotFoundException("User with id " + id + " not found"));

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());

        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }

    public UserDTO createUser(User user) {
        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }

    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new com.skillsync.userservice.exception.UserNotFoundException("User with id " + id + " not found");
        }
        userRepository.deleteById(id);
        return true;
    }

    public UserDTO blockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.skillsync.userservice.exception.UserNotFoundException("User with id " + id + " not found"));
        user.setStatus(User.Status.BLOCKED);
        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }

    public UserDTO unblockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.skillsync.userservice.exception.UserNotFoundException("User with id " + id + " not found"));
        user.setStatus(User.Status.ACTIVE);
        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser);
    }
}