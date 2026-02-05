package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.mapper.EventMapper;
import ru.yandex.practicum.filmorate.service.mapper.UserMapper;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.FilmUserLikeStorage;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;
    private final EventStorage eventStorage;
    private final FilmUserLikeStorage filmUserLikeStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("friendshipDbStorage") FriendshipStorage friendshipStorage,
                       @Qualifier("eventDbStorage") EventStorage eventStorage,
                       @Qualifier("filmUserLikeDbStorage") FilmUserLikeStorage filmUserLikeStorage) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
        this.eventStorage = eventStorage;
        this.filmUserLikeStorage = filmUserLikeStorage;
    }

    public UserDto create(UserDto user) {
        return UserMapper.mapToUserDto(userStorage.create(UserMapper.mapToUser(user)));
    }

    private void checkUserExists(long userId) {
        if (userStorage.findByUserId(userId) == null) {
            throw new NotFoundException("User with ID [" + userId + "] was not found");
        }
    }

    public UserDto update(UserDto user) {
        checkUserExists(user.getId());
        return UserMapper.mapToUserDto(userStorage.update(UserMapper.mapToUser(user)));
    }

    public List<UserDto> getAll() {
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : userStorage.getAll()) {
            userDtos.add(UserMapper.mapToUserDto(user));
        }
        return userDtos;
    }

    public void clear() {
        friendshipStorage.clear();
        userStorage.clear();
    }

    public void addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new NotFoundException("Cannot add yourself as a friend");
        }
        checkUserExists(userId);
        checkUserExists(friendId);
        Friendship friendship = new Friendship(userId, friendId, false);
        friendshipStorage.add(friendship);
        eventStorage.create(userId, EventType.FRIEND, Operation.ADD, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new NotFoundException("Cannot remove yourself from friends");
        }
        checkUserExists(userId);
        checkUserExists(friendId);
        Friendship friendship = new Friendship(userId, friendId, false);
        friendshipStorage.delete(friendship);
        eventStorage.create(userId, EventType.FRIEND, Operation.REMOVE, friendId);
    }

    public List<UserDto> getCommonFriends(long userId, long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        List<Long> commonFriendsIds = friendshipStorage.findCommonFriendId(userId, friendId);
        List<UserDto> commonFriends = new ArrayList<>();
        for (Long commonFriendId : commonFriendsIds) {
            commonFriends.add(UserMapper.mapToUserDto(userStorage.findByUserId(commonFriendId)));
        }
        return commonFriends;
    }

    public List<UserDto> getFriends(long userId) {
        checkUserExists(userId);
        List<UserDto> friends = new ArrayList<>();
        for (Friendship friendship : friendshipStorage.findByFromUserId(userId)) {
            friends.add(UserMapper.mapToUserDto(userStorage.findByUserId(friendship.getToUserId())));
        }
        return friends;
    }

    public List<EventDto> getEvent(long userId) {
        checkUserExists(userId);
        List<Event> events = eventStorage.findUserEventsById(userId);
        return events.stream()
                .map(EventMapper::mapToEventDto)
                .collect(Collectors.toList());
    }

    public void deleteUserById(long userId) {
        checkUserExists(userId);
        friendshipStorage.deleteByUserId(userId);
        filmUserLikeStorage.deleteByUserId(userId);
        userStorage.deleteByUserId(userId);
    }

    public UserDto getUserById(long userId) {
        checkUserExists(userId);
        return UserMapper.mapToUserDto(userStorage.findByUserId(userId));
    }
}
