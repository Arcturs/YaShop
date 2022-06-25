package ru.vrn.vsu.csf.asashina.yandexproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vrn.vsu.csf.asashina.yandexproject.model.entity.ShopUnit;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShopUnitRepository extends JpaRepository<ShopUnit, Long> {

    ShopUnit findShopUnitById(UUID id);

    @Query(value = """
                SELECT *
                FROM shop_unit
                WHERE unit_type = 'OFFER'
                  AND id IN (
                    SELECT DISTINCT id
                    FROM archive_shop_unit
                    WHERE (date >= :fromDate AND date <= :toDate))""", nativeQuery = true)
    List<ShopUnit> getLastUpdatedOffers(@Param("fromDate") Instant fromDate,
                                        @Param("toDate") Instant toDate);
}
