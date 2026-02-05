package ru.yandex.practicum.filmorate.enums;

public enum SortBy {
    YEAR,
    LIKES;

    public static SortBy fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Sorting can only be performed by the following parameters: year or likes");
        }

        try {
            return SortBy.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Sorting can only be performed by the following parameters: year or likes");
        }
    }
}