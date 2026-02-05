package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Validated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    Long id;
    @Email(message = "Email address must be valid and contain the '@' symbol")
    @NotNull(message = "Email address must not be null")
    String email;
    @NotBlank(message = "Login must not be empty")
    @Pattern(regexp = "[^ ]+", message = "Login must not contain spaces")
    String login;
    String name;
    @PastOrPresent(message = "Birthday must not be in the future")
    LocalDate birthday;
    Set<Long> friends = new HashSet<>();

    public UserDto(Long id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        this.friends = new HashSet<>();
    }

    public void setFriendsIds(Set<Long> friendsIds) {
        this.friends = friendsIds != null ? friendsIds : new HashSet<>();
    }
}
