package com.example.springboottutorial.controller;

import com.example.springboottutorial.service.UserService;
import com.example.springboottutorial.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import com.example.springboottutorial.dto.UserRequestDTO;
import com.example.springboottutorial.dto.UserResponseDTO;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(
            @Valid @RequestBody UserRequestDTO requestDTO) {

        UserResponseDTO created = userService.createUser(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "User created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        return ResponseEntity.ok(
                ApiResponse.success(userService.getAllUsers(), "Users retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.getUserById(id), "User retrieved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO requestDTO) {

        return ResponseEntity.ok(
                ApiResponse.success(userService.updateUser(id, requestDTO), "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}