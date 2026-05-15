package ru.practicum.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.user.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private UserService userService;

    @Mock
    UserRepository userRepository;

    @BeforeEach
    public void setUp(){
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void create_shouldSaveUser_whenEmailIsFree() {
        UserDto dto = UserDto.builder()
                .name("John")
                .email("john@mail.com")
                .build();

        when(userRepository.findByEmail("john@mail.com"))
                .thenReturn(Optional.empty());

        User savedUser = User.builder()
                .id(1L)
                .name("John")
                .email("john@mail.com")
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserDto result = userService.create(dto);

        assertEquals("John", result.getName());
        assertEquals("john@mail.com", result.getEmail());
        assertEquals(1L, result.getId());

        verify(userRepository).findByEmail("john@mail.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_shouldThrowException_whenEmailExists() {
        UserDto dto = UserDto.builder()
                .email("exist@mail.com")
                .build();

        when(userRepository.findByEmail("exist@mail.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(DataIntegrityViolationException.class,
                () -> userService.create(dto));

        verify(userRepository).findByEmail("exist@mail.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnUser_whenExists() {
        User user = User.builder()
                .id(1L)
                .name("John")
                .email("john@mail.com")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        UserDto result = userService.getById(1L);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@mail.com");
    }

    @Test
    void getById_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll_shouldReturnList() {
        when(userRepository.findAll())
                .thenReturn(List.of(
                        User.builder().id(1L).name("A").email("a@mail.com").build(),
                        User.builder().id(2L).name("B").email("b@mail.com").build()
                ));

        List<UserDto> result = userService.getAll();
        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getName());
        assertEquals("B", result.get(1).getName());
    }

    @Test
    void update_shouldChangeNameAndEmail_whenValid() {
        User existing = User.builder()
                .id(1L)
                .name("Old")
                .email("old@mail.com")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(userRepository.findByEmail("new@mail.com"))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserDto updateDto = UserDto.builder()
                .name("New")
                .email("new@mail.com")
                .build();

        UserDto result = userService.update(1L, updateDto);

        assertEquals("New", result.getName());
        assertEquals("new@mail.com", result.getEmail());
    }

    @Test
    void update_shouldThrow_whenEmailAlreadyUsed() {
        User existing = User.builder()
                .id(1L)
                .name("Old")
                .email("old@mail.com")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(userRepository.findByEmail("taken@mail.com"))
                .thenReturn(Optional.of(new User()));

        UserDto dto = UserDto.builder()
                .email("taken@mail.com")
                .build();

        assertThrows(DataIntegrityViolationException.class,
                () -> userService.update(1L, dto));
    }

    @Test
    void delete_shouldCallRepository() {
        userService.delete(1L);
        verify(userRepository).deleteById(1L);
    }
}