package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CouponService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.OrderService;
import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("")
@CrossOrigin
public class OrderController {
    @Autowired
    private CouponService couponBusinessService;

    @Autowired
    private OrderService orderBusinessService;

    @Autowired
    private CustomerService customerService;

    @RequestMapping(method = RequestMethod.GET, path = "/order/coupon/{coupon_name}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CouponDetailsResponse> getCouponByCouponName(@RequestHeader("authorization") final String authorization,
                           @PathVariable("coupon_name") final String couponName)
                            throws AuthorizationFailedException, CouponNotFoundException {
        String[] authorizedData = authorization.split(" ");
        String accessToken;
        try {
            accessToken = authorizedData[1];
        }catch (ArrayIndexOutOfBoundsException e) {
            accessToken = authorizedData[0];
        }
        CouponEntity couponEntity =  couponBusinessService.getCouponByName(accessToken, couponName);

        CouponDetailsResponse couponDetailsResponse = new CouponDetailsResponse()
                .id(UUID.fromString(couponEntity.getUuid())).couponName(couponEntity.getCouponName())
                .percent(couponEntity.getPercent());

        return new ResponseEntity<CouponDetailsResponse>(couponDetailsResponse, HttpStatus.OK);
    }
    @RequestMapping(method = RequestMethod.GET, path = "/order", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CustomerOrderResponse> getPastOrdersOfUser(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException, AuthorizationFailedException {

        String accessToken = authorization.split("Bearer ")[1];

        CustomerAuthEntity customerEntity = customerService.getCustomer(accessToken);

        if (accessToken.equals(null)) {
            throw new AuthenticationFailedException("ATHR-001", "Customer is not Logged in.");
        }
        if (customerEntity.getLogoutAt() != null && accessToken != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }
        final ZonedDateTime customerSessionExpireTime = ZonedDateTime.now();
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.systemDefault());
        if (customerSessionExpireTime.compareTo(customerEntity.getExpiresAt()) < 0) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }

        List<OrderEntity> ordersEntities = orderBusinessService.getOrdersByCustomers(accessToken, customerEntity.getUuid());

        List<OrderList> orderLists = new LinkedList<>();

        if (ordersEntities != null) {
            for (OrderEntity ordersEntity : ordersEntities) {

                List<OrderItemEntity> orderItemEntities = orderBusinessService.getOrderItemsByOrder(ordersEntity);

                List<ItemQuantityResponse> itemQuantityResponseList = new LinkedList<>();
                orderItemEntities.forEach(orderItemEntity -> {
                    ItemQuantityResponseItem itemQuantityResponseItem = new ItemQuantityResponseItem()
                            .itemName(orderItemEntity.getItem().getItemName())
                            .itemPrice(orderItemEntity.getItem().getPrice())
                            .id(UUID.fromString(orderItemEntity.getItem().getUuid()))
                            .type(ItemQuantityResponseItem.TypeEnum.valueOf(orderItemEntity.getItem().getType().getValue()));
                    //Creating ItemQuantityResponse which will be added to the list
                    ItemQuantityResponse itemQuantityResponse = new ItemQuantityResponse()
                            .item(itemQuantityResponseItem).quantity(orderItemEntity.getQuantity())
                            .price(orderItemEntity.getPrice());
                    itemQuantityResponseList.add(itemQuantityResponse);
                });
                OrderListAddressState orderListAddressState = new OrderListAddressState()
                        .id(UUID.fromString(ordersEntity.getAddress().getState_id().getUuid()))
                        .stateName(ordersEntity.getAddress().getState_id().getState_name());

                OrderListAddress orderListAddress = new OrderListAddress().id(UUID.fromString(ordersEntity.getAddress().getUuid()))
                        .flatBuildingName(ordersEntity.getAddress().getFlat_buil_number())
                        .locality(ordersEntity.getAddress().getLocality()).city(ordersEntity.getAddress().getCity())
                        .pincode(ordersEntity.getAddress().getPincode()).state(orderListAddressState);
                OrderListCoupon orderListCoupon = new OrderListCoupon().couponName(ordersEntity.getCoupon().getCouponName())
                        .id(UUID.fromString(ordersEntity.getCoupon().getUuid())).percent(ordersEntity.getCoupon().getPercent());

                OrderListCustomer orderListCustomer = new OrderListCustomer()
                        .id(UUID.fromString(ordersEntity.getCustomer().getUuid()))
                        .firstName(ordersEntity.getCustomer().getFirstname())
                        .lastName(ordersEntity.getCustomer().getLastname())
                        .emailAddress(ordersEntity.getCustomer().getEmail())
                        .contactNumber(ordersEntity.getCustomer().getContactNumber());

                OrderListPayment orderListPayment = new OrderListPayment()
                        .id(UUID.fromString(ordersEntity.getPayment().getUuid()))
                        .paymentName(ordersEntity.getPayment().getPaymentName());

                OrderList orderList = new OrderList().id(UUID.fromString(ordersEntity.getUuid())).itemQuantities(itemQuantityResponseList)
                        .address(orderListAddress).bill(BigDecimal.valueOf(ordersEntity.getBill()))
                        .date(String.valueOf(ordersEntity.getDate())).discount(BigDecimal.valueOf(ordersEntity.getDiscount()))
                        .coupon(orderListCoupon).customer(orderListCustomer).payment(orderListPayment);
                orderLists.add(orderList);
            }
            CustomerOrderResponse customerOrderResponse = new CustomerOrderResponse()
                    .orders(orderLists);
            return new ResponseEntity<CustomerOrderResponse>(customerOrderResponse, HttpStatus.OK);
        } else {
            return new ResponseEntity<CustomerOrderResponse>(new CustomerOrderResponse(), HttpStatus.OK);
        }

    }

    @RequestMapping(method = RequestMethod.POST, path = "/order", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveOrderResponse> saveOrder(@RequestHeader("authorization") final String authorization,
                                                       @RequestBody(required = false) final SaveOrderRequest saveOrderRequest)
            throws AuthorizationFailedException, CouponNotFoundException, AddressNotFoundException,
            PaymentMethodNotFoundException, RestaurantNotFoundException, ItemNotFoundException
    {
        String[] authorizedData = authorization.split(" ");
        String accessToken;
        try {
            accessToken = authorizedData[1];
        }catch (ArrayIndexOutOfBoundsException e) {
            accessToken = authorizedData[0];
        }

        String couponUuid = saveOrderRequest.getCouponId().toString();
        String addressUuid = saveOrderRequest.getAddressId();
        String paymentUuid = saveOrderRequest.getPaymentId().toString();
        String restaurantUuid = saveOrderRequest.getRestaurantId().toString();
        BigDecimal bill = saveOrderRequest.getBill();

        List<ItemQuantity> itemList = saveOrderRequest.getItemQuantities();
        String itemUuid = itemList.get(0).getItemId().toString();
        OrderList orderList = new OrderList();
        OrderEntity ordersEntity = orderBusinessService.saveOrderList(accessToken,couponUuid,
                addressUuid,paymentUuid,restaurantUuid,itemUuid, bill);
        SaveOrderResponse saveOrderResponse = new SaveOrderResponse().id(ordersEntity.getUuid())
                .status("ORDER SUCCESSFULLY PLACED");
        return new ResponseEntity<SaveOrderResponse>(saveOrderResponse, HttpStatus.CREATED);
    }
}
