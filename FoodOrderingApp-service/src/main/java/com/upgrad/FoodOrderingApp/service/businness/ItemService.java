package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class ItemService {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    RestaurantItemDao restaurantItemDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    RestaurantDao restaurantDao;

    public List<ItemEntity> getItemsByPopularity(RestaurantEntity restaurantEntity) throws RestaurantNotFoundException {

        if (restaurantEntity == null) {
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");
        }
        List<OrderEntity> ordersEntities = orderDao.getOrderByRestaurant(restaurantEntity);

        List<ItemEntity> itemEntities = new LinkedList<>();

        ordersEntities.forEach(ordersEntity -> {
            List<OrderItemEntity> orderItemEntities = orderItemDao.getItemsByOrders(ordersEntity);
            orderItemEntities.forEach(orderItemEntity -> {
                itemEntities.add(orderItemEntity.getItem());
            });
        });
        Map<String, Integer> itemCountMap = new HashMap<>();
        itemEntities.forEach(itemEntity -> {
            Integer count = itemCountMap.get(itemEntity.getUuid());
            itemCountMap.put(itemEntity.getUuid(), (count == null) ? 1 : count + 1);
        });
        Map<String, Integer> sortedItemCountMap = itemDao.sortMapByValues(itemCountMap);

        List<ItemEntity> sortedItemEntites = new LinkedList<>();
        Integer count = 0;
        for (Map.Entry<String, Integer> item : sortedItemCountMap.entrySet()) {
            if (count < 5) {
                sortedItemEntites.add(itemDao.getItemByUuid(item.getKey()));
                count = count + 1;
            } else {
                break;
            }
        }

        return sortedItemEntites;
    }
    public List<ItemEntity> getItemsByCategoryAndRestaurant(String restaurantUuid, String categoryUuid) {

        RestaurantEntity restaurantEntity = restaurantDao.getRestaurantByUuid(restaurantUuid);

        CategoryEntity categoryEntity = categoryDao.getCategoryByUuid(categoryUuid);

        List<RestaurantItemEntity> restaurantItemEntities = restaurantItemDao.getItemByRestaurant(restaurantEntity);

        List<CategoryItemEntity> categoryItemEntities = categoryDao.getItemByCategory(categoryEntity);

        List<ItemEntity> itemEntities = new LinkedList<>();
        restaurantItemEntities.forEach(restaurantItemEntity -> {
            categoryItemEntities.forEach(categoryItemEntity -> {
                if (restaurantItemEntity.getItem().equals(categoryItemEntity.getItem())) {
                    itemEntities.add(restaurantItemEntity.getItem());
                }
            });
        });
        return itemEntities;
    }
}
