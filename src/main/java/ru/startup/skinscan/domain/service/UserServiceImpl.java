package ru.startup.skinscan.domain.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.startup.skinscan.datasource.mapper.UserMapper;
import ru.startup.skinscan.datasource.repository.JpaUserRepository;
import ru.startup.skinscan.domain.model.User;
import ru.startup.skinscan.exception.IncorrectPasswordException;
import ru.startup.skinscan.exception.NotFindUserException;
import ru.startup.skinscan.exception.UserAlreadyExistsException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final JpaUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(JpaUserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UUID createUser(User user) {
        try {
            UUID id = findUserIdByLogin(user.login());
            throw new UserAlreadyExistsException(id, user.login());
        } catch (NotFindUserException e) {
            String encodedPassword = passwordEncoder.encode(user.password());
            user.setPassword(encodedPassword);
            System.out.println("Пользователь создан!");
            return userRepo.save(UserMapper.dtoToEntity(user)).id();
        }
    }

    @Override
    public User findUserByLogin(String login) {
        return UserMapper.entityToDto(userRepo.findByLogin(login)
                .orElseThrow(() -> new NotFindUserException(login))
        );
    }

    @Override
    public UUID findUserIdByLogin(String login) {
        return userRepo.findIdByLogin(login)
                .orElseThrow(() -> new NotFindUserException(login)
        );
    }

    @Override
    @Transactional
    public UUID updateUser(User user) {
        try {
            User curUser = UserMapper.entityToDto(
                    userRepo.findByLogin(user.login()).orElseThrow(() -> new NotFindUserException(user.login()))
            );
            if (!passwordEncoder.matches(user.password(), curUser.password())) throw new IncorrectPasswordException();
            if (userRepo.update(user.login(), curUser.password(), user.email(), user.phone(), OffsetDateTime.now()) == 1)
                return userRepo.findIdByLogin(user.login()).orElseThrow();
            throw new RuntimeException("Ошибка обновления пользователя");
        } catch (NotFindUserException e) {
            System.out.println("Пользователь не найден");
            throw new NotFindUserException(user.login());
        }
    }

    @Override
    @Transactional
    public int updatePassword(UUID idUser, String oldPassword, String newPassword) {
        try {
            String curPassword = userRepo.findById(idUser).orElseThrow().password();
            if (!passwordEncoder.matches(oldPassword, curPassword)) {
                System.out.println("Старый пароль не совпадает");
                throw new IncorrectPasswordException();
            }
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            if (userRepo.updatePassword(idUser, encodedNewPassword, OffsetDateTime.now()) == 1)
                return 1;
            throw new RuntimeException("Ошибка обновления пароля пользователя");
        } catch (NotFindUserException e) {
            System.out.println("Пользователь не найден");
            throw new NotFindUserException(idUser.toString());
        }
    }

    public PasswordEncoder passwordEncoder() {
        return passwordEncoder;
    }
}
