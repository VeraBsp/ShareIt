package ru.practicum.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.ShareItGateway;
import ru.practicum.shareit.gateway.user.UserClient;
import ru.practicum.shareit.gateway.user.UserController;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers =  UserController.class)
@ContextConfiguration(classes = ShareItGateway.class)
class UserControllerTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserClient userClient;

    @Test
    void create_shouldCallClient() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "John",
                "email", "john@mail.com"
        );

        when(userClient.create(any()))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(userClient).create(any());
    }

    @Test
    void getAll_shouldCallClient() throws Exception {
        when(userClient.getAll())
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userClient).getAll();
    }

    @Test
    void getById_shouldCallClient() throws Exception {
        when(userClient.getById(1L))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk());

        verify(userClient).getById(1L);
    }

    @Test
    void update_shouldCallClient() throws Exception {
        Map<String, Object> body = Map.of("name", "Mike");

        when(userClient.update(eq(1L), any()))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(userClient).update(eq(1L), any());
    }

    @Test
    void delete_shouldCallClient() throws Exception {
        when(userClient.delete(1L))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userClient).delete(1L);
    }
}