package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.service.mapper.RatingMpaMapper;
import ru.yandex.practicum.filmorate.storage.RatingMpaStorage;

import java.util.List;

@Service
public class RatingMpaService {

    private final RatingMpaStorage ratingMpaStorage;

    public RatingMpaService(@Qualifier("ratingMpaDbStorage") RatingMpaStorage ratingMpaStorage) {
        this.ratingMpaStorage = ratingMpaStorage;
    }

    public List<MpaDto> getAllMpa() {
        return ratingMpaStorage.getAll().stream().map(RatingMpaMapper::mapToMpaDto).toList();
    }

    public MpaDto getMpaById(int id) {
        RatingMpa ratingMpa = ratingMpaStorage.findByRatingMpaId(id);
        if (ratingMpa == null) {
            throw new NotFoundException("MPA rating not found");
        }
        return RatingMpaMapper.mapToMpaDto(ratingMpa);
    }
}
