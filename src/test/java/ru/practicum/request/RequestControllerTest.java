package ru.practicum.request;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RequestController.class)
class RequestControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private RequestService requestService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_shouldReturnCreatedRequest() throws Exception {
        Long userId = 1L;

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need laptop")
                .build();

        ItemRequestDto responseDto = ItemRequestDto.builder()
                .id(10L)
                .description("Need laptop")
                .requestorId(userId)
                .build();

        when(requestService.create(any(), eq(userId)))
                .thenReturn(responseDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.description").value("Need laptop"));

        verify(requestService).create(any(), eq(userId));
    }

    @Test
    void getAllByUser_shouldReturnList() throws Exception {
        Long userId = 1L;

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Need item")
                .build();

        when(requestService.getAllByUser(userId))
                .thenReturn(List.of(dto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Need item"));

        verify(requestService).getAllByUser(userId);
    }

    @Test
    void getAll_shouldReturnPagedList() throws Exception {
        Long userId = 1L;

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(2L)
                .description("Need bike")
                .build();

        when(requestService.getAll(eq(userId), eq(0), eq(10)))
                .thenReturn(List.of(dto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(requestService).getAll(userId, 0, 10);
    }

    @Test
    void getById_shouldReturnRequest() throws Exception {
        Long userId = 1L;

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(5L)
                .description("Need phone")
                .build();

        when(requestService.getById(5L, userId))
                .thenReturn(dto);

        mvc.perform(get("/requests/5")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.description").value("Need phone"));

        verify(requestService).getById(5L, userId);
    }
}