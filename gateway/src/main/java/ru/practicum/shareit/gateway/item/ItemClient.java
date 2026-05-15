package ru.practicum.shareit.gateway.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.dto.CommentDto;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.gateway.client.BaseClient;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl,
                      RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .build()
        );
    }

    public ResponseEntity<Object> create(Long userId, ItemDto dto) {
        return post("", userId, dto);
    }

    public ResponseEntity<Object> update(Long userId, Long itemId, ItemUpdateDto dto) {
        return patch("/" + itemId, userId, dto);
    }

    public ResponseEntity<Object> getById(Long userId, Long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getAllByOwner(Long userId, int from, int size) {
        Map<String, Object> params = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, params);
    }

    public ResponseEntity<Object> search(String text, int from, int size) {
        Map<String, Object> params = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", null, params);
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, CommentDto dto) {
        return post("/" + itemId + "/comment", userId, dto);
    }
}
