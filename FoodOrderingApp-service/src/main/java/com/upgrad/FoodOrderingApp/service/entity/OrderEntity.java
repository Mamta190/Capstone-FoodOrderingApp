package com.upgrad.FoodOrderingApp.service.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "orders")
@NamedQueries({
        @NamedQuery(name = "getOrdersByRestaurant", query = "select o from OrderEntity o where o.restaurant = :restaurant"),
        @NamedQuery(name = "getOrdersByCustomer", query = "select o from OrderEntity o where o.customer = :customer")
})
public class OrderEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "UUID")
    @Size(max = 200)
    @NotNull
    private String uuid;

    @Column(name = "BILL")
    @NotNull
    private double bill;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COUPON_ID")
    private CouponEntity coupon;

    @Column(name = "DISCOUNT")
    private double discount;

    @Column(name = "DATE")
    @NotNull
    private Timestamp date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PAYMENT_ID")
    private PaymentEntity payment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CUSTOMER_ID")
    @NotNull
    private CustomerEntity customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ADDRESS_ID")
    @NotNull
    private AddressEntity address;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RESTAURANT_ID")
    @NotNull
    private RestaurantEntity restaurant;

    public OrderEntity() {

    }

    public OrderEntity(String uuid, Double bill, CouponEntity couponEntity, Double discount, Timestamp orderDate, PaymentEntity paymentEntity, CustomerEntity customerEntity, AddressEntity addressEntity, RestaurantEntity restaurantEntity) {
        this.uuid = uuid;
        this.bill = bill;
        this.coupon = couponEntity;
        this.discount = discount;
        this.date = orderDate;
        this.payment = paymentEntity;
        this.customer = customerEntity;
        this.address = addressEntity;
        this.restaurant = restaurantEntity;

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public double getBill() {
        return bill;
    }

    public void setBill(double bill) {
        this.bill = bill;
    }

    public CouponEntity getCoupon() {
        return coupon;
    }

    public void setCoupon(CouponEntity coupon) {
        this.coupon = coupon;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public PaymentEntity getPayment() {
        return payment;
    }

    public void setPayment(PaymentEntity payment) {
        this.payment = payment;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }

    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(AddressEntity address) {
        this.address = address;
    }

    public RestaurantEntity getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(RestaurantEntity restaurant) {
        this.restaurant = restaurant;
    }
}
