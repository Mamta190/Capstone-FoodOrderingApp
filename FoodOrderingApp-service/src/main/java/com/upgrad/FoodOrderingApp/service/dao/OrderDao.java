package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class OrderDao {

    @PersistenceContext
    private EntityManager entityManager;

    public List<OrderEntity> getOrderByRestaurant(RestaurantEntity restaurantEntity) {
        try {
            List<OrderEntity> ordersEntities = entityManager.createNamedQuery("getOrdersByRestaurant", OrderEntity.class).setParameter("restaurant", restaurantEntity).getResultList();
            return ordersEntities;
        } catch (NoResultException nre){
            return null;
        }
    }

    public List<OrderEntity> getOrdersByCustomers(CustomerEntity customerEntity) {
        try {
            List<OrderEntity> ordersEntities = entityManager.createNamedQuery("getOrdersByCustomer", OrderEntity.class).setParameter("customer", customerEntity).getResultList();
            return ordersEntities;
        } catch (NoResultException nre){
            return null;
        }
    }

    public OrderEntity saveORDER(OrderEntity ordersEntity) {
        try {
            entityManager.persist(ordersEntity);
            return ordersEntity;
        }catch (Exception e) {
            return null;
        }
    }
}
