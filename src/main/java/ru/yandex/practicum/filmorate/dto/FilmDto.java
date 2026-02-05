package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.annotations.MinDate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Film.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilmDto {
    Long id;
    @NotBlank(message = "Film title must not be empty") String name;
    @Size(max = 200, message = "Film description must not exceed 200 characters") String description;
    @MinDate(value = "1895-12-28", message = "Release date must not be earlier than December 28, 1895")
    LocalDate releaseDate;
    @Positive(message = "Film duration must be a positive number") Long duration;
    Set<Long> userLikeIds = new HashSet<>();
    List<GenreDto> genres = new ArrayList<>();
    MpaDto mpa;
    Set<DirectorDto> directors;

    public FilmDto(Long id, String name, String description, LocalDate releaseDate,
                   Long duration, List<GenreDto> genres, MpaDto mpa) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.userLikeIds = new HashSet<>();
        this.genres = genres;
        this.mpa = mpa;
    }
}
