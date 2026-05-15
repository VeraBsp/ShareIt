package ru.practicum.shareit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    @NotBlank(message = "Имя не может быть пустым")
    private String text;
    private String authorName;
    private LocalDateTime created;
}
