package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;


@RestController
@RequestMapping(value = "/users")
@Validated
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    UserService userService;
    FilmService filmService;

    @Autowired
    public UserController(UserService userService, FilmService filmService) {
        this.userService = userService;
        this.filmService = filmService;
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.debug("Creating user [{}]", userDto);
        return userService.create(userDto);
    }

    @PutMapping
    public UserDto update(@Valid @RequestBody UserDto userDto) {
        log.debug("Updating user [{}]", userDto);
        return userService.update(userDto);
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) {
        log.debug("Adding friends [{}] - [{}]", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable long id, @PathVariable long friendId) {
        log.debug("Removing friends [{}] - [{}]", id, friendId);
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<UserDto> getFriends(@PathVariable long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{friendId}")
    public List<UserDto> getCommonFriends(@PathVariable long id, @PathVariable long friendId) {
        return userService.getCommonFriends(id, friendId);
    }

    @GetMapping("/{id}/feed")
    public List<EventDto> getEvent(@PathVariable long id) {
        log.debug("Retrieving event feed for user - [{}]", id);
        return userService.getEvent(id);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable long id) {
        log.debug("Retrieving user with id - [{}]", id);
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable long id) {
        log.debug("Deleting user with id [{}]", id);
        userService.deleteUserById(id);
    }

    @GetMapping("/{id}/recommendations")
    public List<FilmDto> getFilmRecommendations(@PathVariable long id) {
        log.debug("Received request to retrieve film recommendations for user [{}]", id);
        List<FilmDto> recommendationDtos = filmService.getFilmRecommendations(id);
        log.debug("Number of recommended films for user [{}]: {}", id, recommendationDtos.size());
        return recommendationDtos;
    }
}
