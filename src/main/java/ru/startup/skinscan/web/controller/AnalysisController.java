package ru.startup.skinscan.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/skinScan/analysisPhotos")
public interface AnalysisController {


    @GetMapping("/{nameFilePhoto}")
    @Operation(
            summary = "Получение анализа для фото по наименованию",
            description = "Получает результат анализа для фото найденного по наименованию"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Анализ получен"),
            @ApiResponse(responseCode = "409", description = "Не найдена фотография или анализ"),
            @ApiResponse(responseCode = "502", description = "Ошибка при анализе фото"),
            @ApiResponse(responseCode = "401", description = "Необходима аутентификация, неверный пароль или логин"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к этому анализу"),
    })
    ResponseEntity<?> getAllAnalysesByNamePhoto(@PathVariable String nameFilePhoto, @RequestAttribute("userId") UUID userId);

}
