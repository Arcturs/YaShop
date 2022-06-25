package ru.vrn.vsu.csf.asashina.yandexproject.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.vrn.vsu.csf.asashina.yandexproject.model.dto.ShopUnitCategoryDTO;
import ru.vrn.vsu.csf.asashina.yandexproject.model.dto.ShopUnitDTO;
import ru.vrn.vsu.csf.asashina.yandexproject.model.dto.ShopUnitOfferDTO;
import ru.vrn.vsu.csf.asashina.yandexproject.model.entity.ArchiveShopUnit;
import ru.vrn.vsu.csf.asashina.yandexproject.model.entity.ShopUnit;
import ru.vrn.vsu.csf.asashina.yandexproject.model.enumeration.ShopUnitType;
import ru.vrn.vsu.csf.asashina.yandexproject.model.request.ShopUnitImport;
import ru.vrn.vsu.csf.asashina.yandexproject.model.response.ShopUnitStaticUnit;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mapper
public interface ShopUnitConverter {

    default ShopUnitCategoryDTO toCategoryDTO(ShopUnit entity, ShopUnitCategoryDTO dto) {
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDate(entity.getDate());
        dto.setParentId(entity.getParentId());
        dto.setType(entity.getUnitType());
        dto.setPrice(entity.getPrice());

        Set<ShopUnit> children = entity.getChildren();
        Set<ShopUnitDTO> childrenDTO = new HashSet<>();
        if (children != null) {
            children.forEach(child -> {
                if (child.getUnitType().equals(ShopUnitType.OFFER)) {
                    childrenDTO.add(toOfferDTO(child));
                } else {
                    childrenDTO.add(toCategoryDTO(child, new ShopUnitCategoryDTO()));
                }
            });
        }
        dto.setChildren(childrenDTO);
        return dto;
    }

    @Mappings({
            @Mapping(target = "type", source = "unitType"),
            @Mapping(target = "children", ignore = true)})
    ShopUnitOfferDTO toOfferDTO(ShopUnit entity);

    default ShopUnit fromRequestToEntity(ShopUnitImport request, ShopUnit parent, Instant updateDate) {
        var entity = new ShopUnit();
        entity.setChildren(new HashSet<>());
        return updateShopUnit(request, entity, parent, updateDate);
    }

    default ShopUnit updateShopUnit(ShopUnitImport request, ShopUnit oldEntity, ShopUnit parent, Instant updateDate) {
        oldEntity.setId(UUID.fromString(request.getId()));
        oldEntity.setName(request.getName());
        oldEntity.setDate(updateDate);
        oldEntity.setUnitType(request.getType());
        oldEntity.setPrice(request.getPrice());
        oldEntity.setParentId(request.getParentId() == null ? null : UUID.fromString(request.getParentId()));
        oldEntity.setShopUnitParent(parent);
        oldEntity.setChildren(oldEntity.getChildren());
        return oldEntity;
    }

    @Mappings({@Mapping(target = "type", source = "unitType")})
    ShopUnitStaticUnit toShopUnitStaticUnitFromShopUnit(ShopUnit entity);

    ArchiveShopUnit toArchiveShopUnit(ShopUnit entity);

    @Mappings({@Mapping(target = "type", source = "unitType")})
    ShopUnitStaticUnit toShopUnitStaticUnitFromArchive(ArchiveShopUnit entity);
}
