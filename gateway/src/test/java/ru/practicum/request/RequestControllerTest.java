package ru.practicum.request;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.ShareItGateway;
import ru.practicum.shareit.gateway.request.RequestClient;
import ru.practicum.shareit.gateway.request.RequestController;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RequestController.class)
@ContextConfiguration(classes = ShareItGateway.class)
class RequestControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private RequestClient requestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_shouldCallClient() throws Exception {
        Long userId = 1L;

        Map<String, Object> body = Map.of("description", "Need laptop");

        when(requestClient.create(eq(userId), any()))
                .thenReturn(ResponseEntity.ok(Map.of(
                        "id", 10,
                        "description", "Need laptop"
                )));

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(requestClient).create(eq(userId), any());
    }

    @Test
    void getAllByUser_shouldCallClient() throws Exception {
        Long userId = 1L;

        when(requestClient.getAllByUser(userId))
                .thenReturn(ResponseEntity.ok(
                        java.util.List.of(Map.of("id", 1))
                ));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(requestClient).getAllByUser(userId);
    }

    @Test
    void getAll_shouldCallClient() throws Exception {
        Long userId = 1L;

        when(requestClient.getAll(eq(userId), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok(
                        java.util.List.of(Map.of("id", 2))
                ));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(requestClient).getAll(userId, 0, 10);
    }

    @Test
    void getById_shouldCallClient() throws Exception {
        Long userId = 1L;

        when(requestClient.getById(userId, 5L))
                .thenReturn(ResponseEntity.ok(Map.of(
                        "id", 5,
                        "description", "Need phone"
                )));

        mvc.perform(get("/requests/5")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(requestClient).getById(userId, 5L);
    }
}