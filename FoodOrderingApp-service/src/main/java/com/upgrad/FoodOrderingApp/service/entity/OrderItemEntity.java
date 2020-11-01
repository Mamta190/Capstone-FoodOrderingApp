package com.upgrad.FoodOrderingApp.service.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "order_item")
@NamedQueries({
        @NamedQuery(name = "getItemsByOrders", query = "select o from OrderItemEntity o where o.order = :order"),
        @NamedQuery(name = "getOrderItemByOrder", query = "select o from OrderItemEntity o where o.order = :order")
})
public class OrderItemEntity implements Serializable {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ORDER_ID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ITEM_ID")
    @NotNull
    private ItemEntity item;

    @Column(name = "QUANTITY")
    @NotNull
    private Integer quantity;

    @Column(name = "PRICE")
    @NotNull
    private Integer price;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }

    public ItemEntity getItem() {
        return item;
    }

    public void setItem(ItemEntity item) {
        this.item = item;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

}
