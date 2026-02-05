package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;


@RestController
@RequestMapping(value = "/reviews")
@Validated
public class ReviewController {
    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ReviewDto create(@Valid @RequestBody ReviewDto reviewDto) {
        log.debug("Creating review [{}]", reviewDto);
        return reviewService.create(reviewDto);
    }

    @PutMapping
    public ReviewDto update(@Valid @RequestBody ReviewDto reviewDto) {
        log.debug("Updating review [{}]", reviewDto);
        return reviewService.update(reviewDto);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        log.debug("Deleting review with id=[{}]", id);
        reviewService.deleteById(id);
    }

    @GetMapping("/{id}")
    public ReviewDto getById(@PathVariable Long id) {
        log.debug("Retrieving review with id=[{}]", id);
        return reviewService.findById(id);
    }

    @GetMapping
    public List<ReviewDto> getReviewsWithParam(@RequestParam(required = false) Long filmId,
                                               @RequestParam(defaultValue = "10") Long count) {
        if (filmId == null) {
            log.debug("Retrieving all popular reviews with count=[{}]", count);
            return reviewService.all(count);
        } else {
            log.debug("Retrieving popular reviews for film with id=[{}], count=[{}]", filmId, count);
            return reviewService.findByFilmId(filmId, count);
        }
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        log.debug("Adding like to review [{}] by user [{}]", id, userId);
        reviewService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable long id, @PathVariable long userId) {
        log.debug("Removing like from review [{}] by user [{}]", id, userId);
        reviewService.deleteLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable long id, @PathVariable long userId) {
        log.debug("Adding dislike to review [{}] by user [{}]", id, userId);
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable long id, @PathVariable long userId) {
        log.debug("Removing dislike from review [{}] by user [{}]", id, userId);
        reviewService.deleteDislike(id, userId);
    }
}
