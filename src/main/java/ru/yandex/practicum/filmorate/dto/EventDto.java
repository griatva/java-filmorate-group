package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    @NotNull(message = "Event ID must not be null")
    private Long eventId;

    @NotNull(message = "User ID must not be null")
    @Positive(message = "User ID must be a positive number")
    private Long userId;

    @NotNull(message = "Timestamp must not be null")
    @Positive(message = "Timestamp must be a positive number")
    private Long timestamp;

    @NotNull(message = "Event type must not be null")
    private EventType eventType;

    @NotNull(message = "Operation must not be null")
    private Operation operation;

    @NotNull(message = "Entity ID must not be null")
    @Positive(message = "Entity ID must be a positive number")
    private Long entityId;
}
