package ru.vrn.vsu.csf.asashina.yandexproject.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vrn.vsu.csf.asashina.yandexproject.model.entity.ShopUnit;
import ru.vrn.vsu.csf.asashina.yandexproject.model.entity.ShopUnitTree;
import ru.vrn.vsu.csf.asashina.yandexproject.model.enumeration.ShopUnitType;
import ru.vrn.vsu.csf.asashina.yandexproject.repository.ShopUnitTreeRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ShopUnitTreeService {

    private final ShopUnitTreeRepository shopUnitTreeRepository;

    @Transactional
    public void saveShopUnitToTree(ShopUnit entity) {
        ShopUnitTree shopUnitTree;
        if (entity.getShopUnitParent() != null) {
            shopUnitTree = shopUnitTreeRepository.findShopUnitTreeByNodeId(entity.getShopUnitId());

            //добавление нового узла в дерево
            addNewNodeWithParentToTree(entity, shopUnitTree);
            if (entity.getUnitType() != ShopUnitType.OFFER) {
                return;
            }

            //обновления путей у детей
            shopUnitTree = shopUnitTreeRepository.findShopUnitTreeByNodeId(entity.getShopUnitId());
            String parentPath = getParentPath(shopUnitTree);
            updateChildrenPaths(entity, shopUnitTree, parentPath);

            //обновление средней цены тех категорий, которые содержат в пути новый узел
            updateCategoryPriceWithFollowingChildNode(parentPath);
        } else {
            shopUnitTree = shopUnitTreeRepository.findShopUnitTreeByNodeId(entity.getShopUnitId());
            addNewNodeWithoutParentToTree(entity, shopUnitTree);
        }
    }

    public void saveAllShopUnitsToTree(List<ShopUnit> shopUnits) {
        shopUnits.forEach(this::saveShopUnitToTree);
    }

    @Transactional
    public void deleteNodeFromTree(ShopUnit entity) {
        var shopUnitTree = shopUnitTreeRepository.findShopUnitTreeByNodeId(entity.getShopUnitId());
        shopUnitTreeRepository.delete(shopUnitTree);
        String parentPath = getParentPath(shopUnitTree);

        if (!parentPath.equals("")) {
            //обновление средней цены тех категорий, которые содержали в пути удаленный узел
            updateCategoryPriceWithFollowingChildNode(parentPath);
        }
    }

    private String getParentPath(ShopUnitTree shopUnitTree) {
        var lastIndex = shopUnitTree.getPath().lastIndexOf('.');
        if (lastIndex == -1) {
            return "";
        }
        return shopUnitTree.getPath().substring(0, lastIndex);
    }

    private void addNewNodeWithParentToTree(ShopUnit entity, ShopUnitTree shopUnitTree) {
        if (shopUnitTree == null) {
            shopUnitTreeRepository.saveShopUnitWithParent(entity.getShopUnitId(),
                    Long.toString(entity.getShopUnitId()), entity.getParentId());
        } else {
            shopUnitTreeRepository.updateShopUnitWithParent(shopUnitTree.getId(),
                    Long.toString(shopUnitTree.getNodeId()), entity.getParentId());
        }
    }

    private void addNewNodeWithoutParentToTree(ShopUnit entity, ShopUnitTree shopUnitTree) {
        if (shopUnitTree == null) {
            shopUnitTree = new ShopUnitTree();
            shopUnitTree.setNodeId(entity.getShopUnitId());
        }
        shopUnitTree.setPath(Long.toString(entity.getShopUnitId()));
        shopUnitTreeRepository.save(shopUnitTree);
    }

    private void updateChildrenPaths(ShopUnit entity, ShopUnitTree shopUnitTree, String parentPath) {
        if (entity.getChildren() != null && !entity.getChildren().isEmpty()) {
            shopUnitTreeRepository.saveNewParentForChildren(shopUnitTree.getPath(), parentPath);
        }
    }

    private void updateCategoryPriceWithFollowingChildNode(String parentPath) {
        String[] ancestorsPaths = parentPath.split("\\.");
        if (ancestorsPaths.length == 0) {
            shopUnitTreeRepository.updateAveragePriceForCategories(parentPath);
            return;
        }
        StringBuilder sb = new StringBuilder("");
        for (String ancestor : ancestorsPaths) {
            sb.append(ancestor);
            shopUnitTreeRepository.updateAveragePriceForCategories(sb.toString());
            sb.append(".");
        }
    }
}
