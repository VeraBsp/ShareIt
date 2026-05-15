package ru.practicum.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.gateway.ShareItGateway;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItGateway.class)
class UserDtoTest {
    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void serialize_shouldConvertUserDtoToJson() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("Ivan")
                .email("ivan@mail.com")
                .build();

        String result = json.write(dto).getJson();

        assertThat(result).contains("Ivan");
        assertThat(result).contains("ivan@mail.com");
        assertThat(result).contains("1");
    }

    @Test
    void deserialize_shouldConvertJsonToUserDto() throws Exception {
        String content = """
                {
                    "id": 1,
                    "name": "Ivan",
                    "email": "ivan@mail.com"
                }
                """;

        UserDto result = json.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Ivan");
        assertThat(result.getEmail()).isEqualTo("ivan@mail.com");
    }
}