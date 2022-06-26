package ru.vrn.vsu.csf.asashina.yandexproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vrn.vsu.csf.asashina.yandexproject.model.entity.ArchiveShopUnit;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ArchiveShopUnitRepository extends JpaRepository<ArchiveShopUnit, Long> {

    @Query(value = """
            SELECT *
            FROM archive_shop_unit
            WHERE id = :id
              AND date >= :dateStart AND date < :dateEnd""", nativeQuery = true)
    List<ArchiveShopUnit> getArchiveByIdInDateRange(@Param("id") UUID id,
                                                    @Param("dateStart") Instant dateStart,
                                                    @Param("dateEnd") Instant dateEnd);
}
