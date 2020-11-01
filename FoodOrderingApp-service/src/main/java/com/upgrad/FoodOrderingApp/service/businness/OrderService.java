package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import java.sql.Timestamp;
import java.time.Instant;

@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    OrderItemDao orderItemDao;

    @Autowired
    CouponDao couponDao;

    @Autowired
    AddressDao addressDao;

    @Autowired
    PaymentDao paymentDao;

    @Autowired
    ItemDao itemDao;

    @Autowired
    RestaurantDao restaurantDao;

    @Autowired
    CustomerAddressDao customerAddressDao;


    @Transactional(propagation = Propagation.REQUIRED)
    public List<OrderEntity> getOrdersByCustomers(String accessToken, String customerUuid){

        CustomerEntity customerEntity = customerDao.getCustomerByUuid(customerUuid);

        List<OrderEntity> ordersEntities = orderDao.getOrdersByCustomers(customerEntity);
        return ordersEntities;
    }

    public List<OrderItemEntity> getOrderItemsByOrder(OrderEntity ordersEntity) {
        List<OrderItemEntity> orderItemEntities = orderItemDao.getOrderItemsByOrder(ordersEntity);
        return orderItemEntities;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OrderEntity saveOrderList(final String accessToken, final String couponUuid,
                                     final String addressUuid, final String paymentUuid, final String restaurantUuid,
                                     final String itemUuid, final BigDecimal bill)
            throws AuthorizationFailedException, CouponNotFoundException ,
            AddressNotFoundException, PaymentMethodNotFoundException,
            RestaurantNotFoundException, ItemNotFoundException{
        CustomerAuthEntity customerAuthTokenEntity = customerDao.checkAuthToken(accessToken);
        if (customerAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }
        if(customerAuthTokenEntity.getLogoutAt()!=null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }
        final ZonedDateTime now = ZonedDateTime.now();
        if(customerAuthTokenEntity.getExpiresAt().isBefore(now) || customerAuthTokenEntity.getExpiresAt().isEqual(now)){
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }
        CouponEntity couponEntity = couponDao.getCouponByUUID(couponUuid);
        if(couponEntity == null) {
            throw new CouponNotFoundException("CPF-002", "No coupon by this id");
        }
        AddressEntity addressEntity = addressDao.getAddressByUUID(addressUuid);
        if(addressEntity == null) {
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }

        CustomerEntity customerEntity = customerAuthTokenEntity.getCustomer();
        CustomerAddressEntity customerAddressEntity = customerAddressDao.getEntityByCustomerID(customerEntity);
        if(addressEntity!=customerAddressEntity.getAddress()) {
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address");
        }

        PaymentEntity paymentEntity = paymentDao.getPaymentMethodByUUID(paymentUuid) ;
        if(paymentEntity == null ) {
            throw new PaymentMethodNotFoundException("PNF-002", "No payment method found by this id");
        }

        RestaurantEntity restaurantEntity = restaurantDao.getRestaurantByUuid(restaurantUuid);
        if(restaurantEntity == null) {
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");
        }

        ItemEntity itemEntity = itemDao.getItemByUuid(itemUuid);
        if(itemEntity == null ) {
            throw new ItemNotFoundException("INF-003", "No item by this id exist");
        }

        OrderEntity ordersEntity = new OrderEntity();
        ordersEntity.setCustomer(customerEntity);
        ordersEntity.setCoupon(couponEntity);
        ordersEntity.setAddress(addressEntity);
        ordersEntity.setPayment(paymentEntity);
        ordersEntity.setRestaurant(restaurantEntity);
        ordersEntity.setUuid(UUID.randomUUID().toString());
        ordersEntity.setBill(bill.doubleValue());
        Timestamp instant= Timestamp.from(Instant.now());
        ordersEntity.setDate(instant);

        OrderEntity savedOrderEntity = orderDao.saveORDER(ordersEntity);
        return savedOrderEntity;
    }
}

