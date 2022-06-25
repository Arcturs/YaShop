package ru.vrn.vsu.csf.asashina.yandexproject.service;

import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vrn.vsu.csf.asashina.yandexproject.converter.ShopUnitConverter;
import ru.vrn.vsu.csf.asashina.yandexproject.model.entity.ArchiveShopUnit;
import ru.vrn.vsu.csf.asashina.yandexproject.model.entity.ShopUnit;
import ru.vrn.vsu.csf.asashina.yandexproject.model.response.ShopUnitStaticResponse;
import ru.vrn.vsu.csf.asashina.yandexproject.model.response.ShopUnitStaticUnit;
import ru.vrn.vsu.csf.asashina.yandexproject.repository.ArchiveShopUnitRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ArchiveShopUnitService {

    private final ArchiveShopUnitRepository archiveShopUnitRepository;

    private final ShopUnitConverter shopUnitConverter = Mappers.getMapper(ShopUnitConverter.class);

    @Transactional
    public void saveNodesToArchive(List<ShopUnit> entities) {
        archiveShopUnitRepository.saveAll(
                entities.stream()
                        .map(shopUnitConverter::toArchiveShopUnit)
                        .toList());
    }

    public List<ShopUnitStaticUnit> getArchiveForNodeById(ShopUnit entity, Instant dateStart, Instant dateEnd) {
        List<ArchiveShopUnit> archive = archiveShopUnitRepository.getArchiveByIdInDateRange(entity.getId(), dateStart,
                dateEnd);
        return archive.isEmpty()
                ? null
                : archive.stream()
                    .map(shopUnitConverter::toShopUnitStaticUnitFromArchive)
                    .toList();
    }
}
