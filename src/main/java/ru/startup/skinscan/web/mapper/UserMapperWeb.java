package ru.startup.skinscan.web.mapper;

import ru.startup.skinscan.domain.model.User;
import ru.startup.skinscan.domain.model.role.UserRole;
import ru.startup.skinscan.web.model.UserRequest;
import ru.startup.skinscan.web.model.UserResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class UserMapperWeb {

    public static User requestToDto(UserRequest request) {
        return new User(
                request.login(),
                request.password(),
                request.email(),
                request.phone()
        );
    }

    public static UserResponse dtoToResponse(User user, UUID userId, OffsetDateTime createdAt) {
        List<String> roles = user.roles().stream()
                .map(UserRole::getAuthority)
                .toList();
        return new UserResponse(
                userId,
                user.login(),
                user.email(),
                user.phone(),
                roles,
                createdAt
        );
    }
}
