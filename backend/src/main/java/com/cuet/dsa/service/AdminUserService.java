//package com.cuet.dsa.service;
//
//import com.cuet.dsa.dto.request.AdminUserCreateRequest;
//import com.cuet.dsa.dto.request.AdminUserUpdateRequest;
//import com.cuet.dsa.dto.response.AdminUserResponse;
//import com.cuet.dsa.entity.Role;
//import com.cuet.dsa.entity.User;
//import com.cuet.dsa.enums.RoleName;
//import com.cuet.dsa.repository.RoleRepository;
//import com.cuet.dsa.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class AdminUserService {
//
//    private final UserRepository userRepository;
//    private final RoleRepository roleRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    /**
//     * Retrieve all users and map them to AdminUserResponse DTOs.
//     */
////    public List<AdminUserResponse> findAllUsers() {
////        return userRepository.findAll().stream()
////                .map(this::mapToResponse)
////                .collect(Collectors.toList());
////    }
//
//    /**
//     * Create a new user with administrative overrides.
//     */
////    @Transactional
////    public AdminUserResponse createUser(AdminUserCreateRequest req) {
////        // Validate uniqueness
////        if (userRepository.existsByEmail(req.getEmail())) {
////            throw new RuntimeException("Error: Email is already in use!");
////        }
////
////        // Map DTO to Entity
////        User user = User.builder()
////                .fullName(req.getFullName())
////                .email(req.getEmail())
////                .password(passwordEncoder.encode(req.getPassword()))
////                .enabled(req.isEnabled())
////                .emailVerified(req.isEmailVerified())
////                .roles(mapRoleNamesToEntities(req.getRoles()))
////                .build();
////
////        User savedUser = userRepository.save(user);
////        return mapToResponse(savedUser);
////    }
//
//    /**
//     * Update an existing user. Password and Roles are updated only if provided.
//     */
//    @Transactional
//    public AdminUserResponse updateUser(Long id, AdminUserUpdateRequest req) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Error: User not found with id: " + id));
//
//        // Update basic fields
//        if (req.getFullName() != null) user.setFullName(req.getFullName());
//        if (req.getEmail() != null) user.setEmail(req.getEmail());
//
//        // Update account status
//        if (req.getEnabled() != null) user.setEnabled(req.getEnabled());
//        if (req.getEmailVerified() != null) user.setEmailVerified(req.getEmailVerified());
//
//        // Update password if provided in the request
//        if (req.getPassword() != null && !req.getPassword().isBlank()) {
//            user.setPassword(passwordEncoder.encode(req.getPassword()));
//        }
//
//        // Update roles if provided
//        if (req.getRoles() != null && !req.getRoles().isEmpty()) {
//            user.setRoles(mapRoleNamesToEntities(req.getRoles()));
//        }
//
//        User updatedUser = userRepository.save(user);
//        return mapToResponse(updatedUser);
//    }
//
//    /**
//     * Delete user by ID.
//     */
//    @Transactional
//    public void deleteUser(Long id) {
//        if (!userRepository.existsById(id)) {
//            throw new RuntimeException("Error: Cannot delete. User not found with id: " + id);
//        }
//        userRepository.deleteById(id);
//    }
//
//    // --- Helper Methods ---
//
//    /**
//     * Converts a set of RoleName enums to a set of Role entities from the database.
//     */
//    private Set<Role> mapRoleNamesToEntities(Set<RoleName> roleNames) {
//        return roleNames.stream()
//                .map(name -> roleRepository.findByName(name)
//                        .orElseThrow(() -> new RuntimeException("Error: Role " + name + " not found in database.")))
//                .collect(Collectors.toSet());
//    }
//
//    /**
//     * Maps User entity to AdminUserResponse DTO.
//     */
////    private AdminUserResponse mapToResponse(User user) {
////        return AdminUserResponse.builder()
////                .id(user.getId())
////                .fullName(user.getFullName())
////                .email(user.getEmail())
////                .enabled(user.isEnabled())
////                .emailVerified(user.isEmailVerified())
////                .roles(user.getRoleNames()) // Using the helper method you added to User.java
////                .createdAt(user.getCreatedAt())
////                .updatedAt(user.getUpdatedAt())
////                .build();
////    }
//}