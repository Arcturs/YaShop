package ru.vrn.vsu.csf.asashina.yandexproject.controller;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.vrn.vsu.csf.asashina.yandexproject.model.entity.ArchiveShopUnit;
import ru.vrn.vsu.csf.asashina.yandexproject.model.entity.ShopUnit;
import ru.vrn.vsu.csf.asashina.yandexproject.model.enumeration.ShopUnitType;
import ru.vrn.vsu.csf.asashina.yandexproject.repository.ArchiveShopUnitRepository;
import ru.vrn.vsu.csf.asashina.yandexproject.repository.ShopUnitRepository;
import ru.vrn.vsu.csf.asashina.yandexproject.repository.ShopUnitTreeRepository;
import ru.vrn.vsu.csf.asashina.yandexproject.service.ShopUnitService;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
class ShopUnitControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ShopUnitRepository shopUnitRepository;

    @Autowired
    private ShopUnitTreeRepository shopUnitTreeRepository;

    @Autowired
    private ArchiveShopUnitRepository archiveShopUnitRepository;

    @Autowired
    private ShopUnitService shopUnitService;

    @AfterEach
    void tearDown() {
        shopUnitRepository.deleteAll();
        jdbcTemplate.execute("ALTER TABLE shop_unit ALTER COLUMN shop_unit_id RESTART WITH 1");

        shopUnitTreeRepository.deleteAll();
        jdbcTemplate.execute("ALTER TABLE shop_unit_tree ALTER COLUMN id RESTART WITH 1");

        archiveShopUnitRepository.deleteAll();
        jdbcTemplate.execute("ALTER TABLE archive_shop_unit ALTER COLUMN archive_shop_unit_id RESTART WITH 1");
    }

    @Test
    void getsNodeById() throws JSONException {
        // given
        var shopUnit1 = new ShopUnit(1L, UUID.fromString("069cb8d7-bbdd-47d3-ad8f-82ef4c269df1"),
                "Товары", Instant.parse("2022-02-03T15:00:00.000Z"), null, null,
                ShopUnitType.CATEGORY, null, new HashSet<>());
        var shopUnit2 = new ShopUnit(2L, UUID.fromString("d515e43f-f3f6-4471-bb77-6b455017a2d2"),
                "Смартфоны", Instant.parse("2022-02-02T12:00:00.000Z"), shopUnit1.getId(), shopUnit1,
                ShopUnitType.CATEGORY, null, new HashSet<>());
        var shopUnit3 = new ShopUnit(3L, UUID.fromString("863e1a7a-1304-42ae-943b-179184c077e3"),
                "jPhone 13", Instant.parse("2022-02-02T12:00:00.000Z"), shopUnit2.getId(), shopUnit2,
                ShopUnitType.OFFER, 79999L, new HashSet<>());
        var shopUnit4 = new ShopUnit(4L, UUID.fromString("b1d8fd7d-2ae3-47d5-b2f9-0f094af800d4"),
                "Xomiа Readme 10", Instant.parse("2022-02-02T12:00:00.000Z"), shopUnit2.getId(), shopUnit2,
                ShopUnitType.OFFER, 59999L, new HashSet<>());
        var shopUnit5 = new ShopUnit(5L, UUID.fromString("1cc0129a-2bfe-474c-9ee6-d435bf5fc8f2"),
                "Телевизоры", Instant.parse("2022-02-03T15:00:00.000Z"), shopUnit1.getId(), shopUnit1,
                ShopUnitType.CATEGORY, null, new HashSet<>());
        var shopUnit6 = new ShopUnit(6L, UUID.fromString("98883e8f-0507-482f-bce2-2fb306cf6483"),
                "Samson 70\" LED UHD Smart", Instant.parse("2022-02-03T12:00:00.000Z"), shopUnit5.getId(),
                shopUnit5, ShopUnitType.OFFER, 32999L, new HashSet<>());
        var shopUnit7 = new ShopUnit(7L, UUID.fromString("74b81fda-9cdc-4b63-8927-c978afed5cf4"),
                "Phyllis 50\" LED UHD Smarter", Instant.parse("2022-02-03T12:00:00.000Z"), shopUnit5.getId(),
                shopUnit5, ShopUnitType.OFFER, 49999L, new HashSet<>());
        var shopUnit8 = new ShopUnit(8L, UUID.fromString("73bc3b36-02d1-4245-ab35-3106c9ee1c65"),
                "Goldstar 65\" LED UHD LOL Very Smart", Instant.parse("2022-02-03T15:00:00.000Z"),
                shopUnit5.getId(), shopUnit5, ShopUnitType.OFFER, 69999L, new HashSet<>());
        shopUnitService.saveAllShopUnitsToDBTreeAndArchive(List.of(shopUnit1, shopUnit2, shopUnit3, shopUnit4,
                shopUnit5, shopUnit6, shopUnit7, shopUnit8));

        // when
        ResponseEntity<String> response = testRestTemplate.getForEntity("/nodes/069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
                String.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "type": "CATEGORY",
                    "name": "Товары",
                    "id": "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
                    "price": 58599,
                    "parentId": null,
                    "date": "2022-02-03T15:00:00.000Z",
                    "children": [
                        {
                            "type": "CATEGORY",
                            "name": "Телевизоры",
                            "id": "1cc0129a-2bfe-474c-9ee6-d435bf5fc8f2",
                            "parentId": "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
                            "price": 50999,
                            "date": "2022-02-03T15:00:00.000Z",
                            "children": [
                                {
                                    "type": "OFFER",
                                    "name": "Samson 70\\" LED UHD Smart",
                                    "id": "98883e8f-0507-482f-bce2-2fb306cf6483",
                                    "parentId": "1cc0129a-2bfe-474c-9ee6-d435bf5fc8f2",
                                    "price": 32999,
                                    "date": "2022-02-03T12:00:00.000Z",
                                    "children": null
                                },
                                {
                                    "type": "OFFER",
                                    "name": "Phyllis 50\\" LED UHD Smarter",
                                    "id": "74b81fda-9cdc-4b63-8927-c978afed5cf4",
                                    "parentId": "1cc0129a-2bfe-474c-9ee6-d435bf5fc8f2",
                                    "price": 49999,
                                    "date": "2022-02-03T12:00:00.000Z",
                                    "children": null
                                },
                                {
                                    "type": "OFFER",
                                    "name": "Goldstar 65\\" LED UHD LOL Very Smart",
                                    "id": "73bc3b36-02d1-4245-ab35-3106c9ee1c65",
                                    "parentId": "1cc0129a-2bfe-474c-9ee6-d435bf5fc8f2",
                                    "price": 69999,
                                    "date": "2022-02-03T15:00:00.000Z",
                                    "children": null
                                }
                            ]
                        },
                        {
                            "type": "CATEGORY",
                            "name": "Смартфоны",
                            "id": "d515e43f-f3f6-4471-bb77-6b455017a2d2",
                            "parentId": "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
                            "price": 69999,
                            "date": "2022-02-02T12:00:00.000Z",
                            "children": [
                                {
                                    "type": "OFFER",
                                    "name": "jPhone 13",
                                    "id": "863e1a7a-1304-42ae-943b-179184c077e3",
                                    "parentId": "d515e43f-f3f6-4471-bb77-6b455017a2d2",
                                    "price": 79999,
                                    "date": "2022-02-02T12:00:00.000Z",
                                    "children": null
                                },
                                {
                                    "type": "OFFER",
                                    "name": "Xomiа Readme 10",
                                    "id": "b1d8fd7d-2ae3-47d5-b2f9-0f094af800d4",
                                    "parentId": "d515e43f-f3f6-4471-bb77-6b455017a2d2",
                                    "price": 59999,
                                    "date": "2022-02-02T12:00:00.000Z",
                                    "children": null
                                }
                            ]
                        }
                    ]
                }""", response.getBody(), false);
    }

    @Test
    void getNodeByIdThrowsExceptionForInvalidId() throws JSONException {
        // when
        ResponseEntity<String> response = testRestTemplate.getForEntity("/nodes/069cb8d7-bbdd-ad8f-82ef4c269df1",
                String.class);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 400,
                    "message": "Validation Failed"
                }""", response.getBody(), false);
    }

    @Test
    void getNodeByIdThrowsExceptionForNotExistingId() throws JSONException {
        // when
        ResponseEntity<String> response = testRestTemplate.getForEntity("/nodes/d515e43f-f3f6-4471-bb77-6b455017a2d2",
                String.class);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 404,
                    "message": "Item not found"
                }""", response.getBody(), false);
    }

    @Test
    void getsLastUpdatedOffers() throws JSONException {
        // given
        var shopUnit1 = new ShopUnit(1L, UUID.fromString("069cb8d7-bbdd-47d3-ad8f-82ef4c269df1"), "Товары",
                Instant.parse("2022-02-03T15:00:00.000Z"), null, null, ShopUnitType.CATEGORY,
                null, new HashSet<>());
        var shopUnit2 = new ShopUnit(2L, UUID.fromString("d515e43f-f3f6-4471-bb77-6b455017a2d2"),
                "Смартфоны", Instant.parse("2022-02-02T12:00:00.000Z"), shopUnit1.getId(), shopUnit1,
                ShopUnitType.CATEGORY, null, new HashSet<>());
        var shopUnit3 = new ShopUnit(3L, UUID.fromString("863e1a7a-1304-42ae-943b-179184c077e3"),
                "jPhone 13", Instant.parse("2022-02-02T12:00:00.000Z"), shopUnit2.getId(), shopUnit2,
                ShopUnitType.OFFER, 79999L, new HashSet<>());
        var shopUnit4 = new ShopUnit(4L, UUID.fromString("b1d8fd7d-2ae3-47d5-b2f9-0f094af800d4"),
                "Xomiа Readme 10", Instant.parse("2022-02-02T21:00:00.000Z"), shopUnit2.getId(), shopUnit2,
                ShopUnitType.OFFER, 59999L, new HashSet<>());
        var shopUnit5 = new ShopUnit(5L, UUID.fromString("1cc0129a-2bfe-474c-9ee6-d435bf5fc8f2"),
                "Телевизоры", Instant.parse("2022-02-04T21:00:00.000Z"), shopUnit1.getId(), shopUnit1,
                ShopUnitType.CATEGORY, null, new HashSet<>());
        var shopUnit6 = new ShopUnit(6L, UUID.fromString("98883e8f-0507-482f-bce2-2fb306cf6483"),
                "Samson 70\" LED UHD Smart", Instant.parse("2022-02-03T15:00:00.000Z"), shopUnit5.getId(),
                shopUnit5, ShopUnitType.OFFER, 32999L, new HashSet<>());
        var shopUnit7 = new ShopUnit(7L, UUID.fromString("74b81fda-9cdc-4b63-8927-c978afed5cf4"),
                "Phyllis 50\" LED UHD Smarter", Instant.parse("2022-02-02T17:00:00.000Z"), shopUnit5.getId(),
                shopUnit5, ShopUnitType.OFFER, 49999L, new HashSet<>());
        var shopUnit8 = new ShopUnit(8L, UUID.fromString("73bc3b36-02d1-4245-ab35-3106c9ee1c65"),
                "Goldstar 65\" LED UHD LOL Very Smart", Instant.parse("2022-02-03T16:00:00.000Z"),
                shopUnit5.getId(), shopUnit5, ShopUnitType.OFFER, 69999L, new HashSet<>());
        shopUnitService.saveAllShopUnitsToDBTreeAndArchive(List.of(shopUnit1, shopUnit2, shopUnit3, shopUnit4, shopUnit5,
                shopUnit6, shopUnit7, shopUnit8));

        // when
        ResponseEntity<String> response = testRestTemplate.getForEntity("/sales?date=2022-02-03T15:00:00.000Z",
                String.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "items": [
                        {
                            "type": "OFFER",
                            "name": "Xomiа Readme 10",
                            "id": "b1d8fd7d-2ae3-47d5-b2f9-0f094af800d4",
                            "parentId": "d515e43f-f3f6-4471-bb77-6b455017a2d2",
                            "price": 59999,
                            "date": "2022-02-02T21:00:00.000Z"
                        },
                        {
                            "type": "OFFER",
                            "name": "Samson 70\\" LED UHD Smart",
                            "id": "98883e8f-0507-482f-bce2-2fb306cf6483",
                            "parentId": "1cc0129a-2bfe-474c-9ee6-d435bf5fc8f2",
                            "price": 32999,
                            "date": "2022-02-03T15:00:00.000Z"
                        },
                        {
                            "type": "OFFER",
                            "name": "Phyllis 50\\" LED UHD Smarter",
                            "id": "74b81fda-9cdc-4b63-8927-c978afed5cf4",
                            "parentId": "1cc0129a-2bfe-474c-9ee6-d435bf5fc8f2",
                            "price": 49999,
                            "date": "2022-02-02T17:00:00.000Z"
                        }
                    ]
                }""", response.getBody(), false);
    }

    @Test
    void getLastUpdatedOffersThrowsExceptionForInvalidDate() throws JSONException {
        // when
        ResponseEntity<String> response = testRestTemplate.getForEntity("/sales?date=2022-02-03", String.class);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 400,
                    "message": "Validation Failed"
                }""", response.getBody(), false);
    }

    @Test
    void getsNodeStatisticByIdWithoutDateParams() throws JSONException {
        // given
        var shopUnit1 = new ShopUnit(1L, UUID.fromString("069cb8d7-bbdd-47d3-ad8f-82ef4c269df1"), "Товары",
                Instant.parse("2022-02-03T15:00:00.000Z"), null, null, ShopUnitType.CATEGORY,
                null, new HashSet<>());
        var shopUnit2 = new ShopUnit(2L, UUID.fromString("d515e43f-f3f6-4471-bb77-6b455017a2d2"),
                "Смартфоны", Instant.parse("2022-02-02T12:00:00.000Z"), shopUnit1.getId(), shopUnit1,
                ShopUnitType.CATEGORY, null, new HashSet<>());
        var shopUnit3 = new ShopUnit(3L, UUID.fromString("863e1a7a-1304-42ae-943b-179184c077e3"),
                "jPhone 13", Instant.parse("2022-02-02T12:00:00.000Z"), shopUnit2.getId(), shopUnit2,
                ShopUnitType.OFFER, 79999L, new HashSet<>());
        shopUnitService.saveAllShopUnitsToDBTreeAndArchive(List.of(shopUnit1, shopUnit2, shopUnit3));

        var archiveShopUnit21 = new ArchiveShopUnit(4L, shopUnit2.getShopUnitId(), shopUnit2.getId(),
                "smartphones", Instant.parse("2022-02-03T14:45:00.000Z"), shopUnit1.getId(),
                shopUnit2.getUnitType(), shopUnit2.getPrice());
        var archiveShopUnit31 = new ArchiveShopUnit(5L, shopUnit3.getShopUnitId(), shopUnit3.getId(),
                shopUnit3.getName(), Instant.parse("2022-02-03T14:45:00.000Z"), shopUnit2.getId(),
                shopUnit3.getUnitType(), 89999L);
        var archiveShopUnit32 = new ArchiveShopUnit(6L, shopUnit3.getShopUnitId(), shopUnit3.getId(),
                "jPhone 13 new", Instant.parse("2022-02-04T18:45:00.000Z"), shopUnit2.getId(),
                shopUnit3.getUnitType(), 89999L);
        archiveShopUnitRepository.saveAll(List.of(archiveShopUnit21, archiveShopUnit31, archiveShopUnit32));

        // when
        ResponseEntity<String> response = testRestTemplate.getForEntity("/nodes/863e1a7a-1304-42ae-943b-179184c077e3/statistic",
                String.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "items": [
                        {
                            "type": "OFFER",
                            "name": "jPhone 13",
                            "id": "863e1a7a-1304-42ae-943b-179184c077e3",
                            "parentId": "d515e43f-f3f6-4471-bb77-6b455017a2d2",
                            "price": 79999,
                            "date": "2022-02-02T12:00:00.000Z"
                        },
                        {
                            "type": "OFFER",
                            "name": "jPhone 13",
                            "id": "863e1a7a-1304-42ae-943b-179184c077e3",
                            "parentId": "d515e43f-f3f6-4471-bb77-6b455017a2d2",
                            "price": 89999,
                            "date": "2022-02-03T14:45:00.000Z"
                        },
                        {
                            "type": "OFFER",
                            "name": "jPhone 13 new",
                            "id": "863e1a7a-1304-42ae-943b-179184c077e3",
                            "parentId": "d515e43f-f3f6-4471-bb77-6b455017a2d2",
                            "price": 89999,
                            "date": "2022-02-04T18:45:00.000Z"
                        }
                    ]
                }""", response.getBody(), false);
    }

    @Test
    void getsNodeStatisticByIdWithDateParams() throws JSONException {
        // given
        var shopUnit1 = new ShopUnit(1L, UUID.fromString("069cb8d7-bbdd-47d3-ad8f-82ef4c269df1"), "Товары",
                Instant.parse("2022-02-03T15:00:00.000Z"), null, null, ShopUnitType.CATEGORY,
                null, new HashSet<>());
        var shopUnit2 = new ShopUnit(2L, UUID.fromString("d515e43f-f3f6-4471-bb77-6b455017a2d2"),
                "Смартфоны", Instant.parse("2022-02-02T12:00:00.000Z"), shopUnit1.getId(), shopUnit1,
                ShopUnitType.CATEGORY, null, new HashSet<>());
        var shopUnit3 = new ShopUnit(3L, UUID.fromString("863e1a7a-1304-42ae-943b-179184c077e3"),
                "jPhone 13", Instant.parse("2022-02-02T12:00:00.000Z"), shopUnit2.getId(), shopUnit2,
                ShopUnitType.OFFER, 79999L, new HashSet<>());
        shopUnitService.saveAllShopUnitsToDBTreeAndArchive(List.of(shopUnit1, shopUnit2, shopUnit3));

        var archiveShopUnit21 = new ArchiveShopUnit(2L, shopUnit2.getShopUnitId(), shopUnit2.getId(),
                "smartphones", Instant.parse("2022-02-03T14:45:00.000Z"), shopUnit1.getId(),
                shopUnit2.getUnitType(), shopUnit2.getPrice());
        var archiveShopUnit31 = new ArchiveShopUnit(3L, shopUnit3.getShopUnitId(), shopUnit3.getId(),
                shopUnit3.getName(), Instant.parse("2022-02-03T14:45:00.000Z"), shopUnit2.getId(),
                shopUnit3.getUnitType(), 89999L);
        var archiveShopUnit32 = new ArchiveShopUnit(4L, shopUnit3.getShopUnitId(), shopUnit3.getId(),
                "jPhone 13 new", Instant.parse("2022-02-04T18:45:00.000Z"), shopUnit2.getId(),
                shopUnit3.getUnitType(), 89999L);
        archiveShopUnitRepository.saveAll(List.of(archiveShopUnit21, archiveShopUnit31, archiveShopUnit32));

        // when
        ResponseEntity<String> response = testRestTemplate.getForEntity(
                "/nodes/863e1a7a-1304-42ae-943b-179184c077e3/statistic?dateStart=2022-02-03T12:00:00.000Z&dateEnd=2022-02-04T19:45:00.000Z",
                String.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "items": [
                        {
                            "type": "OFFER",
                            "name": "jPhone 13",
                            "id": "863e1a7a-1304-42ae-943b-179184c077e3",
                            "parentId": "d515e43f-f3f6-4471-bb77-6b455017a2d2",
                            "price": 89999,
                            "date": "2022-02-03T14:45:00.000Z"
                        },
                        {
                            "type": "OFFER",
                            "name": "jPhone 13 new",
                            "id": "863e1a7a-1304-42ae-943b-179184c077e3",
                            "parentId": "d515e43f-f3f6-4471-bb77-6b455017a2d2",
                            "price": 89999,
                            "date": "2022-02-04T18:45:00.000Z"
                        }
                    ]
                }""", response.getBody(), false);
    }

    @Test
    void getNodeStatisticByIdWithDateParamsThrowsExceptionForInvalidDateFormat() throws JSONException {
        // when
        ResponseEntity<String> response = testRestTemplate.getForEntity(
                "/nodes/863e1a7a-1304-42ae-943b-179184c077e3/statistic?dateStart=2022-02-03&dateEnd=2022-02-04T19:45:00.000Z",
                String.class);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 400,
                    "message": "Validation Failed"
                }""", response.getBody(), false);
    }

    @Test
    void getNodeStatisticByIdThrowsExceptionForInvalidId() throws JSONException {
        // when
        ResponseEntity<String> response = testRestTemplate.getForEntity("/nodes/863e1a7a-1304-42ae-943b/statistic",
                String.class);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 400,
                    "message": "Validation Failed"
                }""", response.getBody(), false);
    }

    @Test
    void getNodeStatisticByIdThrowsExceptionForNotExistingId() throws JSONException {
        // when
        ResponseEntity<String> response = testRestTemplate.getForEntity(
                "/nodes/069cb8d7-bbdd-47d3-ad8f-82ef4c269df1/statistic",
                String.class);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 404,
                    "message": "Item not found"
                }""", response.getBody(), false);
    }

    @Test
    void addsNodeForNodeWithoutParent() {
        // given
        HttpEntity<Map<String, Object>> request = createRequestWithRequestBody(Map.of(
                "items", List.of(
                        Map.of(
                                "type", "CATEGORY",
                                "name", "Товары",
                                "id", "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
                                "price", "",
                                "parentId", "")
                ),
                "updateDate", "2022-02-03T15:00:00.000Z"
        ));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity("/imports", request, String.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM shop_unit WHERE id = '069cb8d7-bbdd-47d3-ad8f-82ef4c269df1')",
                Boolean.class), true);
    }

    @Test
    void addsNodeForNodeWithParent() {
        // given
        var shopUnit1 = new ShopUnit(1L, UUID.fromString("069cb8d7-bbdd-47d3-ad8f-82ef4c269df1"), "Товары",
                Instant.parse("2022-02-03T15:00:00.000Z"), null, null, ShopUnitType.CATEGORY,
                null, new HashSet<>());
        shopUnitService.saveAllShopUnitsToDBTreeAndArchive(List.of(shopUnit1));

        HttpEntity<Map<String, Object>> request = createRequestWithRequestBody(Map.of(
                "items", List.of(
                        Map.of(
                                "type", "CATEGORY",
                                "name", "Телевизоры",
                                "id", "1cc0129a-2bfe-474c-9ee6-d435bf5fc8f2",
                                "price", "",
                                "parentId", "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1")
                ),
                "updateDate", "2022-02-03T16:00:00.000Z"
        ));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity("/imports", request, String.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM shop_unit WHERE id = '1cc0129a-2bfe-474c-9ee6-d435bf5fc8f2')",
                Boolean.class), true);
        assertEquals(jdbcTemplate.queryForObject("SELECT shop_unit_parent_id FROM shop_unit WHERE shop_unit_id = 2",
                Long.class), 1L);
    }

    @Test
    void addsNodeForNodesWhenParentCategoryAfterOffers() {
        // given
        HttpEntity<Map<String, Object>> request = createRequestWithRequestBody(Map.of(
                "items", List.of(
                        Map.of(
                                "type", "OFFER",
                                "name", "jPhone 13",
                                "id", "863e1a7a-1304-42ae-943b-179184c077e3",
                                "price", 79999L,
                                "parentId", "d515e43f-f3f6-4471-bb77-6b455017a2d2"),
                        Map.of(
                                "type", "OFFER",
                                "name", "Xomiа Readme 10",
                                "id", "b1d8fd7d-2ae3-47d5-b2f9-0f094af800d4",
                                "price", 59999L,
                                "parentId", "d515e43f-f3f6-4471-bb77-6b455017a2d2"),
                        Map.of(
                                "type", "CATEGORY",
                                "name", "Смартфоны",
                                "id", "d515e43f-f3f6-4471-bb77-6b455017a2d2",
                                "price", "",
                                "parentId", "")
                ),
                "updateDate", "2022-02-03T15:00:00.000Z"
        ));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity("/imports", request, String.class);

        // then
        var shopUnit1 = new ShopUnit(1L, UUID.fromString("d515e43f-f3f6-4471-bb77-6b455017a2d2"),
                "Смартфоны", Instant.parse("2022-02-02T12:00:00.000Z"), null, null,
                ShopUnitType.CATEGORY, null, new HashSet<>());
        var shopUnit2 = new ShopUnit(2L, UUID.fromString("863e1a7a-1304-42ae-943b-179184c077e3"),
                "jPhone 13", Instant.parse("2022-02-02T12:00:00.000Z"), shopUnit1.getId(), shopUnit1,
                ShopUnitType.OFFER, 79999L, new HashSet<>());
        var shopUnit3 = new ShopUnit(3L, UUID.fromString("b1d8fd7d-2ae3-47d5-b2f9-0f094af800d4"),
                "Xomiа Readme 10", Instant.parse("2022-02-02T12:00:00.000Z"), shopUnit1.getId(), shopUnit1,
                ShopUnitType.OFFER, 59999L, new HashSet<>());
        shopUnit1.setChildren(Set.of(shopUnit2, shopUnit3));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM shop_unit", Integer.class), 3);
        assertEquals(jdbcTemplate.queryForObject("SELECT price FROM shop_unit WHERE shop_unit_id = 1", Integer.class),
                (int) Math.floor((shopUnit2.getPrice() + shopUnit3.getPrice()) / 2));
    }

    @Test
    void addNodeThrowsExceptionWhenChangingType() throws JSONException {
        // given
        var shopUnit1 = new ShopUnit(1L, UUID.fromString("069cb8d7-bbdd-47d3-ad8f-82ef4c269df1"), "Товары",
                Instant.parse("2022-02-03T15:00:00.000Z"), null, null, ShopUnitType.CATEGORY,
                null, new HashSet<>());
        shopUnitService.saveAllShopUnitsToDBTreeAndArchive(List.of(shopUnit1));

        HttpEntity<Map<String, Object>> request = createRequestWithRequestBody(Map.of(
                "items", List.of(
                        Map.of(
                                "type", "OFFER",
                                "name", "Товары",
                                "id", "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
                                "price", 79999L,
                                "parentId", "")
                ),
                "updateDate", "2022-02-03T15:00:00.000Z"
        ));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity("/imports", request, String.class);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 400,
                    "message": "Validation Failed"
                }""", response.getBody(), false);
    }

    @Test
    void addNodeThrowsExceptionWhenParentIsOffer() throws JSONException {
        // given
        HttpEntity<Map<String, Object>> request = createRequestWithRequestBody(Map.of(
                "items", List.of(
                        Map.of(
                                "type", "OFFER",
                                "name", "Xomiа Readme 10",
                                "id", "b1d8fd7d-2ae3-47d5-b2f9-0f094af800d4",
                                "price", 59999L,
                                "parentId", ""),
                        Map.of(
                                "type", "CATEGORY",
                                "name", "Смартфоны",
                                "id", "d515e43f-f3f6-4471-bb77-6b455017a2d2",
                                "price", "",
                                "parentId", "b1d8fd7d-2ae3-47d5-b2f9-0f094af800d4")
                ),
                "updateDate", "2022-02-03T15:00:00.000Z"
        ));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity("/imports", request, String.class);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 400,
                    "message": "Validation Failed"
                }""", response.getBody(), false);
    }

    @Test
    void addNodeThrowsExceptionWhenNameIsEmpty() throws JSONException {
        // given
        HttpEntity<Map<String, Object>> request = createRequestWithRequestBody(Map.of(
                "items", List.of(
                        Map.of(
                                "type", "OFFER",
                                "name", "",
                                "id", "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
                                "price", 79999L,
                                "parentId", "")
                ),
                "updateDate", "2022-02-03T15:00:00.000Z"
        ));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity("/imports", request, String.class);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 400,
                    "message": "Validation Failed"
                }""", response.getBody(), false);
    }

    @Test
    void addNodeThrowsExceptionWhenCategoryHasPrice() throws JSONException {
        // given
        HttpEntity<Map<String, Object>> request = createRequestWithRequestBody(Map.of(
                "items", List.of(
                        Map.of(
                                "type", "CATEGORY",
                                "name", "Товары",
                                "id", "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
                                "price", 79999L,
                                "parentId", "")
                ),
                "updateDate", "2022-02-03T15:00:00.000Z"
        ));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity("/imports", request, String.class);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 400,
                    "message": "Validation Failed"
                }""", response.getBody(), false);
    }

    @Test
    void addNodeThrowsExceptionWhenOfferDoNotHavePrice() throws JSONException {
        // given
        HttpEntity<Map<String, Object>> request = createRequestWithRequestBody(Map.of(
                "items", List.of(
                        Map.of(
                                "type", "OFFER",
                                "name", "Товары",
                                "id", "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
                                "price", "",
                                "parentId", "")
                ),
                "updateDate", "2022-02-03T15:00:00.000Z"
        ));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity("/imports", request, String.class);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 400,
                    "message": "Validation Failed"
                }""", response.getBody(), false);
    }

    @Test
    void addNodeThrowsExceptionWhenOffersPriceNegative() throws JSONException {
        // given
        HttpEntity<Map<String, Object>> request = createRequestWithRequestBody(Map.of(
                "items", List.of(
                        Map.of(
                                "type", "OFFER",
                                "name", "Товары",
                                "id", "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
                                "price", -125000L,
                                "parentId", "")
                ),
                "updateDate", "2022-02-03T15:00:00.000Z"
        ));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity("/imports", request, String.class);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 400,
                    "message": "Validation Failed"
                }""", response.getBody(), false);
    }

    @Test
    void addNodeThrowsExceptionWhenItemsHaveSameId() throws JSONException {
        // given
        HttpEntity<Map<String, Object>> request = createRequestWithRequestBody(Map.of(
                "items", List.of(
                        Map.of(
                                "type", "OFFER",
                                "name", "Xomiа Readme 10",
                                "id", "b1d8fd7d-2ae3-47d5-b2f9-0f094af800d4",
                                "price", 59999L,
                                "parentId", ""),
                        Map.of(
                                "type", "CATEGORY",
                                "name", "Смартфоны",
                                "id", "b1d8fd7d-2ae3-47d5-b2f9-0f094af800d4",
                                "price", "",
                                "parentId", "")
                ),
                "updateDate", "2022-02-03T15:00:00.000Z"
        ));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity("/imports", request, String.class);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 400,
                    "message": "Validation Failed"
                }""", response.getBody(), false);
    }

    @Test
    void addNodeThrowsExceptionForWrongFormatDate() throws JSONException {
        // given
        HttpEntity<Map<String, Object>> request = createRequestWithRequestBody(Map.of(
                "items", List.of(
                        Map.of(
                                "type", "OFFER",
                                "name", "Товары",
                                "id", "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
                                "price", 135L,
                                "parentId", "")
                ),
                "updateDate", "2022-02-03 15:00:00"
        ));

        // when
        ResponseEntity<String> response = testRestTemplate.postForEntity("/imports", request, String.class);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 400,
                    "message": "Validation Failed"
                }""", response.getBody(), false);
    }

    @Test
    void deletesNodeById() {
        // given
        var shopUnit1 = new ShopUnit(1L, UUID.fromString("069cb8d7-bbdd-47d3-ad8f-82ef4c269df1"), "Товары",
                Instant.parse("2022-02-03T15:00:00.000Z"), null, null, ShopUnitType.CATEGORY,
                null, new HashSet<>());
        shopUnitService.saveAllShopUnitsToDBTreeAndArchive(List.of(shopUnit1));

        HttpEntity<Map<String, Object>> request = createRequest();

        // when
        ResponseEntity<String> response = testRestTemplate.exchange("/delete/069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
                HttpMethod.DELETE, request, String.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM shop_unit WHERE id = '069cb8d7-bbdd-47d3-ad8f-82ef4c269df1')",
                Boolean.class), false);
    }

    @Test
    void deleteNodeByIdThrowsExceptionForInvalidId() throws JSONException {
        // given
        HttpEntity<Map<String, Object>> request = createRequest();

        // when
        ResponseEntity<String> response = testRestTemplate.exchange("/delete/069cb8d7",
                HttpMethod.DELETE, request, String.class);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 400,
                    "message": "Validation Failed"
                }""", response.getBody(), false);
    }

    @Test
    void deleteNodeByIdThrowsExceptionForNotExistingId() throws JSONException {
        // given
        HttpEntity<Map<String, Object>> request = createRequest();

        // when
        ResponseEntity<String> response = testRestTemplate.exchange("/delete/069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
                HttpMethod.DELETE, request, String.class);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        JSONAssert.assertEquals("""
                {
                    "code": 404,
                    "message": "Item not found"
                }""", response.getBody(), false);
    }

    @Test
    void deletesNodeByIdWhenCategoryHasChildren() {
        // given
        var shopUnit1 = new ShopUnit(1L, UUID.fromString("069cb8d7-bbdd-47d3-ad8f-82ef4c269df1"), "Товары",
                Instant.parse("2022-02-03T15:00:00.000Z"), null, null, ShopUnitType.CATEGORY,
                null, new HashSet<>());
        var shopUnit2 = new ShopUnit(2L, UUID.fromString("d515e43f-f3f6-4471-bb77-6b455017a2d2"),
                "Смартфоны", Instant.parse("2022-02-02T12:00:00.000Z"), shopUnit1.getId(), shopUnit1,
                ShopUnitType.CATEGORY, null, new HashSet<>());
        var shopUnit3 = new ShopUnit(3L, UUID.fromString("863e1a7a-1304-42ae-943b-179184c077e3"),
                "jPhone 13", Instant.parse("2022-02-02T12:00:00.000Z"), shopUnit2.getId(), shopUnit2,
                ShopUnitType.OFFER, 79999L, new HashSet<>());
        var shopUnit4 = new ShopUnit(4L, UUID.fromString("b1d8fd7d-2ae3-47d5-b2f9-0f094af800d4"),
                "Xomiа Readme 10", Instant.parse("2022-02-02T12:00:00.000Z"), shopUnit2.getId(), shopUnit2,
                ShopUnitType.OFFER, 59999L, new HashSet<>());
        var shopUnit5 = new ShopUnit(5L, UUID.fromString("1cc0129a-2bfe-474c-9ee6-d435bf5fc8f2"),
                "Телевизоры", Instant.parse("2022-02-03T15:00:00.000Z"), shopUnit1.getId(), shopUnit1,
                ShopUnitType.CATEGORY, null, new HashSet<>());
        var shopUnit6 = new ShopUnit(6L, UUID.fromString("98883e8f-0507-482f-bce2-2fb306cf6483"),
                "Samson 70\" LED UHD Smart", Instant.parse("2022-02-03T12:00:00.000Z"), shopUnit5.getId(),
                shopUnit5, ShopUnitType.OFFER, 32999L, new HashSet<>());
        var shopUnit7 = new ShopUnit(7L, UUID.fromString("74b81fda-9cdc-4b63-8927-c978afed5cf4"),
                "Phyllis 50\" LED UHD Smarter", Instant.parse("2022-02-03T12:00:00.000Z"), shopUnit5.getId(),
                shopUnit5, ShopUnitType.OFFER, 49999L, new HashSet<>());
        var shopUnit8 = new ShopUnit(8L, UUID.fromString("73bc3b36-02d1-4245-ab35-3106c9ee1c65"),
                "Goldstar 65\" LED UHD LOL Very Smart", Instant.parse("2022-02-03T15:00:00.000Z"),
                shopUnit5.getId(), shopUnit5, ShopUnitType.OFFER, 69999L, new HashSet<>());
        shopUnitService.saveAllShopUnitsToDBTreeAndArchive(List.of(shopUnit1, shopUnit2, shopUnit3, shopUnit4, shopUnit5,
                shopUnit6, shopUnit7, shopUnit8));

        HttpEntity<Map<String, Object>> request = createRequest();

        // when
        ResponseEntity<String> response = testRestTemplate.exchange("/delete/069cb8d7-bbdd-47d3-ad8f-82ef4c269df1",
                HttpMethod.DELETE, request, String.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM shop_unit WHERE id = '069cb8d7-bbdd-47d3-ad8f-82ef4c269df1')",
                Boolean.class), false);
        assertEquals(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM shop_unit", Integer.class), 0);
    }

    private HttpEntity<Map<String, Object>> createRequestWithRequestBody(Map<String, Object> requestBody) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(requestBody, headers);
    }

    private HttpEntity<Map<String, Object>> createRequest() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }
}