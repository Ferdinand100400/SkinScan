package ru.startup.skinscan.datasource.mapper;

import ru.startup.skinscan.datasource.entity.UserEntity;
import ru.startup.skinscan.domain.model.User;

public class UserMapper {

    public static UserEntity dtoToEntity(User user) {
        return new UserEntity(user.login(), user.password(), user.email(), user.phone());
    }

    public static User entityToDto(UserEntity entity) {
        return new User(entity.login(), entity.password(), entity.email(), entity.phone());
    }
}
