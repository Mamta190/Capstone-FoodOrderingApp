package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import com.upgrad.FoodOrderingApp.service.businness.RestaurantService;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("")
@CrossOrigin
public class RestaurantController {
    @Autowired
    private RestaurantService restaurantBusinessService;

    @Autowired
    private AddressService addressBusinessService;

    @Autowired
    private StateBusinessService stateBusinessService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    CategoryService categoryBusinessService;

    @Autowired
    ItemService itemBusinessService;

    @Autowired
    CustomerService customerService;

    @RequestMapping(method = RequestMethod.GET, path = "/restaurant", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getAllRestaurants() {
        List<RestaurantList> restaurantList = new LinkedList<>();

        List<RestaurantEntity> restaurantEntities = restaurantBusinessService.restaurantsByRating();

        for (RestaurantEntity restaurantEntity : restaurantEntities) {
            RestaurantList restaurant = new RestaurantList();
            restaurant.setId(UUID.fromString(restaurantEntity.getUuid()));
            restaurant.setRestaurantName(restaurantEntity.getRestaurantName());
            restaurant.setPhotoURL(restaurant.getPhotoURL());
            restaurant.setCustomerRating(
                    new BigDecimal(Double.toString(restaurantEntity.getCustomerRating()))
                            .setScale(2, RoundingMode.HALF_DOWN));
            restaurant.setNumberCustomersRated(restaurantEntity.getNumberCustomersRated());
            restaurant.setAveragePrice(restaurantEntity.getAvgPrice());

            RestaurantDetailsResponseAddress address = new RestaurantDetailsResponseAddress();
            address.setId(UUID.fromString((restaurantEntity.getAddress().getUuid())));
            address.setFlatBuildingName(restaurantEntity.getAddress().getFlatBuilNo());
            address.setLocality(restaurantEntity.getAddress().getLocality());
            address.setCity(restaurantEntity.getAddress().getCity());
            address.setPincode(restaurantEntity.getAddress().getPincode());
            RestaurantDetailsResponseAddressState state = new RestaurantDetailsResponseAddressState();
            state.setId(UUID.fromString(restaurantEntity.getAddress().getState().getUuid()));
            state.setStateName(restaurantEntity.getAddress().getState().getStateName());
            address.setState(state);
            restaurant.setAddress(address);

            List<CategoryEntity> categoryEntityList =
                    categoryBusinessService.getCategoriesByRestaurant(restaurantEntity.getUuid());
            List<String> categoryNames = new ArrayList<>();
            for (CategoryEntity category : categoryEntityList) {
                categoryNames.add(category.getCategoryName());
            }
            Collections.sort(categoryNames);
            String categoryString = String.join(", ", categoryNames);
            restaurant.setCategories(categoryString);

            restaurantList.add(restaurant);
        }
        RestaurantListResponse restaurantListResponse = new RestaurantListResponse();
        restaurantListResponse.setRestaurants(restaurantList);
        return new ResponseEntity<>(restaurantListResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/name/{restaurant_name}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getRestaurantByName(@PathVariable("restaurant_name") final String restaurantName) throws RestaurantNotFoundException {

        List<RestaurantList> restaurantLists = new LinkedList<>();

        List<RestaurantEntity> restaurantEntities = restaurantBusinessService.restaurantsByName(restaurantName);

        for (RestaurantEntity restaurantEntity : restaurantEntities) {

            RestaurantList restaurant = new RestaurantList();
            restaurant.setId(UUID.fromString(restaurantEntity.getUuid()));
            restaurant.setRestaurantName(restaurantEntity.getRestaurantName());
            restaurant.setPhotoURL(restaurant.getPhotoURL());
            restaurant.setCustomerRating(
                    new BigDecimal(Double.toString(restaurantEntity.getCustomerRating()))
                            .setScale(2, RoundingMode.HALF_DOWN));
            restaurant.setNumberCustomersRated(restaurantEntity.getNumberCustomersRated());
            restaurant.setAveragePrice(restaurantEntity.getAvgPrice());

            RestaurantDetailsResponseAddress address = new RestaurantDetailsResponseAddress();
            address.setId(UUID.fromString((restaurantEntity.getAddress().getUuid())));
            address.setFlatBuildingName(restaurantEntity.getAddress().getFlatBuilNo());
            address.setLocality(restaurantEntity.getAddress().getLocality());
            address.setCity(restaurantEntity.getAddress().getCity());
            address.setPincode(restaurantEntity.getAddress().getPincode());
            RestaurantDetailsResponseAddressState state = new RestaurantDetailsResponseAddressState();
            state.setId(UUID.fromString(restaurantEntity.getAddress().getState().getUuid()));
            state.setStateName(restaurantEntity.getAddress().getState().getStateName());
            address.setState(state);
            restaurant.setAddress(address);

            List<CategoryEntity> categories = categoryBusinessService.getCategoriesByRestaurant(restaurantEntity.getUuid());
            List<String> categoryNames = new ArrayList<>();

            for (CategoryEntity categoryEntity : categories) {
                categoryNames.add(categoryEntity.getCategoryName());
            }
            Collections.sort(categoryNames);
            String category = String.join(", ", categoryNames);
            restaurant.setCategories(category);
            restaurantLists.add(restaurant);
        }
        RestaurantListResponse restaurantListResponse = new RestaurantListResponse();
        restaurantListResponse.setRestaurants(restaurantLists);
        return new ResponseEntity<>(restaurantListResponse, HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.GET, path = "/category/{category_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getRestaurantByCategoryId(@PathVariable("category_id") String categoryId) throws CategoryNotFoundException {

        List<RestaurantList> restaurantList = new LinkedList<>();
        // Get Restaurants information
        List<RestaurantEntity> restaurantEntityList =
                restaurantService.restaurantByCategory(categoryId);

        for (RestaurantEntity restaurantEntity : restaurantEntityList) {
            RestaurantList restaurant = new RestaurantList();
            restaurant.setId(UUID.fromString(restaurantEntity.getUuid()));
            restaurant.setRestaurantName(restaurantEntity.getRestaurantName());
            restaurant.setPhotoURL(restaurantEntity.getPhotoUrl());
            restaurant.setCustomerRating(
                    new BigDecimal(Double.toString(restaurantEntity.getCustomerRating()))
                            .setScale(2, RoundingMode.HALF_DOWN));
            restaurant.setNumberCustomersRated(restaurantEntity.getNumberCustomersRated());

            RestaurantDetailsResponseAddress address = new RestaurantDetailsResponseAddress();
            address.setId(UUID.fromString((restaurantEntity.getAddress().getUuid())));
            address.setFlatBuildingName(restaurantEntity.getAddress().getFlatBuilNo());
            address.setLocality(restaurantEntity.getAddress().getLocality());
            address.setCity(restaurantEntity.getAddress().getCity());
            address.setPincode(restaurantEntity.getAddress().getPincode());
            RestaurantDetailsResponseAddressState state = new RestaurantDetailsResponseAddressState();
            state.setId(UUID.fromString(restaurantEntity.getAddress().getState().getUuid()));
            state.setStateName(restaurantEntity.getAddress().getState().getStateName());
            address.setState(state);
            restaurant.setAddress(address);

            List<CategoryEntity> categoryEntityList =
                    categoryBusinessService.getCategoriesByRestaurant(restaurantEntity.getUuid());
            List<String> categoryNames = new ArrayList<>();
            for (CategoryEntity category : categoryEntityList) {
                categoryNames.add(category.getCategoryName());
            }
            Collections.sort(categoryNames);
            String categoryString = String.join(", ", categoryNames);
            restaurant.setCategories(categoryString);

            restaurantList.add(restaurant);
        }
        restaurantList =
                restaurantList.stream()
                        .sorted(
                                Comparator.comparing(
                                        RestaurantList::getRestaurantName, String.CASE_INSENSITIVE_ORDER))
                        .collect(Collectors.toList());
        RestaurantListResponse restaurantListResponse = new RestaurantListResponse();
        restaurantListResponse.setRestaurants(restaurantList);
        return new ResponseEntity<>(restaurantListResponse, HttpStatus.OK);
    }
}

