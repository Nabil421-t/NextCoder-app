//package com.cuet.dsa.controller;
//
//import com.cuet.dsa.dto.request.AdminUserCreateRequest;
//import com.cuet.dsa.dto.request.AdminUserUpdateRequest;
//import com.cuet.dsa.dto.response.AdminUserResponse;
//import com.cuet.dsa.dto.response.ApiResponse;
//import com.cuet.dsa.service.AdminUserService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/admin/users")
//@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")   // entire controller requires ADMIN role
//public class AdminUserController {
//
//    private final AdminUserService adminUserService;
//
//    @GetMapping
//    public ApiResponse<List<AdminUserResponse>> getAllUsers() {
//        return ApiResponse.success("Users retrieved", adminUserService.findAllUsers());
//    }
//
//    @PostMapping
//    public ApiResponse<AdminUserResponse> createUser(@Valid @RequestBody AdminUserCreateRequest req) {
//        return ApiResponse.success("User created successfully", adminUserService.createUser(req));
//    }
//
//    @PutMapping("/{id}")
//    public ApiResponse<AdminUserResponse> updateUser(
//            @PathVariable Long id,
//            @RequestBody AdminUserUpdateRequest req) {
//        return ApiResponse.success("User updated successfully", adminUserService.updateUser(id, req));
//    }
//
//    @DeleteMapping("/{id}")
//    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
//        adminUserService.deleteUser(id);
//        return ApiResponse.success("User deleted successfully", null);
//    }
//}