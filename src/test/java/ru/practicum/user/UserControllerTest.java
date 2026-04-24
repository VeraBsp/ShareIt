package ru.practicum.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers =  UserController.class)
class UserControllerTest {

    @MockBean
    UserService userService;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void create_shouldReturnCreatedUser() throws Exception {
        UserDto dto = new UserDto(null, "John", "john@mail.com");
        UserDto saved = new UserDto(1L, "John", "john@mail.com");

        when(userService.create(any())).thenReturn(saved);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@mail.com"));

        verify(userService).create(any());
    }

    @Test
    void getAll_shouldReturnUsers() throws Exception {
        List<UserDto> users = List.of(
                new UserDto(1L, "John", "john@mail.com"),
                new UserDto(2L, "Mike", "mike@mail.com")
        );

        when(userService.getAll()).thenReturn(users);

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(userService).getAll();
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        mvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

    @Test
    void create_shouldFailValidation_whenEmailInvalid() throws Exception {
        UserDto dto = new UserDto(null, "John", "wrong-email");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any());
    }

}