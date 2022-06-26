package ru.vrn.vsu.csf.asashina.yandexproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vrn.vsu.csf.asashina.yandexproject.model.entity.ShopUnitTree;

import java.util.UUID;

@Repository
public interface ShopUnitTreeRepository extends JpaRepository<ShopUnitTree, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO shop_unit_tree(node_id, path)
            VALUES(
              :nodeId, text2ltree((
                SELECT ltree2text(path)
                FROM shop_unit_tree sut
                JOIN shop_unit su
                  ON su.shop_unit_id = sut.node_id
                  AND su.id = :parentId
              ) || '.' || :nodeIdAsString))""", nativeQuery = true)
    void saveShopUnitWithParent(@Param("nodeId") Long nodeId,
                                @Param("nodeIdAsString") String nodeIdString,
                                @Param("parentId") UUID parentId);

    @Modifying
    @Query(value = """
            UPDATE shop_unit_tree
            SET path = text2ltree((
                SELECT ltree2text(path)
                FROM shop_unit_tree sut
                JOIN shop_unit su
                  ON su.shop_unit_id = sut.node_id
                  AND su.id = :parentId) 
                || '.' || :nodeIdAsString)
            WHERE id = :id""", nativeQuery = true)
    void updateShopUnitWithParent(@Param("id") Long id,
                                  @Param("nodeIdAsString") String nodeIdString,
                                  @Param("parentId") UUID parentId);

    ShopUnitTree findShopUnitTreeByNodeId(Long nodeId);

    @Modifying
    @Query(value = """
            UPDATE shop_unit_tree
            SET path = :path || '.' || subpath(path, nlevel(text2ltree(:parentPath)))
            WHERE text2ltree(:parentPath) @> path""", nativeQuery = true)
    void saveNewParentForChildren(@Param("path") String path,
                                  @Param("parentPath") String parentPath);

    @Modifying
    @Query(value = """
            UPDATE shop_unit su
            SET price = (
              SELECT FLOOR(SUM(price) / COUNT(*))
              FROM shop_unit su
              JOIN shop_unit_tree sut
                ON sut.node_id = su.shop_unit_id
                AND sut.path <@ text2ltree(:path)
              WHERE unit_type = 'OFFER')
            FROM shop_unit_tree sut
            WHERE sut.node_id = su.shop_unit_id 
              AND ltree2text(path) = :path
              AND unit_type = 'CATEGORY'""", nativeQuery = true)
    void updateAveragePriceForCategories(@Param("path") String path);
}
