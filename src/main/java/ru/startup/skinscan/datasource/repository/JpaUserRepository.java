package ru.startup.skinscan.datasource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.startup.skinscan.datasource.entity.UserEntity;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {

    @Query("SELECT u FROM UserEntity u WHERE u.login = :login")
    Optional<UserEntity> findByLogin(String login);

    @Query("SELECT u.id FROM UserEntity u WHERE u.login = :login")
    Optional<UUID> findIdByLogin(String login);

    @Modifying
    @Query("UPDATE UserEntity u SET u.password = :password, u.email = :email, u.phone = :phone, u.updatedAt = CAST(:update_at AS OffsetDateTime) WHERE u.login = :login")
    int update(
            @Param("login") String login,
            @Param("password") String password,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("update_at") OffsetDateTime updateAt
    );

    @Modifying
    @Query("UPDATE UserEntity u SET u.password = :password, u.updatedAt = CAST(:update_at AS OffsetDateTime) WHERE u.id = :id")
    int updatePassword(
            @Param("id") UUID login,
            @Param("password") String password,
            @Param("update_at") OffsetDateTime updateAt
    );
}
