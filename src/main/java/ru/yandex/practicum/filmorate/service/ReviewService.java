package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewLike;
import ru.yandex.practicum.filmorate.service.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.ReviewLikeStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final UserStorage userStorage;
    private final FilmService filmService;
    private final ReviewStorage reviewStorage;
    private final ReviewLikeStorage reviewLikeStorage;
    private final EventStorage eventStorage;

    @Autowired
    public ReviewService(@Qualifier("userDbStorage") UserStorage userStorage,
                         FilmService filmService,
                         @Qualifier("reviewDbStorage") ReviewStorage reviewStorage,
                         @Qualifier("reviewLikeDbStorage") ReviewLikeStorage reviewLikeStorage,
                         @Qualifier("eventDbStorage") EventStorage eventStorage) {
        this.userStorage = userStorage;
        this.filmService = filmService;
        this.reviewStorage = reviewStorage;
        this.reviewLikeStorage = reviewLikeStorage;
        this.eventStorage = eventStorage;
    }

    public ReviewDto create(ReviewDto reviewDto) {
        checkReview(reviewDto);
        reviewDto.setUseful(0);
        Review review = reviewStorage.create(ReviewMapper.mapToReview(reviewDto));
        eventStorage.create(reviewDto.getUserId(), EventType.REVIEW, Operation.ADD, review.getId());
        return ReviewMapper.mapToReviewDto(review);
    }

    private void checkReviewExists(long reviewId) {
        if (reviewStorage.findByReviewId(reviewId) == null) {
            throw new NotFoundException("Review with ID [" + reviewId + "] was not found");
        }
    }

    private void checkReview(ReviewDto reviewDto) {
        if (userStorage.findByUserId(reviewDto.getUserId()) == null) {
            throw new NotFoundException("User with ID [" + reviewDto.getUserId() + "] was not found");
        }
        if (filmService.getById(reviewDto.getFilmId()) == null) {
            throw new NotFoundException("Film with ID [" + reviewDto.getFilmId() + "] was not found");
        }
        if (reviewDto.getContent() == null || reviewDto.getContent().isBlank()) {
            throw new BadRequestException("Review content must not be empty");
        }
        if (reviewDto.getIsPositive() == null) {
            throw new BadRequestException("Review type must be specified (positive or negative)");
        }
    }

    public ReviewDto update(ReviewDto reviewDto) {
        checkReviewExists(reviewDto.getReviewId());
        Review review = reviewStorage.findByReviewId(reviewDto.getReviewId());
        review.setContent(reviewDto.getContent());
        review.setIsPositive(reviewDto.getIsPositive());
        review.setUseful(calculateUsefulByReviewId(review.getId()));
        reviewStorage.update(review);
        eventStorage.create(review.getUserId(), EventType.REVIEW, Operation.UPDATE, review.getId());
        return ReviewMapper.mapToReviewDto(review);
    }

    public void updateReviewUseful(long reviewId) {
        Review review = reviewStorage.findByReviewId(reviewId);
        review.setUseful(calculateUsefulByReviewId(review.getId()));
        reviewStorage.update(review);
    }

    public void deleteById(long reviewId) {
        Review review = reviewStorage.findByReviewId(reviewId);
        if (review == null) {
            throw new NotFoundException("Review not found");
        }
        reviewStorage.deleteById(reviewId);
        eventStorage.create(review.getUserId(), EventType.REVIEW, Operation.REMOVE, reviewId);
    }

    public void addLike(long reviewId, long userId) {
        ReviewLike reviewLike = new ReviewLike(reviewId, userId, true);
        reviewLikeStorage.create(reviewLike);
        updateReviewUseful(reviewId);
    }

    public void addDislike(long reviewId, long userId) {
        ReviewLike reviewDislike = new ReviewLike(reviewId, userId, false);
        reviewLikeStorage.create(reviewDislike);
        updateReviewUseful(reviewId);
    }

    public void deleteLike(long reviewId, long userId) {
        reviewLikeStorage.deleteByReviewIdAndUserId(reviewId, userId);
        updateReviewUseful(reviewId);
    }

    public void deleteDislike(long reviewId, long userId) {
        reviewLikeStorage.deleteByReviewIdAndUserId(reviewId, userId);
        updateReviewUseful(reviewId);
    }

    public ReviewDto findById(long reviewId) {
        Review review = reviewStorage.findByReviewId(reviewId);
        if (review == null) {
            throw new NotFoundException("Review not found");
        }
        return ReviewMapper.mapToReviewDto(review);
    }

    private Integer calculateUsefulByReviewId(long reviewId) {
        List<ReviewLike> reviewLikes = reviewLikeStorage.findByReviewId(reviewId);

        Map<Long, Integer> userVotes = new HashMap<>();
        for (ReviewLike reviewLike : reviewLikes) {
            userVotes.merge(reviewLike.getUserId(), reviewLike.getIsLike() ? 1 : -1, Integer::sum);
        }

        return userVotes.values().stream()
                .mapToInt(likeCount -> {
                    if (likeCount > 0) return 1;
                    if (likeCount < 0) return -1;
                    return 0;
                })
                .sum();
    }

    public List<ReviewDto> findByFilmId(Long filmId, Long limit) {
        List<Review> reviews = reviewStorage.findByFilmId(filmId, limit);
        if (reviews.isEmpty()) {
            return new ArrayList<>();
        }
        return reviews.stream().map(ReviewMapper::mapToReviewDto).collect(Collectors.toList());
    }

    public List<ReviewDto> all(long limit) {
        List<Review> reviews = reviewStorage.getAll(limit);
        if (reviews.isEmpty()) {
            return new ArrayList<>();
        }
        return reviews.stream().map(ReviewMapper::mapToReviewDto).collect(Collectors.toList());
    }

    public void clear() {
        reviewLikeStorage.clear();
        reviewStorage.clear();
    }
}
