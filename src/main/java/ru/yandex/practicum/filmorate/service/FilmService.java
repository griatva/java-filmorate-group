package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.enums.SortBy;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.service.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.service.mapper.RatingMpaMapper;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FilmGenreStorage filmGenreStorage;
    private final FilmUserLikeStorage filmUserLikeStorage;
    private final EventStorage eventStorage;
    private final Map<Long, RatingMpa> cacheRatingMpa;
    private final Map<Long, Genre> cacheGenre;
    private final DirectorStorage directorStorage;
    private final FilmDirectorStorage filmDirectorStorage;
    public static final Integer FILM_BIRTHDAY_YEAR = 1895;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("filmGenreDbStorage") FilmGenreStorage filmGenreStorage,
                       @Qualifier("filmUserLikeDbStorage") FilmUserLikeStorage filmUserLikeStorage,
                       @Qualifier("eventDbStorage") EventStorage eventStorage,
                       @Qualifier("genreDbStorage") GenreStorage genreStorage,
                       @Qualifier("ratingMpaDbStorage") RatingMpaStorage ratingMpaStorage,
                       @Qualifier("directorDbStorage") DirectorStorage directorStorage,
                       @Qualifier("filmDirectorDbStorage") FilmDirectorStorage filmDirectorStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.filmGenreStorage = filmGenreStorage;
        this.filmUserLikeStorage = filmUserLikeStorage;
        this.eventStorage = eventStorage;
        this.cacheRatingMpa = ratingMpaStorage.getAll().stream()
                .collect(Collectors.toMap(RatingMpa::getRatingMpaId, Function.identity()));
        this.cacheGenre = genreStorage.getAll().stream()
                .collect(Collectors.toMap(Genre::getGenreId, Function.identity()));
        this.directorStorage = directorStorage;
        this.filmDirectorStorage = filmDirectorStorage;
    }

    private void checkRatingMpaAndGenresFilmDto(FilmDto filmDto) {
        if (filmDto.getMpa() != null && filmDto.getMpa().getId() != null) {
            if (cacheRatingMpa.get(filmDto.getMpa().getId()) == null) {
                throw new BadRequestException("Specified MPA rating ID was not found: " +
                        "[{" + filmDto.getMpa().getId() + "}]");
            }
            cacheRatingMpa.get(filmDto.getMpa().getId());
        }
        if (filmDto.getGenres() != null) {
            for (GenreDto genreDto : filmDto.getGenres()) {
                if (cacheGenre.get(genreDto.getId()) == null) {
                    throw new BadRequestException("Specified genre ID was not found: " +
                            "[{" + genreDto.getId() + "}]");
                }
            }
        }
    }

    private void checkDirectorsExist(FilmDto filmDto) {
        Set<Long> directorIdSet = filmDto.getDirectors().stream()
                .map(DirectorDto::getId)
                .collect(Collectors.toSet());

        directorStorage.existsByDirectorIdIn(directorIdSet);
    }

    private Set<DirectorDto> getDirectorDtoByFilmId(long id) {
        Set<Director> directors = directorStorage.findByFilmId(id);
        if (directors == null) {
            return Collections.emptySet();
        } else {
            return directors.stream()
                    .map(DirectorMapper::mapToDirectorDto)
                    .collect(Collectors.toSet());
        }
    }

    public FilmDto create(FilmDto filmDto) {
        checkRatingMpaAndGenresFilmDto(filmDto);
        if (filmDto.getDirectors() != null && !filmDto.getDirectors().isEmpty()) {
            checkDirectorsExist(filmDto);
        }
        Film film = FilmMapper.mapToFilm(filmDto);
        film = filmStorage.create(film);

        FilmDto filmDtoResponse = FilmMapper.mapToFilmDto(film);
        filmDtoResponse.setGenres(updateFilmGenre(film, filmDto.getGenres()));

        if (film.getRatingMpaId() != null) {
            RatingMpa ratingMpa = cacheRatingMpa.get(film.getRatingMpaId());
            filmDtoResponse.setMpa(RatingMpaMapper.mapToMpaDto(ratingMpa));
        }
        if (filmDto.getDirectors() != null && !filmDto.getDirectors().isEmpty()) {
            filmDirectorStorage.create(film.getId(), filmDto.getDirectors());
            filmDtoResponse.setDirectors(getDirectorDtoByFilmId(film.getId()));
        }

        return filmDtoResponse;
    }

    public FilmDto update(FilmDto filmDto) {
        checkFilmExists(filmDto.getId());
        checkRatingMpaAndGenresFilmDto(filmDto);
        if (filmDto.getDirectors() != null && !filmDto.getDirectors().isEmpty()) {
            checkDirectorsExist(filmDto);
        }
        Film film = FilmMapper.mapToFilm(filmDto);
        film = filmStorage.update(film);

        FilmDto filmDtoResponse = FilmMapper.mapToFilmDto(film);
        filmDtoResponse.setGenres(updateFilmGenre(film, filmDto.getGenres()));

        if (film.getRatingMpaId() != null) {
            RatingMpa ratingMpa = cacheRatingMpa.get(film.getRatingMpaId());
            filmDtoResponse.setMpa(RatingMpaMapper.mapToMpaDto(ratingMpa));
        }
        if (filmDto.getDirectors() != null && !filmDto.getDirectors().isEmpty()) {
            filmDirectorStorage.deleteByFilmId(film.getId());
            filmDirectorStorage.create(film.getId(), filmDto.getDirectors());
            filmDtoResponse.setDirectors(getDirectorDtoByFilmId(film.getId()));
        } else {
            filmDirectorStorage.deleteByFilmId(film.getId());
        }

        return filmDtoResponse;
    }

    private List<GenreDto> updateFilmGenre(Film film, List<GenreDto> genreDtos) {
        List<GenreDto> genreDtosUpdated = new ArrayList<>();
        if (genreDtos == null) return genreDtosUpdated;
        filmGenreStorage.deleteByFilmId(film.getId());
        Set<Long> genreIds = genreDtos.stream()
                .map(GenreDto::getId)
                .collect(Collectors.toSet());
        for (Long genreId : genreIds) {
            filmGenreStorage.add(film.getId(), genreId);
        }
        genreDtosUpdated = filmGenreStorage.findByFilmId(film.getId()).stream()
                .map(item -> new GenreDto(item.getGenreId(), cacheGenre.get(item.getGenreId()).getName()))
                .collect(Collectors.toList());
        return genreDtosUpdated;
    }

    public List<FilmDto> getAll() {
        List<Film> films = filmStorage.getAll();
        return mapFilmsToFilmDtosAndAddDopInfo(films);
    }

    public FilmDto getById(long filmId) {
        checkFilmExists(filmId);
        Film film = filmStorage.findByFilmId(filmId);
        return mapFilmsToFilmDtosAndAddDopInfo(Collections.singletonList(film)).get(0);
    }

    private void checkFilmExists(long filmId) {
        if (filmStorage.findByFilmId(filmId) == null) {
            throw new NotFoundException("Film was not found");
        }
    }

    private void checkUserExists(long userId) {
        if (userStorage.findByUserId(userId) == null) {
            throw new NotFoundException("User was not found");
        }
    }

    public void addLike(long filmId, long userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);
        filmUserLikeStorage.add(filmId, userId);
        eventStorage.create(userId, EventType.LIKE, Operation.ADD, filmId);
    }

    public void deleteLike(long filmId, long userId) {
        checkFilmExists(filmId);
        checkUserExists(userId);
        filmUserLikeStorage.delete(filmId, userId);
        eventStorage.create(userId, EventType.LIKE, Operation.REMOVE, filmId);
    }

    public void clear() {
        filmUserLikeStorage.clear();
        filmStorage.clear();
    }

    private void checkSearchParams(Map<String, Long> params) {
        if (params.get("genreId") != null && cacheGenre.get(params.get("genreId")) == null) {
            throw new BadRequestException("Specified genre ID was not found: " +
                    "[{" + params.get("genreId") + "}]");
        }

        if (params.get("year") != null &&
                (params.get("year") < FILM_BIRTHDAY_YEAR || params.get("year") > LocalDate.now().getYear())) {
            throw new BadRequestException("Film release year must be no earlier than 1895 and no later than " +
                    LocalDate.now().getYear() + ".");
        }
    }

    private List<Long> findFilmsIdByParamsWithAndCondition(Map<String, Long> params, Long limit) {
        List<Long> filteredFilmsId = new ArrayList<>();

        boolean firstIteration = true;
        for (Map.Entry<String, Long> entry : params.entrySet()) {
            List<Long> foundFilmsId = new ArrayList<>();

            if (entry.getKey().equals("genreId")) {
                foundFilmsId = filmGenreStorage.findFilmsIdByGenreId(params.get("genreId"));
            }
            if (entry.getKey().equals("year")) {
                foundFilmsId = filmStorage.findFilmsIdByYear(params.get("year").intValue());
            }

            if (firstIteration) {
                filteredFilmsId.addAll(foundFilmsId);
                firstIteration = false;
            } else {
                filteredFilmsId.retainAll(foundFilmsId);
            }
        }

        return filteredFilmsId;
    }

    public List<FilmDto> getPopularFilmsByParams(Map<String, Long> params, Long limit) {
        checkSearchParams(params);
        List<Long> filteredFilmsId;
        if (params.size() == 0) {
            filteredFilmsId = filmStorage.findPopularFilmsIdWithLimit(limit);
        } else {
            filteredFilmsId = findFilmsIdByParamsWithAndCondition(params, limit);
        }
        List<Film> popularFilms = filmStorage.findPopularByFilmIdIn(filteredFilmsId, limit);
        return mapFilmsToFilmDtosAndAddDopInfo(popularFilms);
    }

    private List<FilmDto> mapFilmsToFilmDtosAndAddDopInfo(List<Film> films) {
        List<FilmDto> filmDtos = new ArrayList<>();
        if (films == null) return filmDtos;
        for (Film film : films) {
            RatingMpa ratingMpa = new RatingMpa();
            if (film.getRatingMpaId() != null) {
                ratingMpa = cacheRatingMpa.get(film.getRatingMpaId());
            }
            List<GenreDto> genreDtos = filmGenreStorage.findByFilmId(film.getId()).stream()
                    .map(item -> new GenreDto(item.getGenreId(), cacheGenre.get(item.getGenreId()).getName()))
                    .collect(Collectors.toList());
            FilmDto filmDto = FilmMapper.mapToFilmDto(film);
            filmDto.setGenres(genreDtos);
            filmDto.setMpa(RatingMpaMapper.mapToMpaDto(ratingMpa));
            filmDto.setDirectors(getDirectorDtoByFilmId(film.getId()));
            filmDtos.add(filmDto);
        }
        return filmDtos;
    }

    public List<FilmDto> getCommonFilms(long userId, long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);

        List<Long> userLikedFilmIds = filmUserLikeStorage.findByUserId(userId)
                .stream()
                .map(FilmUserLike::getFilmId)
                .collect(Collectors.toList());

        List<Long> friendLikedFilmIds = filmUserLikeStorage.findByUserId(friendId)
                .stream()
                .map(FilmUserLike::getFilmId)
                .collect(Collectors.toList());

        userLikedFilmIds.retainAll(friendLikedFilmIds);

        if (userLikedFilmIds.isEmpty()) {
            return List.of();
        }

        List<Film> films = filmStorage.findByFilmIdIn(userLikedFilmIds);

        return mapFilmsToFilmDtosAndAddDopInfo(films);
    }

    public List<FilmDto> getFilmsByDirectorIdWithSort(long directorId, SortBy sortBy) {
        Integer count = directorStorage.existsByDirectorId(directorId);
        if (count == null || count == 0) {
            throw new NotFoundException("Director with id = " + directorId + " was not found");
        }
        List<Film> filmsByDirector = filmStorage.findByDirectorIdWithSort(directorId, sortBy);
        return mapFilmsToFilmDtosAndAddDopInfo(filmsByDirector);
    }


    public List<FilmDto> getSearch(String query, List<String> by) {
        Set<Long> filmIds = new HashSet<>();
        if (by.contains("title")) {
            List<Film> films = filmStorage.findByNameContainingIgnoreCase(query);
            if (!films.isEmpty()) {
                filmIds.addAll(films.stream().map(Film::getId).collect(Collectors.toSet()));
            }
        }
        if (by.contains("director")) {
            List<Director> directors = directorStorage.findByNameContainingIgnoreCase(query);
            if (!directors.isEmpty()) {
                List<Long> directorsIds = directors.stream().map(Director::getId).toList();
                List<FilmDirector> filmDirector = filmDirectorStorage.findByDirectorIdIn(directorsIds.stream().toList());
                if (!filmDirector.isEmpty()) {
                    filmIds.addAll(filmDirector.stream().map(FilmDirector::getFilmId).toList());
                }
            }
        }
        return mapFilmsToFilmDtosAndAddDopInfo(filmStorage.findPopularByFilmIdIn(filmIds.stream().toList(), 0));
    }

    public List<FilmDto> getFilmRecommendations(long userId) {
        checkUserExists(userId);

        Set<Long> currentUserLikesFilmIds = getCurrentUserLikes(userId);
        List<Long> otherUsers = getIntersectFilmsWithOtherUsersLikesIds(userId, currentUserLikesFilmIds);
        Long mostSimilarUserId = findMostSimilarUser(currentUserLikesFilmIds, otherUsers);

        if (mostSimilarUserId == null) {
            return List.of();
        }

        Set<Long> recommendedFilmIds = getRecommendedFilmIds(currentUserLikesFilmIds, mostSimilarUserId);
        return mapFilmsToFilmDtosAndAddDopInfo(filmStorage.findByFilmIdIn(recommendedFilmIds.stream().toList()));
    }

    private Set<Long> getCurrentUserLikes(long userId) {
        Set<Long> currentUserLikes = filmUserLikeStorage.findFilmsIdByUserId(userId);
        log.debug("User with ID [{}] liked films with IDs: {}", userId, currentUserLikes);
        return currentUserLikes;
    }

    private List<Long> getIntersectFilmsWithOtherUsersLikesIds(long userId, Set<Long> currentUserLikesFilmIds) {
        List<Long> otherUserIds = filmUserLikeStorage
                .findUsersIdIntersectByFilmsLikesWithUserByUserId(userId, currentUserLikesFilmIds)
                .stream()
                .toList();
        log.debug("Other users' IDs: {}", otherUserIds);
        return otherUserIds;
    }

    private Long findMostSimilarUser(Set<Long> currentUserLikes, List<Long> otherUsers) {
        Long mostSimilarUserId = null;
        long maxCommonLikes = 0L;

        for (Long otherUserId : otherUsers) {
            Set<Long> otherUserLikes = filmUserLikeStorage.findFilmsIdByUserId(otherUserId);
            Long commonLikes = calculateCommonLikes(currentUserLikes, otherUserLikes);

            if (commonLikes > maxCommonLikes) {
                maxCommonLikes = commonLikes;
                mostSimilarUserId = otherUserId;
            }
        }

        log.debug("ID of the user most similar by likes: [{}]", mostSimilarUserId);
        return mostSimilarUserId;
    }

    private Long calculateCommonLikes(Set<Long> currentUserLikes, Set<Long> otherUserLikes) {
        Long commonLikes = currentUserLikes.stream()
                .filter(otherUserLikes::contains)
                .count();
        log.debug("Common likes: {}", commonLikes);
        return commonLikes;
    }

    private Set<Long> getRecommendedFilmIds(Set<Long> currentUserLikes, Long mostSimilarUserId) {
        Set<Long> similarUserLikes = filmUserLikeStorage.findFilmsIdByUserId(mostSimilarUserId);
        Set<Long> recommendedFilmIds = new HashSet<>(similarUserLikes);
        recommendedFilmIds.removeAll(currentUserLikes);
        log.debug("Recommended films (IDs): {}", recommendedFilmIds);
        return recommendedFilmIds;
    }

    public void deleteById(long filmId) {
        filmUserLikeStorage.deleteByFilmId(filmId);
        filmGenreStorage.deleteByFilmId(filmId);
        filmStorage.deleteByFilmId(filmId);
    }
}