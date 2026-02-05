package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<DirectorDto> getAll() {
        log.debug("Received request to retrieve the list of all directors");
        return directorService.getAll();
    }

    @GetMapping("/{id}")
    public DirectorDto getById(@PathVariable("id") long id) {
        log.debug("Received request to find director by id: [{}]", id);
        return directorService.getById(id);
    }

    @PostMapping
    public DirectorDto create(@Valid @RequestBody DirectorDto directorDto) {
        log.debug("Received request to create director: [{}]", directorDto);
        return directorService.create(directorDto);
    }

    @PutMapping
    public DirectorDto update(@Valid @RequestBody DirectorDto newDirectorDto) {
        log.debug("Received request to update director: [{}]", newDirectorDto);
        return directorService.update(newDirectorDto);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable("id") long id) {
        log.debug("Received request to delete director by id: [{}]", id);
        directorService.deleteById(id);
    }
}
