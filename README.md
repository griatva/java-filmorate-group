# Filmorate — Social Movie Ratings API (Team Project)

A backend REST API for a social movie app. Users can like and review films, add friends, follow activity feeds, search, and get recommendations.

Built as a team project to practice Java + SQL, REST architecture, and clean layered design.

## Key Features
- Films & Users: full CRUD with validation
- Likes and Popular films (with filters by genre and year)
- Friends and mutual friends
- Reviews with usefulness ranking
- Search by film title or director
- Film recommendations based on similar likes
- Activity feed: likes, reviews, friend events
- Directors support: film-director relation and filtering
- Genres and MPA ratings as reference data

## My Contributions
- Popular films by genre and year: `GET /films/popular?count=&genreId=&year=`
- Film-director relation: assign directors to films
- Director endpoints with sorting: `GET /films/director/{id}?sortBy=likes|year`
- Full CRUD for directors

## Database Schema (H2)
![Схема базы данных Filmorate](filmorate.png)
- `users(user_id, email, login, name, birthday)`
- `film(film_id, name, description, release_date, duration, rating_mpa_id)`
- `rating_mpa(rating_mpa_id, name, description)`
- `genre(genre_id, name)` · `film_genre(film_id, genre_id)`
- `film_user_like(film_id, user_id)`
- `directors(director_id, name)` · `film_director(film_id, director_id)`
- `review(review_id, content, is_positive, user_id, film_id, useful)`
- `review_like(review_id, user_id, is_like)`
- `friendship(from_user_id, to_user_id, is_confirmed)`
- `feeds(event_id, user_id, timestamp, event_type, operation, entity_id)`

## Full API Endpoints

###  Films
- `GET /films` — Get all films
- `GET /films/{id}` — Get film by ID
- `POST /films` — Add new film
- `PUT /films` — Update film
- `DELETE /films/{id}` — Delete film
- `PUT /films/{id}/like/{userId}` — Like a film
- `DELETE /films/{id}/like/{userId}` — Remove like
- `GET /films/popular` — Get most popular films (optional filters: `count`, `genreId`, `year`)
- `GET /films/search?query={text}&by=title,director` — Search by title/director
- `GET /films/director/{directorId}?sortBy=likes|year` — Get director’s films sorted
- `GET /films/common` - get common films for 2 users

###  Users
- `GET /users` — Get all users
- `GET /users/{id}` — Get user by ID
- `POST /users` — Add new user
- `PUT /users` — Update user
- `DELETE /users/{id}` — Delete user
- `PUT /users/{id}/friends/{friendId}` — Add friend
- `DELETE /users/{id}/friends/{friendId}` — Remove friend
- `GET /users/{id}/friends` — Get user’s friends
- `GET /users/{id}/friends/common/{otherId}` — Mutual friends
- `GET /users/{id}/feed` — User activity feed
- `GET /users/{id}/recommendations` — Film recommendations

###  Reviews
- `GET /reviews` — Get all reviews (optional: `filmId`)
- `GET /reviews/{id}` — Get review by ID
- `POST /reviews` — Add review
- `PUT /reviews` — Update review
- `DELETE /reviews/{id}` — Delete review
- `PUT /reviews/{id}/like/{userId}` — Like review
- `PUT /reviews/{id}/dislike/{userId}` — Dislike review
- `DELETE /reviews/{id}/like/{userId}` — Remove like
- `DELETE /reviews/{id}/dislike/{userId}` — Remove dislike

###  Directors
- `GET /directors` — Get all directors
- `GET /directors/{id}` — Get director by ID
- `POST /directors` — Create director
- `PUT /directors` — Update director
- `DELETE /directors/{id}` — Delete director

### Genres & MPA Ratings
- `GET /genres` — List all genres
- `GET /genres/{id}` — Get genre by ID
- `GET /mpa` — List all MPA ratings
- `GET /mpa/{id}` — Get MPA rating by ID


__________

## Tech Stack
- Java 17, Spring Boot (Web, Validation)
- SQL: H2 (file-based), JdbcTemplate, schema.sql / data.sql
- Maven, Lombok, Logbook (HTTP logs)

## Project Structure
```
src/main/java/ru/yandex/practicum/filmorate/
├── annotations/            # Project-wide annotations (validation/qualifiers used across layers)
├── controller/             # REST controllers (HTTP endpoints)
├── dto/                    # Data Transfer Objects (request/response models)
├── enums/                  # Enumerations and constants
├── exception/              # Custom exceptions and global error handling
├── model/                  # Domain models
├── service/                # Business logic services
└── storage/                # Data access layer
    └── impl/
        └── h2/             # JdbcTemplate-based repository implementations for H2
            └── mappers/    # RowMapper classes

src/main/resources/
├── application.properties  # App configuration (port, H2, logging)
├── schema.sql              # DDL: tables/relations
└── data.sql                # Seed data

src/test/java/
├── ru/yandex/practicum/filmorate/FilmorateApplicationTests.java
├── ru/yandex/practicum/filmorate/controller/FilmControllerTest.java
└── ru/yandex/practicum/filmorate/controller/UserControllerTest.java
```


H2 console: http://localhost:8080/h2-console  
JDBC: jdbc:h2:file:./db/filmorate · user: sa · password: password

_____

## Run application

```bash
mvn spring-boot:run
```
_____

### Repository & Contributors

This project was developed as part of a team assignment at Practicum.

Original team repository: https://github.com/Serminos/java-filmorate

Team Members:

| Name         | GitHub                                                   |
|--------------| -------------------------------------------------------- |
| griatva (me) | https://github.com/griatva |
| Serminos     | https://github.com/Serminos    |
| NikolayChak  | https://github.com/NikolayChak   |
| naviwe       | https://github.com/naviwe    |
