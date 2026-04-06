package ru.practicum.user;

import java.util.List;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto update(Long userId, UserDto userDto);

    UserDto getById(Long id);

    List<UserDto> getAll();

    void delete(Long userId);
}
