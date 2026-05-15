package ru.practicum.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.gateway.ShareItGateway;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItGateway.class)
class ItemDtoTest {
    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void serialize_shouldConvertItemDtoToJson() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Tent")
                .description("Camping tent")
                .available(true)
                .requestId(10L)
                .build();

        String result = json.write(dto).getJson();

        assertThat(result).contains("Tent");
        assertThat(result).contains("Camping tent");
        assertThat(result).contains("true");
        assertThat(result).contains("10");
    }

    @Test
    void deserialize_shouldConvertJsonToItemDto() throws Exception {
        String content = """
                {
                    "id": 1,
                    "name": "Tent",
                    "description": "Camping tent",
                    "available": true,
                    "requestId": 10
                }
                """;

        ItemDto result = json.parse(content).getObject();

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Tent");
        assertThat(result.getDescription()).isEqualTo("Camping tent");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getRequestId()).isEqualTo(10L);
    }
}