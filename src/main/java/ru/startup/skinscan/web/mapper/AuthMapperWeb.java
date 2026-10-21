package ru.startup.skinscan.web.mapper;

import ru.startup.skinscan.domain.model.User;
import ru.startup.skinscan.domain.model.jwt.JwtTokens;
import ru.startup.skinscan.web.model.jwt.JwtRequest;
import ru.startup.skinscan.web.model.jwt.JwtResponse;

public class AuthMapperWeb {

    public static User requestToDto(JwtRequest request) {
        return new User(request.login(), request.password());
    }

    public static JwtResponse dtoToResponse(JwtTokens tokens) {
        return new JwtResponse(tokens.accessToken(), tokens.refreshToken());
    }
}
