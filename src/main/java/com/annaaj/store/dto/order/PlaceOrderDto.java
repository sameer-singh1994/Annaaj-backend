package com.annaaj.store.dto.order;

import com.annaaj.store.model.User;
import com.annaaj.store.model.Order;

import javax.validation.constraints.NotNull;

public class PlaceOrderDto {
    private Integer id;
    private @NotNull User user;
    private @NotNull Double totalPrice;
    private @NotNull Double totalIncentive;

    public PlaceOrderDto() {
    }

    public PlaceOrderDto(Order order) {
        this.setId(order.getId());
        this.setUser(order.getUser());
        this.setTotalPrice(order.getTotalPrice());
        this.setTotalIncentive(order.getIncentive());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Double getTotalIncentive() {
        return totalIncentive;
    }

    public void setTotalIncentive(Double totalIncentive) {
        this.totalIncentive = totalIncentive;
    }
}
