package ru.practicum.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.exception.NotFoundException;
import ru.practicum.item.ItemRepository;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {
    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    private RequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RequestServiceImpl(requestRepository, itemRepository, userRepository);
    }

    @Test
    void create_shouldSaveRequest_whenUserExists() {
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .name("User")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        ItemRequest saved = ItemRequest.builder()
                .id(10L)
                .description("Need item")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        when(requestRepository.save(any(ItemRequest.class)))
                .thenReturn(saved);

        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need item")
                .build();

        ItemRequestDto result = service.create(dto, userId);

        assertThat(result.getDescription()).isEqualTo("Need item");

        verify(userRepository).findById(userId);
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void create_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        ItemRequestDto dto = ItemRequestDto.builder()
                .description("text")
                .build();

        assertThrows(NotFoundException.class,
                () -> service.create(dto, 1L));

        verify(requestRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnRequestWithItems() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("User")
                .email("user@mail.com")
                .build();

        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Need bike")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        when(requestRepository.findById(1L))
                .thenReturn(Optional.of(request));

        when(itemRepository.findByRequest_Id(1L))
                .thenReturn(List.of());

        ItemRequestDto result = service.getById(1L, userId);

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Need bike");
        assertThat(result.getRequestorId()).isEqualTo(userId);

        verify(itemRepository).findByRequest_Id(1L);
    }

    @Test
    void getById_shouldThrow_whenRequestNotFound() {
        when(requestRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getById(99L, 1L));
    }

    @Test
    void getAllByUser_shouldReturnRequests() {
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .name("User")
                .email("user@mail.com")
                .build();

        ItemRequest request = ItemRequest.builder()
                .id(10L)
                .description("Need something")
                .requestor(user)               // ВАЖНО
                .created(LocalDateTime.now())  // желательно
                .build();

        when(requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId))
                .thenReturn(List.of(request));

        when(itemRepository.findByRequest_Id(10L))
                .thenReturn(List.of());

        List<ItemRequestDto> result = service.getAllByUser(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription())
                .isEqualTo("Need something");
        assertThat(result.get(0).getRequestorId())
                .isEqualTo(userId);

        verify(requestRepository).findAllByRequestorIdOrderByCreatedDesc(userId);
        verify(itemRepository).findByRequest_Id(10L);
    }

    @Test
    void getAll_shouldReturnPagedRequests() {
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .name("User")
                .email("user@mail.com")
                .build();

        ItemRequest request = ItemRequest.builder()
                .id(5L)
                .description("Need laptop")
                .requestor(user)               // ВАЖНО
                .created(LocalDateTime.now())  // желательно
                .build();

        when(requestRepository.findByRequestorIdNotOrderByCreatedDesc(
                eq(userId),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(request)));

        when(itemRepository.findByRequest_Id(5L))
                .thenReturn(List.of());

        List<ItemRequestDto> result = service.getAll(userId, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription())
                .isEqualTo("Need laptop");
        assertThat(result.get(0).getRequestorId())
                .isEqualTo(userId);

        verify(requestRepository)
                .findByRequestorIdNotOrderByCreatedDesc(eq(userId), any(Pageable.class));
        verify(itemRepository).findByRequest_Id(5L);
    }
}