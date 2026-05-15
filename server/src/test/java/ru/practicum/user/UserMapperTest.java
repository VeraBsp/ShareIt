package ru.practicum.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.UserMapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UserMapperTest {

    @Test
    void toUserDto_shouldMapEntityToDto() {
        User user = User.builder()
                .id(1L)
                .name("Vera")
                .email("vera@mail.com")
                .build();

        UserDto dto = UserMapper.toUserDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Vera");
        assertThat(dto.getEmail()).isEqualTo("vera@mail.com");
    }

    @Test
    void toUser_shouldMapDtoToEntity() {
        UserDto dto = UserDto.builder()
                .id(2L)
                .name("Ivan")
                .email("ivan@mail.com")
                .build();

        User user = UserMapper.toUser(dto);

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getName()).isEqualTo("Ivan");
        assertThat(user.getEmail()).isEqualTo("ivan@mail.com");
    }

    @Test
    void toUserDto_shouldHandleNullValues() {
        User user = User.builder()
                .id(null)
                .name(null)
                .email(null)
                .build();

        UserDto dto = UserMapper.toUserDto(user);

        assertThat(dto.getId()).isNull();
        assertThat(dto.getName()).isNull();
        assertThat(dto.getEmail()).isNull();
    }

    @Test
    void toUser_shouldHandleNullValues() {
        UserDto dto = UserDto.builder()
                .id(null)
                .name(null)
                .email(null)
                .build();

        User user = UserMapper.toUser(dto);

        assertThat(user.getId()).isNull();
        assertThat(user.getName()).isNull();
        assertThat(user.getEmail()).isNull();
    }
}