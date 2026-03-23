package ru.practicum.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.exception.NotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Autowired
    public UserServiceImpl() {
    }

    @Override
    public UserDto create(UserDto userDto) {
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
            throw new RuntimeException("Email уже занят");
        }
        User user = UserMapper.toUser(userDto);
        user.setId(nextId++);
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User user = users.get(id);

        if (user == null) {
            throw new RuntimeException("Пользователь не найден");
        }
        if (userDto.getName() != null) {
            if (userDto.getName().isBlank()) {
                throw new RuntimeException("Имя не может быть пустым");
            }
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (userDto.getEmail().isBlank()) {
                throw new RuntimeException("Email не может быть пустым");
            }
            if (!userDto.getEmail().contains("@")) {
                throw new RuntimeException("Email некорректный");
            }
            boolean emailExists = users.values().stream()
                    .anyMatch(u ->
                            u.getEmail().equals(userDto.getEmail()) &&
                                    !u.getId().equals(id)
                    );
            if (emailExists) {
                throw new RuntimeException("Email уже используется");
            }
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getById(Long id) {
        User user = users.get(id);
        if (user == null) throw new NotFoundException("Пользователь не найден");
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new RuntimeException("Пользователь не найден");
        }
        users.remove(userId);
    }
}
