package com.example.springboottutorial.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void successSetsDataAndMessage() {
        ApiResponse<String> response = ApiResponse.success("payload", "ok");

        assertTrue(response.success());
        assertEquals("ok", response.message());
        assertEquals("payload", response.data());
        assertNull(response.errors());
        assertNotNull(response.timestamp());
    }

    @Test
    void errorWithoutDetailsHasNoDataOrErrors() {
        ApiResponse<Void> response = ApiResponse.error("boom");

        assertFalse(response.success());
        assertEquals("boom", response.message());
        assertNull(response.data());
        assertNull(response.errors());
    }

    @Test
    void errorWithDetailsCarriesErrorList() {
        ApiResponse<Void> response =
                ApiResponse.error("Validation failed", List.of(new ApiError("email", "bad")));

        assertFalse(response.success());
        assertEquals(1, response.errors().size());
        assertEquals("email", response.errors().get(0).field());
    }
}
