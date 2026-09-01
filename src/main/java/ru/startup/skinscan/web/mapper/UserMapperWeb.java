package ru.startup.skinscan.web.mapper;

import ru.startup.skinscan.domain.model.User;
import ru.startup.skinscan.web.model.UserRequest;

public class UserMapperWeb {

    public static User requestToDto(UserRequest request) {
        return new User(
                request.login(),
                request.password(),
                request.email(),
                request.phone()
        );
    }
}
