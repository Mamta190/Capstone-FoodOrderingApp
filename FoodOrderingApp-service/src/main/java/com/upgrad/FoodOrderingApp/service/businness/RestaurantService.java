package com.upgrad.FoodOrderingApp.service.businness;


import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class RestaurantService {
    @Autowired
    private RestaurantDao restaurantDao;

    @Autowired
    CustomerDao customerDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public List<RestaurantEntity> getAllRestaurants() {
        return restaurantDao.getAllRestaurants();
    }

    public List<RestaurantEntity> restaurantByName(String restaurantName) throws RestaurantNotFoundException {

        if (restaurantName == null || restaurantName == "") {
            throw new RestaurantNotFoundException("RNF-003", "Restaurant name field should not be empty");
        }

        List<RestaurantEntity> restaurantEntities = restaurantDao.restaurantByName(restaurantName);
        return restaurantEntities;
    }

    public RestaurantEntity restaurantByUUID(String uuid) throws RestaurantNotFoundException {
        if (uuid == null || uuid == "") {
            throw new RestaurantNotFoundException("RNF-002", "Restaurant id field should not be empty");
        }
        RestaurantEntity restaurantEntity = restaurantDao.getRestaurantByUuid(uuid);

        if (restaurantEntity == null) {
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");
        }

        return restaurantEntity;
        /* List<RestaurantItemEntity> restaurantItemEntities = restaurantItemDao.getItemByRestaurant(restaurantEntity);

        if(restaurantItemEntities.isEmpty()) {
        return null;
    }
    List<RestaurantItemEntity> restaurantEntities = new ArrayList<RestaurantItemEntity>();
        for(RestaurantItemEntity rc: restaurantItemEntities) {
        restaurantEntities.add(rc.getItem());
    }restaurantEntity
        return restaurantEntities;
}*/
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public RestaurantEntity updateRestaurantDetails(RestaurantEntity restaurantEntity, String authorization) throws RestaurantNotFoundException,InvalidRatingException,AuthorizationFailedException {
        CustomerAuthEntity userAuthToken = restaurantDao.getUserAuthToken(authorization);
        RestaurantEntity existingRestaurantEntity =  restaurantDao.getRestaurantByUuid(restaurantEntity.getUuid());
        if(userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001","Customer is not Logged in.");
        } else if(userAuthToken.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002","Customer is logged out. Log in again to access this endpoint.");
        } else if(userAuthToken.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthorizationFailedException("ATHR-003","Your session is expired. Log in again to access this endpoint.");
        } else if(restaurantEntity.getUuid() == "") {
            throw new RestaurantNotFoundException("RNF-002", "Restaurant id field should not be empty");
        } else if(existingRestaurantEntity == null) {
            throw new RestaurantNotFoundException("RNF-001","No restaurant by this id");
        } else if(restaurantEntity.getCustomerRating() == null || !(restaurantEntity.getCustomerRating().compareTo(new BigDecimal(0)) > 0 && restaurantEntity.getCustomerRating().compareTo(new BigDecimal(6)) < 0 ) ) {
            throw new InvalidRatingException("IRE-001","Restaurant should be in the range of 1 to 5");
        }
        int numOfCustomersRated = existingRestaurantEntity.getNumOfCustomersRated() + 1;
        BigDecimal avgCustRating = existingRestaurantEntity.getCustomerRating().add(restaurantEntity.getCustomerRating()).divide(new BigDecimal(2));
        restaurantEntity.setCustomerRating(avgCustRating);
        restaurantEntity.setNumOfCustomersRated(numOfCustomersRated);
        restaurantEntity.setAddress(existingRestaurantEntity.getAddress());
        restaurantEntity.setAvgPriceForTwo(existingRestaurantEntity.getAvgPriceForTwo());
        restaurantEntity.setId(existingRestaurantEntity.getId());
        restaurantEntity.setPhotoUrl(existingRestaurantEntity.getPhotoUrl());
        restaurantEntity.setCategories(existingRestaurantEntity.getCategories());
        restaurantEntity.setRestaurantName(existingRestaurantEntity.getRestaurantName());
        return restaurantDao.updateRestaurantDetails(restaurantEntity);
    }

}
