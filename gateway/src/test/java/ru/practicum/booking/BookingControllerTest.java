package ru.practicum.booking;

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
import ru.practicum.shareit.gateway.booking.BookingClient;
import ru.practicum.shareit.gateway.booking.BookingController;
import ru.practicum.shareit.gateway.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@ContextConfiguration(classes = ShareItGateway.class)
class BookingControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    BookingClient bookingClient;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void create_shouldCallClient() throws Exception {
        Long userId = 1L;

        BookItemRequestDto request = BookItemRequestDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        when(bookingClient.bookItem(eq(userId), any()))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(bookingClient).bookItem(eq(userId), any());
    }
}