package ru.vrn.vsu.csf.asashina.yandexproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vrn.vsu.csf.asashina.yandexproject.model.request.ShopUnitImportRequest;
import ru.vrn.vsu.csf.asashina.yandexproject.model.response.ResponseBuilder;
import ru.vrn.vsu.csf.asashina.yandexproject.service.PathVariablesAndParamsValidationService;
import ru.vrn.vsu.csf.asashina.yandexproject.service.ShopUnitService;

import javax.validation.Valid;
import java.time.Instant;
import java.util.UUID;

@RestController
@AllArgsConstructor
@Tag(name = "Shop Unit Controller")
public class ShopUnitController {

    private final ShopUnitService shopUnitService;
    private final PathVariablesAndParamsValidationService pathVariablesAndParamsValidationService;

    @GetMapping("/nodes/{id}")
    @Operation(summary = "Получает информацию об элементе по идентификатору.")
    public ResponseEntity<?> getNodeById(@PathVariable("id") String idVariable) {
        UUID id = pathVariablesAndParamsValidationService.validateId(idVariable);
        return ResponseBuilder.build(HttpStatus.OK, shopUnitService.getShopUnitById(id));
    }

    @GetMapping("/nodes/{id}/statistic")
    @Operation(summary = "Получение статистики (истории обновлений) по товару/категории за заданный полуинтервал [from, to)")
    public ResponseEntity<?> getNodeStatisticById(
            @PathVariable("id") String idVariable,
            @RequestParam(value = "dateStart", defaultValue = "", required = false) String dateStartVariable,
            @RequestParam(value = "dateEnd", defaultValue = "", required = false) String dateEndVariable) {
        UUID id = pathVariablesAndParamsValidationService.validateId(idVariable);
        Instant dateStart = dateStartVariable.isEmpty()
                ? Instant.parse("1000-01-01T00:00:00.000Z")
                : pathVariablesAndParamsValidationService.validateDate(dateStartVariable);
        Instant dateEnd = dateEndVariable.isEmpty()
                ? Instant.parse("9999-12-31T23:59:59.000Z")
                : pathVariablesAndParamsValidationService.validateDate(dateEndVariable);
        return ResponseBuilder.build(HttpStatus.OK,
                shopUnitService.getNodeStatisticsForEntityById(id, dateStart, dateEnd));
    }

    @GetMapping("/sales")
    @Operation(summary = "Получение товаров, цена которых была обновлена за последние 24 часа включительно.")
    public ResponseEntity<?> getLastUpdatedOffers(@RequestParam(value = "date", defaultValue = "") String dateVariable) {
        Instant date = pathVariablesAndParamsValidationService.validateDate(dateVariable);
        return ResponseBuilder.build(HttpStatus.OK, shopUnitService.getLatestStatisticsOnOffers(date));
    }

    @PostMapping("/imports")
    @Operation(summary = "Импортирует новые товары и/или категории.")
    public ResponseEntity<?> addNode(@RequestBody @Valid ShopUnitImportRequest request) {
        shopUnitService.createShopUnitFromRequest(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Удалить элемент по идентификатору.")
    public ResponseEntity<?> deleteNodeById(@PathVariable("id") String idVariable) {
        UUID id = pathVariablesAndParamsValidationService.validateId(idVariable);
        shopUnitService.deleteShopUnitById(id);
        return ResponseEntity.ok().build();
    }
}
