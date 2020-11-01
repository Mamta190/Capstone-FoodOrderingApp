package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.ItemList;
import com.upgrad.FoodOrderingApp.api.model.ItemListResponse;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.businness.RestaurantService;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("")
@CrossOrigin
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private RestaurantService restaurantBusinessService;

    @RequestMapping(method = RequestMethod.GET, path = "/item/restaurant/{restaurant_id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ItemListResponse> getTopFiveItemsByRestaurantId(@PathVariable("restaurant_id") final String restaurantUuid) throws RestaurantNotFoundException {

        RestaurantEntity restaurantEntity = restaurantBusinessService.restaurantByUUID(restaurantUuid);

        List<ItemEntity> itemEntities = itemService.getItemsByPopularity(restaurantEntity);

        ItemListResponse itemListResponse = new ItemListResponse();

        itemEntities.forEach(itemEntity -> {
            ItemList itemList = new ItemList().id(UUID.fromString(itemEntity.getUuid()))
                    .itemName(itemEntity.getItemName()).price(itemEntity.getPrice())
                    .itemType(ItemList.ItemTypeEnum.fromValue(itemEntity.getType().getValue()));
            itemListResponse.add(itemList);
        });
        return new ResponseEntity<ItemListResponse>(itemListResponse, HttpStatus.OK);
    }
}
