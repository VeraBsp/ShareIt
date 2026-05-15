package ru.practicum.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.ShareItServer;
import ru.practicum.shareit.server.booking.BookingController;
import ru.practicum.shareit.server.booking.BookingCreateDto;
import ru.practicum.shareit.server.booking.BookingDto;
import ru.practicum.shareit.server.booking.BookingService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@ContextConfiguration(classes = ShareItServer.class)
class BookingControllerTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    BookingService bookingService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void create_shouldReturnBooking() throws Exception {
        Long userId = 1L;

        BookingCreateDto request = new BookingCreateDto();
        request.setItemId(10L);

        BookingDto response = BookingDto.builder()
                .id(100L)
                .build();

        when(bookingService.create(eq(userId), any()))
                .thenReturn(response);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));

        verify(bookingService).create(eq(userId), any());
    }

    @Test
    void approve_shouldReturnUpdatedBooking() throws Exception {
        Long userId = 1L;

        BookingDto response = BookingDto.builder()
                .id(5L)
                .build();

        when(bookingService.approve(eq(userId), eq(5L), eq(true)))
                .thenReturn(response);

        mvc.perform(patch("/bookings/5")
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(bookingService).approve(eq(userId), eq(5L), eq(true));
    }

    @Test
    void getById_shouldReturnBooking() throws Exception {
        Long userId = 1L;

        BookingDto response = BookingDto.builder()
                .id(7L)
                .build();

        when(bookingService.getById(eq(userId), eq(7L)))
                .thenReturn(response);

        mvc.perform(get("/bookings/7")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        verify(bookingService).getById(eq(userId), eq(7L));
    }

    @Test
    void getByBooker_shouldReturnList() throws Exception {
        Long userId = 1L;

        when(bookingService.getByBooker(eq(userId), any(), anyInt(), anyInt()))
                .thenReturn(List.of(
                        BookingDto.builder().id(1L).build(),
                        BookingDto.builder().id(2L).build()
                ));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(bookingService).getByBooker(eq(userId), any(), eq(0), eq(10));
    }

    @Test
    void getByOwner_shouldReturnList() throws Exception {
        Long userId = 1L;

        when(bookingService.getByOwner(eq(userId), any(), anyInt(), anyInt()))
                .thenReturn(List.of(
                        BookingDto.builder().id(1L).build(),
                        BookingDto.builder().id(2L).build()
                ));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(bookingService).getByOwner(eq(userId), any(), eq(0), eq(10));
    }
}