package com.annaaj.store.controller;

import com.annaaj.store.enums.Role;
import com.annaaj.store.exceptions.AuthenticationFailException;
import com.annaaj.store.exceptions.CartItemNotExistException;
import com.annaaj.store.exceptions.ProductNotExistException;
import com.annaaj.store.model.Product;
import com.annaaj.store.model.User;
import com.annaaj.store.repository.UserRepository;
import com.annaaj.store.service.AuthenticationService;
import com.annaaj.store.service.CartService;
import com.annaaj.store.service.ProductService;
import com.annaaj.store.common.ApiResponse;
import com.annaaj.store.dto.cart.AddToCartDto;
import com.annaaj.store.dto.cart.CartDto;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addToCart(@RequestBody AddToCartDto addToCartDto, @RequestParam("token") String token) throws
                                                                                                                              AuthenticationFailException,
                                                                                                                              ProductNotExistException {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        User user = authenticationService.getUser(token);
        Product product = productService.getProductById(addToCartDto.getProductId());
        System.out.println("product to add"+  product.getName());
        cartService.addToCart(addToCartDto, product, user);
        return new ResponseEntity<>(new ApiResponse(true, "Added to cart"), HttpStatus.CREATED);

    }
    @GetMapping("/")
    public ResponseEntity<CartDto> getCartItems(@RequestParam("token") String token) throws AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        User user = authenticationService.getUser(token);
        CartDto cartDto = cartService.listCartItems(user);
        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }
    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateCartItem(@RequestBody @Valid AddToCartDto cartDto,
                                                      @RequestParam("token") String token) throws AuthenticationFailException,ProductNotExistException {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        User user = authenticationService.getUser(token);
        Product product = productService.getProductById(cartDto.getProductId());
        cartService.updateCartItem(cartDto, user,product);
        return new ResponseEntity<>(new ApiResponse(true, "Product has been updated"), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{cartItemId}")
    public ResponseEntity<ApiResponse> deleteCartItem(@PathVariable("cartItemId") int itemID,@RequestParam("token") String token) throws AuthenticationFailException,
                                                                                                                                         CartItemNotExistException {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        int userId = authenticationService.getUser(token).getId();
        cartService.deleteCartItem(itemID, userId);
        return new ResponseEntity<>(new ApiResponse(true, "Item has been removed"), HttpStatus.OK);
    }

    @PostMapping("/add/user/{userId}")
    public ResponseEntity<ApiResponse> addToCartCommunityLeader(@RequestBody AddToCartDto addToCartDto,
                                                                @RequestParam("token") String token, @PathVariable("userId") Integer userId) throws
                                                                                                                              AuthenticationFailException,
                                                                                                                              ProductNotExistException {
        authenticationService.authenticate(token, Collections.singletonList(Role.communityLeader));
        authenticationService.authenticateCommunityLeader(token, userId);
        User user = userRepository.findById(userId).get();
        Product product = productService.getProductById(addToCartDto.getProductId());
        System.out.println("product to add"+  product.getName());
        cartService.addToCart(addToCartDto, product, user);
        return new ResponseEntity<>(new ApiResponse(true, "Added to cart"), HttpStatus.CREATED);

    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<CartDto> getCartItemsCommunityLeader(@RequestParam("token") String token,
                                                               @PathVariable("userId") Integer userId) throws AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.communityLeader));
        authenticationService.authenticateCommunityLeader(token, userId);
        User user = userRepository.findById(userId).get();
        CartDto cartDto = cartService.listCartItems(user);
        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }
    @PutMapping("/update/user/{userId}")
    public ResponseEntity<ApiResponse> updateCartItemCommunityLeader(@RequestBody @Valid AddToCartDto cartDto,
                                                      @RequestParam("token") String token, @PathVariable("userId") Integer userId) throws AuthenticationFailException,ProductNotExistException {
        authenticationService.authenticate(token, Collections.singletonList(Role.communityLeader));
        authenticationService.authenticateCommunityLeader(token, userId);
        User user = userRepository.findById(userId).get();
        Product product = productService.getProductById(cartDto.getProductId());
        cartService.updateCartItem(cartDto, user,product);
        return new ResponseEntity<>(new ApiResponse(true, "Product has been updated"), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{cartItemId}/user/{userId}")
    public ResponseEntity<ApiResponse> deleteCartItemCommunityLeader(@PathVariable("cartItemId") int itemID,
                                                                     @RequestParam("token") String token, @PathVariable("userId") Integer userId) throws AuthenticationFailException,
                                                                                                                                         CartItemNotExistException {
        authenticationService.authenticate(token, Collections.singletonList(Role.communityLeader));
        authenticationService.authenticateCommunityLeader(token, userId);
        int userId1 = userId;
        cartService.deleteCartItem(itemID, userId1);
        return new ResponseEntity<>(new ApiResponse(true, "Item has been removed"), HttpStatus.OK);
    }
}
