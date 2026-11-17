package com.example.springboottutorial.controller;

import com.example.springboottutorial.dto.UserRequestDTO;
import com.example.springboottutorial.dto.UserResponseDTO;
import com.example.springboottutorial.exception.GlobalExceptionHandler;
import com.example.springboottutorial.exception.UserNotFoundException;
import com.example.springboottutorial.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private UserResponseDTO sampleUser() {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(1L);
        dto.setFirstName("Ada");
        dto.setLastName("Lovelace");
        dto.setEmail("ada@example.com");
        return dto;
    }

    private static final String VALID_BODY =
            "{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"email\":\"ada@example.com\"}";

    @Test
    void createUserReturns201WithEnvelope() throws Exception {
        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(sampleUser());

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void getAllUsersReturnsListInEnvelope() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].email").value("ada@example.com"));
    }

    @Test
    void getUserByIdReturnsEnvelope() throws Exception {
        when(userService.getUserById(1L)).thenReturn(sampleUser());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Ada"));
    }

    @Test
    void updateUserReturnsEnvelope() throws Exception {
        when(userService.updateUser(eq(1L), any(UserRequestDTO.class))).thenReturn(sampleUser());

        mockMvc.perform(put("/api/users/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated successfully"));
    }

    @Test
    void deleteUserReturns204WithEmptyBody() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    void notFoundReturns404Envelope() throws Exception {
        when(userService.getUserById(9L)).thenThrow(new UserNotFoundException("User not found with id: 9"));

        mockMvc.perform(get("/api/users/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found with id: 9"));
    }

    @Test
    void validationFailureReturnsFieldErrorList() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"lastName\":\"L\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{bad json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Malformed or unreadable request body"));
    }

    @Test
    void typeMismatchReturns400() throws Exception {
        mockMvc.perform(get("/api/users/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));
    }

    @Test
    void unexpectedExceptionReturns500WithoutLeakingDetails() throws Exception {
        doThrow(new RuntimeException("db password leaked")).when(userService).deleteUser(2L);

        mockMvc.perform(delete("/api/users/2"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
