package ru.vrn.vsu.csf.asashina.yandexproject.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vrn.vsu.csf.asashina.yandexproject.converter.ShopUnitConverter;
import ru.vrn.vsu.csf.asashina.yandexproject.exception.ObjectNotFoundException;
import ru.vrn.vsu.csf.asashina.yandexproject.exception.validation.IdException;
import ru.vrn.vsu.csf.asashina.yandexproject.exception.validation.PriceException;
import ru.vrn.vsu.csf.asashina.yandexproject.model.dto.ShopUnitCategoryDTO;
import ru.vrn.vsu.csf.asashina.yandexproject.model.dto.ShopUnitDTO;
import ru.vrn.vsu.csf.asashina.yandexproject.model.entity.ShopUnit;
import ru.vrn.vsu.csf.asashina.yandexproject.model.enumeration.ShopUnitType;
import ru.vrn.vsu.csf.asashina.yandexproject.model.request.ShopUnitImport;
import ru.vrn.vsu.csf.asashina.yandexproject.model.request.ShopUnitImportRequest;
import ru.vrn.vsu.csf.asashina.yandexproject.model.response.ShopUnitStaticResponse;
import ru.vrn.vsu.csf.asashina.yandexproject.model.response.ShopUnitStaticUnit;
import ru.vrn.vsu.csf.asashina.yandexproject.repository.ShopUnitRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopUnitServiceTest {

    @InjectMocks
    private ShopUnitService shopUnitService;

    @Mock
    private ShopUnitRepository shopUnitRepository;

    @Mock
    private PathVariablesAndParamsValidationService pathVariablesAndParamsValidationService;

    @Mock
    private ShopUnitTreeService shopUnitTreeService;

    @Mock
    private ArchiveShopUnitService archiveShopUnitService;

    private final ShopUnitConverter shopUnitConverter = Mappers.getMapper(ShopUnitConverter.class);

    @Test
    void getsShopUnitByIdForOffer() {
        // given
        var id = UUID.fromString( "96c9915e-3c09-430f-a314-7774697eeab5");
        var shopUnit = new ShopUnit(1L, id, "shopUnit1", Instant.parse("2022-02-02T12:00:00.000Z"),
                null, null, ShopUnitType.OFFER, 350L, null);

        when(shopUnitRepository.findShopUnitById(id)).thenReturn(shopUnit);

        // when
        ShopUnitDTO result = shopUnitService.getShopUnitById(id);

        // then
        assertThat(result).isEqualTo(shopUnitConverter.toOfferDTO(shopUnit));
    }

    @Test
    void getShopUnitByIdThrowsExceptionForWrongId() {
        // given
        var id = UUID.fromString("96c9915e-3c09-430f-a314-7774697eeab4");

        when(shopUnitRepository.findShopUnitById(id)).thenReturn(null);

        // when, then
        assertThatThrownBy(() -> shopUnitService.getShopUnitById(id)).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void getsShopUnitByIdForCategory() {
        // given
        var id = UUID.fromString("96c9915e-3c09-430f-a314-7774697eeab5");
        var shopUnit = new ShopUnit(1L, id, "shopUnit1", Instant.parse("2022-02-02T12:00:00.000Z"),
                null, null, ShopUnitType.CATEGORY, null, null);

        when(shopUnitRepository.findShopUnitById(id)).thenReturn(shopUnit);

        // when
        ShopUnitDTO result = shopUnitService.getShopUnitById(id);

        // then
        assertThat(result).isEqualTo(shopUnitConverter.toCategoryDTO(shopUnit, new ShopUnitCategoryDTO()));
    }

    @Test
    void createsShopUnitFromRequest() {
        // given
        var shopUnitImport1 = new ShopUnitImport("96c9915e-3c09-430f-a314-7774697eeab5", "shopUnit1",
                null, ShopUnitType.CATEGORY, null);
        var shopUnitImport2 = new ShopUnitImport("d515e43f-f3f6-4471-bb77-6b455017a2d2", "shopUnit2",
                null, ShopUnitType.OFFER, 234L);
        var date = "2022-02-02T12:00:00.000Z";
        var array = new ShopUnitImport[]{shopUnitImport1, shopUnitImport2};
        var request = new ShopUnitImportRequest(array, date);

        when(pathVariablesAndParamsValidationService.validateDate(date)).thenReturn(Instant.parse(date));
        when(shopUnitRepository.findShopUnitById(any(UUID.class))).thenReturn(null);
        when(shopUnitRepository.saveAll(anyList())).thenReturn(anyList());

        // when, then
        shopUnitService.createShopUnitFromRequest(request);
    }

    @Test
    void createShopUnitFromRequestThrowsExceptionForNotUniqueIds() {
        // given
        var shopUnitImport1 = new ShopUnitImport("96c9915e-3c09-430f-a314-7774697eeab5", "shopUnit1",
                null, ShopUnitType.CATEGORY, null);
        var shopUnitImport2 = new ShopUnitImport("96c9915e-3c09-430f-a314-7774697eeab5", "shopUnit2",
                null, ShopUnitType.OFFER, 234L);
        var date = "2022-02-02T12:00:00.000Z";
        var array = new ShopUnitImport[]{shopUnitImport1, shopUnitImport2};
        var request = new ShopUnitImportRequest(array, date);

        when(pathVariablesAndParamsValidationService.validateDate(date)).thenReturn(Instant.parse(date));
        when(shopUnitRepository.findShopUnitById(any(UUID.class))).thenReturn(null);

        // when, then
        assertThatThrownBy(() -> shopUnitService.createShopUnitFromRequest(request)).isInstanceOf(IdException.class);
    }

    @Test
    void createShopUnitFromRequestThrowsExceptionForOfferNullPrice() {
        // given
        var shopUnitImport1 = new ShopUnitImport("96c9915e-3c09-430f-a314-7774697eeab5", "shopUnit1",
                null, ShopUnitType.OFFER, null);
        var date = "2022-02-02T12:00:00.000Z";
        var array = new ShopUnitImport[]{shopUnitImport1};
        var request = new ShopUnitImportRequest(array, date);

        when(pathVariablesAndParamsValidationService.validateDate(date)).thenReturn(Instant.parse(date));

        // when, then
        assertThatThrownBy(() -> shopUnitService.createShopUnitFromRequest(request)).isInstanceOf(PriceException.class);
    }

    @Test
    void createShopUnitFromRequestThrowsExceptionForOfferNegativePrice() {
        // given
        var shopUnitImport1 = new ShopUnitImport("96c9915e-3c09-430f-a314-7774697eeab5", "shopUnit1",
                null, ShopUnitType.OFFER, -89L);
        var date = "2022-02-02T12:00:00.000Z";
        var array = new ShopUnitImport[]{shopUnitImport1};
        var request = new ShopUnitImportRequest(array, date);

        when(pathVariablesAndParamsValidationService.validateDate(date)).thenReturn(Instant.parse(date));

        // when, then
        assertThatThrownBy(() -> shopUnitService.createShopUnitFromRequest(request)).isInstanceOf(PriceException.class);
    }

    @Test
    void createShopUnitFromRequestThrowsExceptionForCategoryNotNullPrice() {
        // given
        var shopUnitImport1 = new ShopUnitImport("96c9915e-3c09-430f-a314-7774697eeab5", "shopUnit1",
                null, ShopUnitType.CATEGORY, 1L);
        var date = "2022-02-02T12:00:00.000Z";
        var array = new ShopUnitImport[]{shopUnitImport1};
        var request = new ShopUnitImportRequest(array, date);

        when(pathVariablesAndParamsValidationService.validateDate(date)).thenReturn(Instant.parse(date));

        // when, then
        assertThatThrownBy(() -> shopUnitService.createShopUnitFromRequest(request)).isInstanceOf(PriceException.class);
    }

    @Test
    void createsShopUnitFromRequestWhenCategoryAfterOffer() {
        // given
        var shopUnitImport1 = new ShopUnitImport("d515e43f-f3f6-4471-bb77-6b455017a2d2", "shopUnit2",
                "96c9915e-3c09-430f-a314-7774697eeab5", ShopUnitType.OFFER, 234L);
        var shopUnitImport2 = new ShopUnitImport("96c9915e-3c09-430f-a314-7774697eeab5", "shopUnit1",
                null, ShopUnitType.CATEGORY, null);
        var date = "2022-02-02T12:00:00.000Z";
        var array = new ShopUnitImport[]{shopUnitImport1, shopUnitImport2};
        var request = new ShopUnitImportRequest(array, date);

        when(pathVariablesAndParamsValidationService.validateDate(date)).thenReturn(Instant.parse(date));
        when(shopUnitRepository.saveAll(anyList())).thenReturn(anyList());

        // when, then
        shopUnitService.createShopUnitFromRequest(request);
    }

    @Test
    void deletesShopUnitById() {
        // given
        var id = UUID.fromString("96c9915e-3c09-430f-a314-7774697eeab5");
        var shopUnit = new ShopUnit(1L, id, "shopUnit1", Instant.parse("2022-02-02T12:00:00.000Z"),
                null, null, ShopUnitType.OFFER, 350L, null);

        when(shopUnitRepository.findShopUnitById(id)).thenReturn(shopUnit);

        // when, then
        shopUnitService.deleteShopUnitById(id);
    }

    @Test
    void deleteShopUnitByIdThrowsExceptionForNotExistingEntity() {
        // given
        var id = UUID.fromString("96c9915e-3c09-430f-a314-7774697eeab5");
        var shopUnit = new ShopUnit(1L, id, "shopUnit1", Instant.parse("2022-02-02T12:00:00.000Z"),
                null, null, ShopUnitType.OFFER, 350L, null);

        when(shopUnitRepository.findShopUnitById(id)).thenReturn(null);

        // when, then
        assertThatThrownBy(() -> shopUnitService.deleteShopUnitById(id)).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void getsLatestStatisticsOnOffers() {
        // given
        var date = Instant.parse("2022-02-02T12:00:00.000Z");
        var shopUnit1 = new ShopUnit(1L, UUID.fromString("96c9915e-3c09-430f-a314-7774697eeab5"),
                "shopUnit1", Instant.parse("2022-02-02T12:00:00.000Z"), null, null,
                ShopUnitType.OFFER, 350L, null);
        var shopUnit2 = new ShopUnit(2L, UUID.fromString("863e1a7a-1304-42ae-943b-179184c077e3"),
                "shopUnit1", Instant.parse("2022-02-02T12:00:00.000Z"), null, null,
                ShopUnitType.OFFER, 650L, null);
        var offers = List.of(shopUnit1, shopUnit2);

        when(shopUnitRepository.getLastUpdatedOffers(any(Instant.class), any(Instant.class)))
                .thenReturn(offers);

        // when
        ShopUnitStaticResponse result = shopUnitService.getLatestStatisticsOnOffers(date);

        // then
        assertThat(result.getItems()).isEqualTo(offers.stream()
                .map(shopUnitConverter::toShopUnitStaticUnitFromShopUnit)
                .toList());
    }

    @Test
    void getsNodeStatisticsForEntityById() {
        // given
        var id = UUID.fromString("96c9915e-3c09-430f-a314-7774697eeab5");
        Instant dateStart = Instant.parse("1000-01-01T00:00:00.000Z");
        Instant dateEnd = Instant.parse("9999-12-31T23:59:59.000Z");
        var shopUnit1 = new ShopUnit(1L, id, "shopUnit1", Instant.parse("2022-02-02T12:00:00.000Z"),
                null, null, ShopUnitType.OFFER, 350L, null);
        var shopUnitStaticUnit1 = new ShopUnitStaticUnit(shopUnit1.getId(), shopUnit1.getName(), shopUnit1.getParentId(),
                shopUnit1.getUnitType(), shopUnit1.getPrice(), shopUnit1.getDate());
        var shopUnitStaticUnit2 = new ShopUnitStaticUnit(shopUnit1.getId(), shopUnit1.getName(), shopUnit1.getParentId(),
                shopUnit1.getUnitType(), 700L, Instant.parse("2022-02-03T12:00:00.000Z"));
        var shopUnitStaticUnit3 = new ShopUnitStaticUnit(shopUnit1.getId(), "shopUnitChanged",
                shopUnit1.getParentId(), shopUnit1.getUnitType(), 700L,
                Instant.parse("2022-02-04T12:40:00.000Z"));
        var shopUnitStaticUnits = List.of(shopUnitStaticUnit1, shopUnitStaticUnit2, shopUnitStaticUnit3);

        when(shopUnitRepository.findShopUnitById(id)).thenReturn(shopUnit1);
        when(archiveShopUnitService.getArchiveForNodeById(shopUnit1, dateStart, dateEnd)).thenReturn(shopUnitStaticUnits);

        // when
        ShopUnitStaticResponse result = shopUnitService.getNodeStatisticsForEntityById(id, dateStart, dateEnd);

        // then
        assertThat(result).isEqualTo(new ShopUnitStaticResponse(shopUnitStaticUnits));
    }

    @Test
    void getNodeStatisticsForEntityByIdThrowsExceptionForNotExistingId() {
        // given
        var id = UUID.fromString("96c9915e-3c09-430f-a314-7774697eeab5");
        Instant dateStart = null;
        Instant dateEnd = null;

        when(shopUnitRepository.findShopUnitById(id)).thenReturn(null);

        // when, then
        assertThatThrownBy(() -> shopUnitService.getNodeStatisticsForEntityById(id, dateStart, dateEnd))
                .isInstanceOf(ObjectNotFoundException.class);
    }
}