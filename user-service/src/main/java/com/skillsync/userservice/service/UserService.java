package com.skillsync.userservice.service;

import com.skillsync.userservice.dto.response.UserDTO;
import com.skillsync.userservice.dto.response.UserStatsDTO;
import com.skillsync.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<UserDTO> getAllUsers();

    Page<UserDTO> getAllUsers(Pageable pageable);

    List<UserDTO> getUsersByIds(List<Long> ids);

    Optional<UserDTO> getUserById(Long id);

    Optional<UserDTO> getUserByEmail(String email);

    UserDTO updateUser(Long id, UserDTO userDetails);

    UserDTO createUser(User user);

    UserDTO updateProfilePicture(Long id, String pictureUrl);

    boolean deleteUser(Long id);

    UserDTO blockUser(Long id);

    UserDTO unblockUser(Long id);

    UserStatsDTO getUserStats();
}
