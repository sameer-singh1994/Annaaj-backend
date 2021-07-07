package com.annaaj.store.dto.cart;

import java.util.List;

public class CartDto {
    private List<CartItemDto> cartItems;
    private double totalCost;
    private double totalIncentive;

    public CartDto(List<CartItemDto> cartItemDtoList, double totalCost, double totalIncentive) {
        this.cartItems = cartItemDtoList;
        this.totalCost = totalCost;
        this.totalIncentive = totalIncentive;
    }

    public List<CartItemDto> getcartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItemDto> cartItemDtoList) {
        this.cartItems = cartItemDtoList;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(int totalCost) {
        this.totalCost = totalCost;
    }

    public double getTotalIncentive() {
        return totalIncentive;
    }

    public void setTotalIncentive(double totalIncentive) {
        this.totalIncentive = totalIncentive;
    }
}
