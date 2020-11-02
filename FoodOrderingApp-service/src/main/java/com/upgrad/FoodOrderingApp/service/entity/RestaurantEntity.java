package com.upgrad.FoodOrderingApp.service.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurant")
@NamedQueries(
        {
                @NamedQuery(name = "restaurantsByRating", query = "select r from RestaurantEntity r order by r.customerRating desc"),
                @NamedQuery(name = "getAllRestaurants", query = "select r from RestaurantEntity r order by r.customerRating desc"),
                @NamedQuery(name = "getRestaurantByUuid", query = "select r from RestaurantEntity r where r.uuid = :uuid"),
                @NamedQuery(name = "restaurantByName", query = "select r from RestaurantEntity r where lower(r.restaurantName) like :restaurant_name_lower")
        }
)
public class RestaurantEntity implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "uuid")
    @NotNull
    @Size(max = 200)
    private String uuid;

    @Column(name = "restaurant_name")
    @NotNull
    @Size(max = 50)
    private String restaurantName;

    @Column(name = "photo_url")
    @NotNull
    @Size(max = 255)
    private String photoUrl;

    @Column(name = "customer_rating")
    private double customerRating;

    @Column(name = "average_price_for_two")
    @NotNull
    private Integer avgPrice;

    @Column(name = "number_of_customers_rated")
    @NotNull
    private Integer numberCustomersRated;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "address_id")
    private AddressEntity address;

    /*@OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "restaurant_category",
            joinColumns = @JoinColumn(name = "restaurant_id", referencedColumnName="id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName="id", nullable = false)
    )
    private Set<CategoryEntity> categoryEntities = new HashSet<>(); */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CategoryEntity> category = new ArrayList<CategoryEntity>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public List<CategoryEntity> getCategories() {
        return category;
    }

    public void setCategories(List<CategoryEntity> categories) {
        this.category = categories;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public double getCustomerRating() {
        return customerRating;
    }

    public void setCustomerRating(double customerRating) {
        this.customerRating = customerRating;
    }

    public Integer getNumberCustomersRated() {
        return numberCustomersRated;
    }

    public void setNumberCustomersRated(Integer numOfCustomersRated) {
        this.numberCustomersRated = numOfCustomersRated;
    }

    public Integer getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(Integer avgPrice) {
        this.avgPrice = avgPrice;
    }

    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(AddressEntity address) {
        this.address = address;
    }


    /*public Set<CategoryEntity> getCategoryEntities() {
        return categoryEntities;
    }

    public void setCategoryEntities(Set<CategoryEntity> categoryEntities) {
        this.categoryEntities = categoryEntities;
    } */
}
