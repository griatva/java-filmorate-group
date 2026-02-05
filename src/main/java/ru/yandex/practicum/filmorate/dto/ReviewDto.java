package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long reviewId;
    @NotBlank(message = "Review content must not be empty")
    private String content;
    @NotNull(message = "Review type must be specified (positive or negative)")
    private Boolean isPositive;
    @NotNull(message = "User ID must be specified")
    private Long userId;
    @NotNull(message = "Film ID must be specified")
    private Long filmId;
    private Integer useful;
}
