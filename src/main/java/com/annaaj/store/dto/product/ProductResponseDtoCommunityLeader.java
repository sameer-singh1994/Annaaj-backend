package com.annaaj.store.dto.product;

import com.annaaj.store.model.Product;
import javax.validation.constraints.NotNull;

public class ProductResponseDtoCommunityLeader {

    private Integer id;
    private @NotNull String name;
    private @NotNull String imageURL;
    private @NotNull double price;
    private double projectedIncentive;
    private @NotNull String description;
    private @NotNull Integer categoryId;

    public ProductResponseDtoCommunityLeader(Product product) {
        this.setId(product.getId());
        this.setName(product.getName());
        this.setImageURL(product.getImageURL());
        this.setDescription(product.getDescription());
        this.setPrice(product.getPrice());
        this.setCategoryId(product.getCategory().getId());
    }

    public ProductResponseDtoCommunityLeader() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getProjectedIncentive() {
        return projectedIncentive;
    }

    public void setProjectedIncentive(double projectedIncentive) {
        this.projectedIncentive = projectedIncentive;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
}
