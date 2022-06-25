package ru.vrn.vsu.csf.asashina.yandexproject.service;

import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vrn.vsu.csf.asashina.yandexproject.converter.ShopUnitConverter;
import ru.vrn.vsu.csf.asashina.yandexproject.exception.ObjectNotFoundException;
import ru.vrn.vsu.csf.asashina.yandexproject.exception.validation.IdException;
import ru.vrn.vsu.csf.asashina.yandexproject.exception.validation.PriceException;
import ru.vrn.vsu.csf.asashina.yandexproject.exception.validation.UpdatingTypeException;
import ru.vrn.vsu.csf.asashina.yandexproject.exception.validation.WrongParentTypeException;
import ru.vrn.vsu.csf.asashina.yandexproject.model.dto.ShopUnitCategoryDTO;
import ru.vrn.vsu.csf.asashina.yandexproject.model.dto.ShopUnitDTO;
import ru.vrn.vsu.csf.asashina.yandexproject.model.entity.ShopUnit;
import ru.vrn.vsu.csf.asashina.yandexproject.model.enumeration.ShopUnitType;
import ru.vrn.vsu.csf.asashina.yandexproject.model.request.ShopUnitImport;
import ru.vrn.vsu.csf.asashina.yandexproject.model.request.ShopUnitImportRequest;
import ru.vrn.vsu.csf.asashina.yandexproject.model.response.ShopUnitStaticResponse;
import ru.vrn.vsu.csf.asashina.yandexproject.repository.ShopUnitRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class ShopUnitService {

    private final ShopUnitRepository shopUnitRepository;

    private final ShopUnitTreeService shopUnitTreeService;
    private final ArchiveShopUnitService archiveShopUnitService;
    private final PathVariablesAndParamsValidationService pathVariablesAndParamsValidationService;

    private final ShopUnitConverter shopUnitConverter = Mappers.getMapper(ShopUnitConverter.class);

    public ShopUnitDTO getShopUnitById(UUID id) {
        var entity = findEntityById(id);
        if (entity == null) {
            throw new ObjectNotFoundException("Item not found");
        }
        return convertEntityToDTO(entity);
    }

    @Transactional
    public void saveAllShopUnitsToDBTreeAndArchive(List<ShopUnit> shopUnits) {
        shopUnits = shopUnitRepository.saveAll(shopUnits);
        archiveShopUnitService.saveNodesToArchive(shopUnits);
        shopUnitTreeService.saveAllShopUnitsToTree(shopUnits);
    }

    public void saveAllShopUnitsToTreeAndArchive(List<ShopUnit> shopUnits) {
        archiveShopUnitService.saveNodesToArchive(shopUnits);
        shopUnitTreeService.saveAllShopUnitsToTree(shopUnits);
    }

    @Transactional
    public void createShopUnitFromRequest(ShopUnitImportRequest request) {
        Instant updateDate = pathVariablesAndParamsValidationService.validateDate(request.getUpdateDate());

        Set<UUID> ids = new HashSet<>();
        List<ShopUnit> offers = new ArrayList<>();
        List<ShopUnit> categories = new LinkedList<>();
        int categoryIndex = -1;
        Map<UUID, Integer> idIndexCategoryMap = new HashMap<>();
        Map<Integer, UUID> indexIdCategoryMap = new HashMap<>();
        Map<UUID, List<UUID>> parentChildrenCategoryMap = new HashMap<>();
        Map<UUID, UUID> childParentCategoryMap = new HashMap<>();
        Map<UUID, UUID> childParentOfferMap = new HashMap<>();
        Map<UUID, ShopUnit> idEntityMap = new HashMap<>();

        List<ShopUnit> entities = new ArrayList<>();
        List<ShopUnit> validCategories = new ArrayList<>();

        //1 этап - валидация и сохранение данных по спискам
        for (ShopUnitImport shopUnitImport : request.getItems()) {
            if (ids.contains(UUID.fromString(shopUnitImport.getId()))) {
                throw new IdException("Validation Failed");
            }
            UUID id = UUID.fromString(shopUnitImport.getId());
            ids.add(id);
            itemValidation(shopUnitImport);

            //преобразование реквеста в сущность
            ShopUnit shopUnit = findEntityById(id);
            shopUnit = fromRequestToEntity(shopUnit, shopUnitImport, updateDate);

            //сохранение по спискам
            if (shopUnit.getUnitType() == ShopUnitType.CATEGORY) {
                categoryIndex++;
                savingCategoryToLists(shopUnit, idEntityMap, parentChildrenCategoryMap, childParentCategoryMap,
                        idIndexCategoryMap, indexIdCategoryMap, categoryIndex, categories);
            } else {
                savingOfferToLists(shopUnit, idEntityMap, childParentOfferMap, offers, entities, ids);
            }
        }

        if (!categories.isEmpty()) {
            //2 этап - сортировка категорий по связи родитель-ребенок
            sortingCategories(categories, parentChildrenCategoryMap, validCategories, idIndexCategoryMap, idEntityMap,
                    indexIdCategoryMap, categoryIndex, ids);

            //2.5 этап - сохранение тех категорий, у которых валидные родители
            if (!validCategories.isEmpty()) {
                var updatedCategories = shopUnitRepository.saveAll(validCategories);
                validCategories.clear();
                updatedCategories.forEach(category -> {
                    idEntityMap.put(category.getId(), category);
                    validCategories.add(category);
                });
            }

            if (!categories.isEmpty()) {
                //3 этап - сохранение категорий в базу данных и обновление сущностей в словаре
                saveCategoriesToDBAndUpdateMaps(categories, idEntityMap, childParentCategoryMap, validCategories,
                        ids);
            }
        }

        if (!offers.isEmpty()) {
            //4 этап - сохранение товаров в список сущностей с уже существующими родителями
            connectOffersToParentCategories(offers, childParentOfferMap, idEntityMap, entities, ids);
        }

        //5 этап - сохранение всех сущностей в бд, в дерево и в архив
        saveAllShopUnitsToTreeAndArchive(
                Stream.concat(validCategories.stream(), shopUnitRepository.saveAll(entities).stream())
                        .toList());
    }

    @Transactional
    public void deleteShopUnitById(UUID id) {
        var entity = findEntityById(id);
        if (entity == null) {
            throw new ObjectNotFoundException("Item not found");
        }
        shopUnitTreeService.deleteNodeFromTree(entity);
        shopUnitRepository.deleteById(entity.getShopUnitId());
    }

    public ShopUnitStaticResponse getLatestStatisticsOnOffers(Instant date) {
        List<ShopUnit> offers = shopUnitRepository.getLastUpdatedOffers(date.minusSeconds(24 * 60 * 60), date);
        return new ShopUnitStaticResponse(offers.isEmpty()
                ? new ArrayList<>()
                : offers.stream()
                        .map(shopUnitConverter::toShopUnitStaticUnitFromShopUnit)
                        .toList());
    }

    public ShopUnitStaticResponse getNodeStatisticsForEntityById(UUID id, Instant dateStart, Instant dateEnd) {
        var entity = findEntityById(id);
        if (entity == null) {
            throw new ObjectNotFoundException("Item not found");
        }
        return new ShopUnitStaticResponse(archiveShopUnitService.getArchiveForNodeById(entity, dateStart, dateEnd));
    }

    private ShopUnit fromRequestToEntity(ShopUnit entity, ShopUnitImport shopUnitImport, Instant updateDate) {
        if (entity == null) {
            return shopUnitConverter.fromRequestToEntity(shopUnitImport,
                    shopUnitImport.getParentId() == null
                            ? null
                            : findEntityById(UUID.fromString(shopUnitImport.getParentId())),
                    updateDate);
        } else {
            if (entity.getUnitType() != shopUnitImport.getType()) {
                throw new UpdatingTypeException("Validation Failed");
            }
            return shopUnitConverter.updateShopUnit(shopUnitImport, entity,
                    shopUnitImport.getParentId() == null
                            ? null
                            : findEntityById(UUID.fromString(shopUnitImport.getParentId())),
                    updateDate);
        }
    }

    /**
     * Распределяет товары по спискам. Далее эти списки понадобятся, чтобы отсортировать полученные на входе сущности
     * в порядке родитель-ребенок, т.к. они (сущности) изначально в произвольном порядке. Если товар без родителя, то он
     * сразу же сохраняется в список сущностей.
     *
     * @param entity              - сущность
     * @param idEntityMap         - словарь "id сущности (UUID) - сама сущность"
     * @param childParentOfferMap - словарь "id ребенка (UUID) - id родителя (UUID)"
     * @param offers              - список товаров
     * @param entities            - список сущностей
     * @param ids                 - множество id сущностей из запроса
     */
    private void savingOfferToLists(ShopUnit entity, Map<UUID, ShopUnit> idEntityMap, Map<UUID, UUID> childParentOfferMap,
                                    List<ShopUnit> offers, List<ShopUnit> entities, Set<UUID> ids) {
        if ((entity.getShopUnitParent() == null && entity.getParentId() == null)
                || (entity.getShopUnitParent() != null
                && entity.getParentId().equals(entity.getShopUnitParent().getId()) && !ids.contains(entity.getParentId()))
        ) {
            entities.add(entity);
        } else {
            offers.add(entity);
            childParentOfferMap.put(entity.getId(), entity.getParentId());
        }
        idEntityMap.put(entity.getId(), entity);
    }

    /**
     * Распределяет категорияя по спискам. Далее эти списки понадобятся, чтобы отсортировать полученные на входе сущности
     * в порядке родитель-ребенок, т.к. они (сущности) изначально в произвольном порядке.
     *
     * @param entity                 - сущность
     * @param idEntityMap            - словарь "id сущности (UUID) - сама сущность"
     * @param parentChildrenMap      - словарь "id родителя (UUID) - ids детей (UUID)"
     * @param childParentCategoryMap - словарь "id ребенка (UUID) - id родителя (UUID)"
     * @param idIndexCategoryMap     - словарь "id сущности (UUID) - индекс в связном списке" для категорий
     * @param indexIdCategoryMap     - словарь "индекс в связном списке - id сущности (UUID)" для категорий
     * @param categoryIndex          - последний индекс связного списка категорий
     * @param categories             - список категорий (связный список)
     */
    private void savingCategoryToLists(ShopUnit entity, Map<UUID, ShopUnit> idEntityMap,
                                       Map<UUID, List<UUID>> parentChildrenMap, Map<UUID, UUID> childParentCategoryMap,
                                       Map<UUID, Integer> idIndexCategoryMap, Map<Integer, UUID> indexIdCategoryMap,
                                       int categoryIndex, List<ShopUnit> categories) {
        categories.add(entity);
        idIndexCategoryMap.put(entity.getId(), categoryIndex);
        indexIdCategoryMap.put(categoryIndex, entity.getId());
        if (entity.getParentId() != null) {
            putValueToParentChildrenMapUsingKey(parentChildrenMap, entity.getParentId(), entity.getId());
            childParentCategoryMap.put(entity.getId(), entity.getParentId());
        }
        idEntityMap.put(entity.getId(), entity);
    }

    /**
     * Сортирует все категории, которые поступили с запроса, в порядке родитель-ребенок. Если категория не фигурирует
     * дальше в запросах, то она сохраняется в список всех сущностей.
     *
     * @param categories         - список категорий (связный список)
     * @param parentChildrenMap  - словарь "id родителя (UUID) - id детей (UUID)"
     * @param validCategories    - список категорий, где parentId соответсвует своей сущности
     * @param idIndexCategoryMap - словарь "id сущности (UUID) - индекс в связном списке" для категорий
     * @param idEntityMap        - словарь "id сущности (UUID) - сущность"
     * @param indexIdCategoryMap - словарь "индекс в связном списке - id сущности (UUID)" для категорий
     * @param categoryIndex      - последний индекс связного списка категорий
     * @param ids                - множество id из запроса
     */
    private void sortingCategories(List<ShopUnit> categories, Map<UUID, List<UUID>> parentChildrenMap,
                                   List<ShopUnit> validCategories, Map<UUID, Integer> idIndexCategoryMap,
                                   Map<UUID, ShopUnit> idEntityMap, Map<Integer, UUID> indexIdCategoryMap,
                                   int categoryIndex, Set<UUID> ids) {
        List<ShopUnit> categoriesCopy = new LinkedList<>();
        categoriesCopy.addAll(categories);
        for (ShopUnit category : categoriesCopy) {
            if (category.getShopUnitParent() != null
                    && category.getParentId().equals(category.getShopUnitParent().getId())
                    && !ids.contains(category.getParentId())) {
                removeCategoryFromMaps(category, categoryIndex, idIndexCategoryMap, indexIdCategoryMap, validCategories,
                        categories);
                continue;
            }
            //если категория ссылается на несуществующий id
            if (category.getParentId() != null && !ids.contains(category.getParentId())) {
                throw new IdException("Validation Failed");
            }

            if (category.getParentId() != null && idEntityMap.get(category.getParentId()).getUnitType() == ShopUnitType.OFFER) {
                throw new IdException("Validation Failed");
            }

            if (!parentChildrenMap.containsKey(category.getParentId())) {
                removeCategoryFromMaps(category, categoryIndex, idIndexCategoryMap, indexIdCategoryMap, validCategories,
                        categories);
                continue;
            }

            replaceCategoryInMaps(category, categories, idIndexCategoryMap, indexIdCategoryMap,
                    parentChildrenMap, categoryIndex);
        }
    }

    private void removeCategoryFromMaps(ShopUnit category, int categoryIndex, Map<UUID, Integer> idIndexCategoryMap,
                                        Map<Integer, UUID> indexIdCategoryMap, List<ShopUnit> entities,
                                        List<ShopUnit> categories) {
        categories.remove(category);
        categoryIndex--;
        int index = idIndexCategoryMap.remove(category.getId());
        indexIdCategoryMap.remove(index);

        //сдвигаем индексы с (parent, inf)
        for (int i = index + 1; i <= categoryIndex; i++) {
            var id = indexIdCategoryMap.get(index);
            indexIdCategoryMap.put(i - 1, id);
            idIndexCategoryMap.put(id, i - 1);
        }
        entities.add(category);
    }

    private void replaceCategoryInMaps(ShopUnit category, List<ShopUnit> categories,
                                       Map<UUID, Integer> idIndexCategoryMap, Map<Integer, UUID> indexIdCategoryMap,
                                       Map<UUID, List<UUID>> parentChildrenMap, int categoryIndex) {
        int index = findMinChildIndex(parentChildrenMap, category.getId(), categoryIndex, idIndexCategoryMap);

        if (index == -1 || index > idIndexCategoryMap.get(category.getId())) {
            return;
        }

        categories.remove(category);
        categories.add(index, category);
        idIndexCategoryMap.put(category.getId(), index);
        indexIdCategoryMap.put(index, category.getId());

        //сдвигаем индексы с (child, parent)
        for (int i = index + 1; i != idIndexCategoryMap.get(category.getId()); i++) {
            var id = indexIdCategoryMap.get(i);
            idIndexCategoryMap.put(id, i + 1);
            indexIdCategoryMap.put(i + 1, id);
        }
    }

    private int findMinChildIndex(Map<UUID, List<UUID>> parentChildrenMap, UUID parentId, int lastIndex,
                                  Map<UUID, Integer> idIndexCategoryMap) {
        var children = parentChildrenMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return -1;
        }
        int index = lastIndex + 1;
        for (UUID child : children) {
            if (idIndexCategoryMap.get(child) < index) {
                index = idIndexCategoryMap.get(child);
            }
            if (index == 0) {
                break;
            }
        }
        return index;
    }

    /**
     * Сохраняет категории в БД (дабы далее добавить их как родителей к другим сущностям) и обновляет словари с
     * сущностями.
     *
     * @param categories             - список категорий
     * @param idEntityMap            - словарь "id сущности (UUID) - сущность"
     * @param childParentCategoryMap - словарь "id ребенка (UUID) - id родителя (UUID)"
     * @param validAndSavedEntities  - список уже сохраненных в БД категорий
     * @param ids                    - множество id из запроса
     */
    private void saveCategoriesToDBAndUpdateMaps(List<ShopUnit> categories, Map<UUID, ShopUnit> idEntityMap,
                                                 Map<UUID, UUID> childParentCategoryMap,
                                                 List<ShopUnit> validAndSavedEntities, Set<UUID> ids) {
        var firstCategory = categories.get(0);
        if (ids.contains(firstCategory.getParentId())) {
            firstCategory.setShopUnitParent(idEntityMap.get(
                    childParentCategoryMap.get(firstCategory.getId()))
            );
        }
        var categoriesToSave = new ArrayList<>(List.of(categories.get(0)));
        var categoriesArray = categories.toArray(new ShopUnit[0]);
        for (int i = 1; i < categories.size(); i++) {
            if (categoriesArray[i].getParentId().equals(categoriesArray[i - 1].getId())) {
                var updatedCategories = shopUnitRepository.saveAll(categoriesToSave);
                categoriesToSave.clear();
                updatedCategories.forEach(category -> {
                    idEntityMap.put(category.getId(), category);
                    validAndSavedEntities.add(category);
                });
            }
            categoriesToSave.add(categoriesArray[i]);
            if (ids.contains(categoriesArray[i].getParentId())) {
                categoriesArray[i].setShopUnitParent(idEntityMap.get(
                        childParentCategoryMap.get(categoriesArray[i].getId()))
                );
            }
        }
        if (!categoriesToSave.isEmpty()) {
            var updatedCategories = shopUnitRepository.saveAll(categoriesToSave);
            updatedCategories.forEach(category -> {
                idEntityMap.put(category.getId(), category);
                validAndSavedEntities.add(category);
            });
        }
    }

    /**
     * Сохраняет товары с список сущностей, а также соединяет родителей с детьми (категории с соответствующим им товарам).
     *
     * @param offers              - список товаров (у которых есть родители)
     * @param childParentOfferMap - словарь "id ребенка (UUID) - id родителя (UUID)"
     * @param idEntityMap         - словарь "id сущности (UUID) - сущность"
     * @param entities            - список сущностей
     * @param ids                 - множество id из запроса
     */
    private void connectOffersToParentCategories(List<ShopUnit> offers, Map<UUID, UUID> childParentOfferMap,
                                                 Map<UUID, ShopUnit> idEntityMap, List<ShopUnit> entities,
                                                 Set<UUID> ids) {
        for (ShopUnit offer : offers) {

            //если товар ссылается на несуществующий id
            if (!ids.contains(offer.getParentId())) {
                throw new IdException("Validation Failed");
            }

            if (idEntityMap.get(offer.getParentId()).getUnitType() == ShopUnitType.OFFER) {
                throw new IdException("Validation Failed");
            }

            offer.setShopUnitParent(idEntityMap.get(
                    childParentOfferMap.get(offer.getId())));
            entities.add(offer);
        }
    }

    private void putValueToParentChildrenMapUsingKey(Map<UUID, List<UUID>> parentChildrenMap, UUID key,
                                                     UUID value) {
        if (parentChildrenMap.containsKey(key)) {
            parentChildrenMap.get(key).add(value);
            parentChildrenMap.put(key, parentChildrenMap.get(key));
        } else {
            parentChildrenMap.put(key, new ArrayList<>(List.of(value)));
        }
    }

    private ShopUnit findEntityById(UUID id) {
        return shopUnitRepository.findShopUnitById(id);
    }

    private void itemValidation(ShopUnitImport request) {
        pathVariablesAndParamsValidationService.validateId(request.getId());

        if (request.getParentId() != null && !request.getParentId().isEmpty()) {
            var parentId = pathVariablesAndParamsValidationService.validateId(request.getParentId());
            var parent = findEntityById(parentId);
            if (parent != null) {
                if (parent.getUnitType() == ShopUnitType.OFFER) {
                    throw new WrongParentTypeException("Validation Failed");
                }
            }
        }

        if (request.getType() == ShopUnitType.CATEGORY) {
            if (request.getPrice() != null) {
                throw new PriceException("Validation Failed");
            }
        } else {
            if (request.getPrice() == null || request.getPrice() < 0) {
                throw new PriceException("Validation Failed");
            }
        }
    }

    private ShopUnitDTO convertEntityToDTO(ShopUnit entity) {
        if (entity.getUnitType().equals(ShopUnitType.OFFER)) {
            return shopUnitConverter.toOfferDTO(entity);
        }
        return shopUnitConverter.toCategoryDTO(entity, new ShopUnitCategoryDTO());
    }
}
