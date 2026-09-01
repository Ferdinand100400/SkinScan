package ru.startup.skinscan.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.startup.skinscan.web.model.PhotoRequest;

import java.util.UUID;

@RestController
@RequestMapping("/skinScan/photos")
public interface PhotoController {

    @PostMapping("/upload")
    @Operation(
            summary = "Загрузка фотографии",
            description = "Добавляет фотографию к пользователю, а ее метаданные в БД, передает на обработку в ML"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Фотография успешно сохранена и началась обработка"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации, а также неверный формат фотографии или превышен размер файла"),
            @ApiResponse(responseCode = "409", description = "Фото с таким именем уже существует"),
            @ApiResponse(responseCode = "401", description = "Необходима аутентификация, неверный пароль или логин"),
            @ApiResponse(responseCode = "500", description = "Ошибка добавления файла на стороне файловой системы"),
    })
    ResponseEntity<?> uploadPhoto(@ModelAttribute @Valid PhotoRequest photoRequest, @RequestAttribute("userId") UUID userId);

    @GetMapping("")
    @Operation(
            summary = "Получение всех фотографий",
            description = "Получает метаданные и ссылки всех фотографий у которых завершен анализ"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Фотографии получены"),
            @ApiResponse(responseCode = "401", description = "Необходима аутентификация, неверный пароль или логин"),
    })
    ResponseEntity<?> getAllPhotos(@RequestAttribute("userId") UUID userId);

    @GetMapping("/name/{nameFile}")
    @Operation(
            summary = "Получение фотографии по наименованию",
            description = "Получает метаданные и ссылку фотографии найденной по наименованию с завершенным анализом"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Фотография получена"),
            @ApiResponse(responseCode = "409", description = "Не найдена фотография"),
            @ApiResponse(responseCode = "502", description = "Фотография находится не в статусе: анализ завершен"),
            @ApiResponse(responseCode = "401", description = "Необходима аутентификация, неверный пароль или логин"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к этой фотографии"),
    })
    ResponseEntity<?> getPhotosByName(@PathVariable String nameFile, @RequestAttribute("userId") UUID userId);

    @GetMapping("/{photoId}")
    @Operation(
            summary = "Получение фотографии по id",
            description = "Получает метаданные и ссылку фотографии найденной по id с завершенным анализом"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Фотография получена"),
            @ApiResponse(responseCode = "409", description = "Не найдена фотография"),
            @ApiResponse(responseCode = "502", description = "Фотография находится не в статусе: анализ завершен"),
            @ApiResponse(responseCode = "401", description = "Необходима аутентификация, неверный пароль или логин"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к этой фотографии"),
    })
    ResponseEntity<?> getPhotosById(@PathVariable UUID photoId, @RequestAttribute("userId") UUID userId);
}
