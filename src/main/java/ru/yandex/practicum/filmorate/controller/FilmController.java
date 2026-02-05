package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.enums.SortBy;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/films")
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody FilmDto filmDto) {
        log.debug("Creating film [{}]", filmDto);
        filmDto = filmService.create(filmDto);
        return filmDto;
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody FilmDto filmDto) {
        log.debug("Updating film  [{}]", filmDto);
        filmDto = filmService.update(filmDto);
        return filmDto;
    }

    @GetMapping
    public List<FilmDto> getAll() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public FilmDto getById(@PathVariable long id) {
        return filmService.getById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        log.debug("Adding like to film [{}] by user [{}]", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable long id, @PathVariable long userId) {
        log.debug("Removing like from film [{}] by user [{}]", id, userId);
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilmsByParams(@RequestParam(defaultValue = "10") Long count,
                                                 @RequestParam(required = false) Long genreId,
                                                 @RequestParam(required = false) Long year) {
        log.debug("Received request to retrieve the most popular films: count=[{}], " +
                        "optional filters: genreId=[{}], year=[{}]", count, genreId, year);

        Map<String, Long> params = new HashMap<>();
        Optional.ofNullable(genreId).ifPresent(v -> params.put("genreId", genreId));
        Optional.ofNullable(year).ifPresent(v -> params.put("year", year));

        return filmService.getPopularFilmsByParams(params, count);
    }

    @GetMapping("/common")
    public List<FilmDto> getCommonFilms(@RequestParam long userId, @RequestParam long friendId) {
        log.debug("Received request to retrieve common films for users: userId=[{}], friendId=[{}]", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<FilmDto> getFilmsByDirectorIdWithSort(@PathVariable long directorId,
                                                      @RequestParam String sortBy) {
        log.debug("Received request to retrieve all films by director with directorId=[{}], sorted by [{}]",
                directorId, sortBy);
        SortBy sort = SortBy.fromString(sortBy);
        return filmService.getFilmsByDirectorIdWithSort(directorId, sort);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable long id) {
        log.debug("Deleting film with id=[{}]", id);
        filmService.deleteById(id);
    }

    @GetMapping("/search")
    public List<FilmDto> getSearch(@RequestParam String query, @RequestParam List<String> by) {
        log.debug("Received film search request: query=[{}], by=[{}]", query, by);
        return filmService.getSearch(query, by);
    }
}