package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;

@Service
public class GenreService {

    private final GenreStorage genreStorage;

    public GenreService(@Qualifier("genreDbStorage") GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public List<GenreDto> getAllGenres() {
        return genreStorage.getAll().stream().map(GenreMapper::mapToGenreDto).toList();
    }

    public GenreDto getGenreById(int id) {
        Genre genre = genreStorage.findByGenreId(id);
        if (genre == null) {
            throw new NotFoundException("Genre not found");
        }
        return GenreMapper.mapToGenreDto(genre);
    }
}
