package ru.yandex.practicum.filmorate.storage.impl.h2;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.impl.h2.mappers.DirectorRowMapper;

import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Repository
@AllArgsConstructor
@Qualifier("directorDbStorage")
public class DirectorDbStorage implements DirectorStorage {

    private JdbcTemplate jdbcTemplate;
    private DirectorRowMapper directorRowMapper;

    private static final String GET_ALL_DIRECTORS = " SELECT * from directors; ";
    private static final String GET_DIRECTOR_BY_ID = " SELECT * FROM directors WHERE director_id = ?; ";
    private static final String CREATE_DIRECTOR = " INSERT INTO directors (name) VALUES (?); ";
    private static final String UPDATE_DIRECTOR = " UPDATE directors SET name = ? WHERE director_id = ?; ";
    private static final String DELETE_DIRECTOR_BY_ID = " DELETE FROM directors WHERE director_id = ?; ";
    private static final String GET_DIRECTORS_BY_FILM_ID = """
            SELECT d.*
            FROM directors d
            JOIN film_director fd ON d.director_id = fd.director_id
            WHERE fd.film_id = ?
            """;
    private static final String CHECK_DIRECTOR_EXISTS = """
            SELECT COUNT(*)
            FROM directors
            WHERE director_id = ?;
            """;
    private static final String FIND_BY_NAME = """
            SELECT *
            FROM DIRECTORS
            WHERE lower(NAME) like '%'||lower(?)||'%'
            """;


    @Override
    public List<Director> getAll() {
        return jdbcTemplate.query(GET_ALL_DIRECTORS, directorRowMapper);
    }

    @Override
    public Director findById(long directorId) {
        return jdbcTemplate.queryForObject(GET_DIRECTOR_BY_ID, directorRowMapper, directorId);

    }

    @Override
    public Director create(Director director) {
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_DIRECTOR, PreparedStatement.RETURN_GENERATED_KEYS);

            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        final Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("director_id")) {
            final Long generatedId = (Long) keys.get("director_id");
            director.setId(generatedId);
        } else {
            throw new RuntimeException("Failed to retrieve the generated ID for the director");
        }
        log.trace("Director creation completed: [{}] - assigned id: [{}]", director, director.getId());
        return director;
    }

    @Override
    public Director update(Director newDirector) {
        long newDirectorId = newDirector.getId();
        jdbcTemplate.update(UPDATE_DIRECTOR, newDirector.getName(), newDirectorId);

        final Director updatedDirector = findById(newDirectorId);
        log.trace("Director update completed: [{}]", updatedDirector);
        return updatedDirector;
    }

    @Override
    public Integer deleteByDirectorId(long directorId) {
        return jdbcTemplate.update(DELETE_DIRECTOR_BY_ID, directorId);
    }

    @Override
    public Set<Director> findByFilmId(long filmId) {
        log.trace("Retrieving director(s) for film with id: [{}]", filmId);
        Set<Director> directors = new HashSet<>(jdbcTemplate.query(GET_DIRECTORS_BY_FILM_ID, directorRowMapper, filmId));
        log.trace("Director list for film with id [{}] prepared. Found [{}] director(s)", filmId, directors.size());
        return directors;
    }


    @Override
    public void existsByDirectorIdIn(Set<Long> directorIdSet) {
        for (Long directorId : directorIdSet) {
            Integer count = jdbcTemplate.queryForObject(CHECK_DIRECTOR_EXISTS, Integer.class, directorId);
            if (count == null || count == 0) {
                throw new NotFoundException("Director with id = " + directorId + " was not found");
            }
        }
    }

    @Override
    public Integer existsByDirectorId(long directorId) {
        return jdbcTemplate.queryForObject(CHECK_DIRECTOR_EXISTS, Integer.class, directorId);
    }

    @Override
    public List<Director> findByNameContainingIgnoreCase(String query) {
        return jdbcTemplate.query(FIND_BY_NAME, directorRowMapper, query);
    }
}