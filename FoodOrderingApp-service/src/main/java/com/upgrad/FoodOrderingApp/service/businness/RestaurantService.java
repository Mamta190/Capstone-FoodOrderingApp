package com.upgrad.FoodOrderingApp.service.businness;


import com.upgrad.FoodOrderingApp.service.dao.CategoryDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantCategoryDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantCategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantService {
    @Autowired
    private RestaurantDao restaurantDao;

    @Autowired
    CustomerDao customerDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    RestaurantCategoryDao restaurantCategoryDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public List<RestaurantEntity> getAllRestaurants() {
        return restaurantDao.getAllRestaurants();
    }

    public List<RestaurantEntity> restaurantsByName(String restaurantName) throws RestaurantNotFoundException {

        if (restaurantName == null || restaurantName == "") {
            throw new RestaurantNotFoundException("RNF-003", "Restaurant name field should not be empty");
        }

        List<RestaurantEntity> restaurantEntities = restaurantDao.restaurantByName(restaurantName);
        return restaurantEntities;
    }

    public List<RestaurantEntity> restaurantsByRating() {

        //Calls restaurantsByRating of restaurantDao to get list of RestaurantEntity
        List<RestaurantEntity> restaurantEntities = restaurantDao.restaurantsByRating();
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
    public RestaurantEntity updateRestaurantRating(RestaurantEntity restaurantEntity, Double customerRating) throws InvalidRatingException {
        if(!customerDao.isValidCustomerRating(customerRating.toString())){ //Checking for the rating to be valid
            throw new InvalidRatingException("IRE-001","Restaurant should be in the range of 1 to 5");
        }
        //Finding the new Customer rating adn updating it.
        DecimalFormat format = new DecimalFormat("##.0"); //keeping format to one decimal
        double restaurantRating = restaurantEntity.getCustomerRating();
        Integer restaurantNoOfCustomerRated = restaurantEntity.getNumberCustomersRated();
        restaurantEntity.setNumberCustomersRated(restaurantNoOfCustomerRated+1);

        //calculating the new customer rating as per the given data and formula
        double newCustomerRating = (restaurantRating*(restaurantNoOfCustomerRated.doubleValue())+customerRating)/restaurantEntity.getNumberCustomersRated();

        restaurantEntity.setCustomerRating(Double.parseDouble(format.format(newCustomerRating)));

        //Updating the restautant in the db using the method updateRestaurantRating of restaurantDao.
        RestaurantEntity updatedRestaurantEntity = restaurantDao.updateRestaurantRating(restaurantEntity);

        return updatedRestaurantEntity;

    }

    public List<RestaurantEntity> restaurantByCategory(final String categoryId)
            throws CategoryNotFoundException {
        if (categoryId == null || categoryId == "") {
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }

        CategoryEntity categoryEntity = categoryDao.getCategoryById(categoryId);
        if (categoryEntity == null) {
            throw new CategoryNotFoundException("CNF-002", "No category by this id");
        }
        List<RestaurantCategoryEntity> restaurantCategoryEntities = restaurantCategoryDao.getRestaurantsByCategoryId(categoryEntity);

        if (restaurantCategoryEntities.isEmpty()) {
            return null;
        }
        List<RestaurantEntity> restaurantEntities = new ArrayList<RestaurantEntity>();
        for (RestaurantCategoryEntity rc : restaurantCategoryEntities) {
            restaurantEntities.add(rc.getRestaurant());
        }
        return restaurantEntities;
    }

    public List<RestaurantCategoryEntity> getRestaurantCategories(RestaurantEntity restaurantEntity) {
        return restaurantCategoryDao.getRestaurantCategories(restaurantEntity);
    }

}