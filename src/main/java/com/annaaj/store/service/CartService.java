package com.annaaj.store.service;

import com.annaaj.store.config.application.CommunityLeaderConfig;
import com.annaaj.store.exceptions.CartItemNotExistException;
import com.annaaj.store.model.Cart;
import com.annaaj.store.model.Product;
import com.annaaj.store.model.User;
import com.annaaj.store.repository.CartRepository;
import com.annaaj.store.dto.cart.AddToCartDto;
import com.annaaj.store.dto.cart.CartDto;
import com.annaaj.store.dto.cart.CartItemDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CommunityLeaderConfig communityLeaderConfig;

    public CartService(){}

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public void addToCart(AddToCartDto addToCartDto, Product product, User user){
        Cart cart = new Cart(product, addToCartDto.getQuantity(), user);
        cart = setCommunityLeaderIncentive(cart, product);
        cartRepository.save(cart);
    }


    public CartDto listCartItems(User user) {
        List<Cart> cartList = cartRepository.findAllByUserOrderByCreatedDateDesc(user);
        List<CartItemDto> cartItems = new ArrayList<>();
        for (Cart cart:cartList){
            CartItemDto cartItemDto = getDtoFromCart(cart);
            cartItems.add(cartItemDto);
        }
        double totalCost = 0;
        double totalIncentive = 0.0;
        for (CartItemDto cartItemDto :cartItems){
            totalCost += (cartItemDto.getProduct().getPrice()* cartItemDto.getQuantity());
            totalIncentive += cartItemDto.getIncentive();
        }
        CartDto cartDto = new CartDto(cartItems,totalCost, totalIncentive);
        return cartDto;
    }


    public static CartItemDto getDtoFromCart(Cart cart) {
        CartItemDto cartItemDto = new CartItemDto(cart);
        return cartItemDto;
    }


    public void updateCartItem(AddToCartDto cartDto, User user,Product product){
        Cart cart = cartRepository.getOne(cartDto.getId());
        cart.setQuantity(cartDto.getQuantity());
        cart.setCreatedDate(new Date());
        cart = setCommunityLeaderIncentive(cart, product);
        cartRepository.save(cart);
    }

    public void deleteCartItem(int id,int userId) throws CartItemNotExistException {
        if (!cartRepository.existsById(id))
            throw new CartItemNotExistException("Cart id is invalid : " + id);
        cartRepository.deleteById(id);

    }

    public Cart setCommunityLeaderIncentive(Cart cart, Product product) {
        double productProfit = product.getPrice() - product.getCostPrice();
        double incentive = productProfit * cart.getQuantity() * ( communityLeaderConfig.getIncentivePercentage() / 100.0 );
        cart.setIncentive(incentive);
        return cart;
    }

    public void deleteCartItems(int userId) {
        cartRepository.deleteAll();
    }


    public void deleteUserCartItems(User user) {
        cartRepository.deleteByUser(user);
    }
}


