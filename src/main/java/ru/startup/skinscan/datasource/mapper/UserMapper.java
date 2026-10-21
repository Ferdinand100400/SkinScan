package ru.startup.skinscan.datasource.mapper;

import ru.startup.skinscan.datasource.entity.UserEntity;
import ru.startup.skinscan.domain.model.User;
import ru.startup.skinscan.domain.model.role.UserRole;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserEntity dtoToEntity(User user) {
        return new UserEntity(
                user.login(),
                user.password(),
                user.roles().stream()
                        .map(UserRole::role)
                        .collect(Collectors.toSet()),
                user.email(),
                user.phone()
        );
    }

    public static User entityToDto(UserEntity entity) {
        List<UserRole> roles = entity.roles().stream()
                .map(UserRole::new)
                .toList();
        return new User(entity.login(), entity.password(), entity.email(), entity.phone(), roles);
    }
}
