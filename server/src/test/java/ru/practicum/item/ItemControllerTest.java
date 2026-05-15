package ru.practicum.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.dto.CommentDto;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.server.ShareItServer;
import ru.practicum.shareit.server.item.*;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@ContextConfiguration(classes = ShareItServer.class)
class ItemControllerTest {
    @MockBean
    ItemService itemService;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void create_shouldReturnItem() throws Exception {
        Long userId = 1L;

        ItemDto request = new ItemDto(null, "Drill", "Power tool", true, null);
        ItemDto response = new ItemDto(1L, "Drill", "Power tool", true, null);

        when(itemService.create(any(), eq(userId))).thenReturn(response);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"));

        verify(itemService).create(any(), eq(userId));
    }

    @Test
    void getById_shouldReturnItem() throws Exception {
        Long userId = 1L;

        ItemWithBookingDto dto = ItemWithBookingDto.builder()
                .id(1L)
                .name("Drill")
                .build();

        when(itemService.getById(1L, userId)).thenReturn(dto);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(itemService).getById(1L, userId);
    }

    @Test
    void getAllByOwner_shouldReturnList() throws Exception {
        Long userId = 1L;

        when(itemService.getAllByOwner(eq(userId), anyInt(), anyInt()))
                .thenReturn(List.of(new ItemWithBookingDto(), new ItemWithBookingDto()));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(itemService).getAllByOwner(eq(userId), eq(0), eq(10));
    }

    @Test
    void search_shouldReturnItems() throws Exception {
        when(itemService.search(eq("drill"), anyInt(), anyInt()))
                .thenReturn(List.of(new ItemDto()));

        mvc.perform(get("/items/search")
                        .param("text", "drill")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(itemService).search(eq("drill"), eq(0), eq(10));
    }

    @Test
    void addComment_shouldReturnComment() throws Exception {
        Long userId = 1L;

        CommentDto request = CommentDto.builder()
                .text("Good item")
                .build();

        CommentDto response = CommentDto.builder()
                .id(1L)
                .text("Good item")
                .build();

        when(itemService.addComment(eq(1L), eq(userId), any()))
                .thenReturn(response);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Good item"));

        verify(itemService).addComment(eq(1L), eq(userId), any());
    }
}