package ru.practicum.item;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.ShareItGateway;
import ru.practicum.shareit.gateway.item.ItemClient;
import ru.practicum.shareit.gateway.item.ItemController;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@ContextConfiguration(classes = ShareItGateway.class)
class ItemControllerTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemClient itemClient;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Test
    void create_shouldCallClient() throws Exception {
        when(itemClient.create(anyLong(), any()))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Drill",
                          "description": "Tool",
                          "available": true
                        }
                    """))
                .andExpect(status().isOk());

        verify(itemClient).create(eq(1L), any());
    }

    @Test
    void update_shouldCallClient() throws Exception {
        when(itemClient.update(anyLong(), anyLong(), any()))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(patch("/items/5")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isOk());

        verify(itemClient).update(eq(1L), eq(5L), any());
    }

    @Test
    void getById_shouldCallClient() throws Exception {
        when(itemClient.getById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/items/5")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemClient).getById(eq(1L), eq(5L));
    }

    @Test
    void getAllByOwner_shouldCallClient() throws Exception {
        when(itemClient.getAllByOwner(anyLong(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/items")
                        .header(USER_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(itemClient).getAllByOwner(eq(1L), eq(0), eq(10));
    }

    @Test
    void search_shouldCallClient() throws Exception {
        when(itemClient.search(anyString(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/items/search")
                        .param("text", "drill")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(itemClient).search(eq("drill"), eq(0), eq(10));
    }

    @Test
    void addComment_shouldCallClient() throws Exception {
        when(itemClient.addComment(anyLong(), anyLong(), any()))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(post("/items/5/comment")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Good\"}"))
                .andExpect(status().isOk());

        verify(itemClient).addComment(eq(1L), eq(5L), any());
    }
}