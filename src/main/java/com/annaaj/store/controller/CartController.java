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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

    @ApiOperation(value = "add item to cart, ROLE = user")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addToCart(@ApiParam(value = "dto for add to cart,"
        + " value of id field can be left as it is") @RequestBody AddToCartDto addToCartDto,
                                                 @ApiParam @RequestParam("token") String token) throws AuthenticationFailException, ProductNotExistException {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        User user = authenticationService.getUser(token);
        Product product = productService.getProductById(addToCartDto.getProductId());
        System.out.println("product to add"+  product.getName());
        cartService.addToCart(addToCartDto, product, user);
        return new ResponseEntity<>(new ApiResponse(true, "Added to cart"), HttpStatus.CREATED);

    }

    @ApiOperation(value = "get cart items, ROLE = user")
    @GetMapping("/")
    public ResponseEntity<CartDto> getCartItems(@ApiParam @RequestParam("token") String token) throws AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        User user = authenticationService.getUser(token);
        CartDto cartDto = cartService.listCartItems(user);
        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }

    @ApiOperation(value = "update cart item, ROLE = USER")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateCartItem(@ApiParam(value = "modified quantity cart item dto") @RequestBody @Valid AddToCartDto cartDto,
                                                      @ApiParam @RequestParam("token") String token) throws AuthenticationFailException,ProductNotExistException {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        User user = authenticationService.getUser(token);
        Product product = productService.getProductById(cartDto.getProductId());
        cartService.updateCartItem(cartDto, user,product);
        return new ResponseEntity<>(new ApiResponse(true, "Product has been updated"), HttpStatus.OK);
    }

    @ApiOperation(value = "delete cart item, ROLE = USER")
    @DeleteMapping("/delete/{cartItemId}")
    public ResponseEntity<ApiResponse> deleteCartItem(
        @ApiParam(value = "id of the item to be removed") @PathVariable("cartItemId") int itemID,
        @ApiParam @RequestParam("token") String token) throws AuthenticationFailException, CartItemNotExistException {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        int userId = authenticationService.getUser(token).getId();
        cartService.deleteCartItem(itemID, userId);
        return new ResponseEntity<>(new ApiResponse(true, "Item has been removed"), HttpStatus.OK);
    }

    @ApiOperation(value = "add to cart, ROLE = COMMUNITY_LEADER")
    @PostMapping("/add/user/{userId}")
    public ResponseEntity<ApiResponse> addToCartCommunityLeader(
        @ApiParam(value = "dto for add to cart, value of id field can be left as it is") @RequestBody AddToCartDto addToCartDto,
        @ApiParam @RequestParam("token") String token,
        @ApiParam(value = "id of the user on whose behalf this op is being performed") @PathVariable("userId") Integer userId) throws
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

    @ApiOperation(value = "get cart items, ROLE = COMMUNITY_LEADER")
    @GetMapping("/user/{userId}")
    public ResponseEntity<CartDto> getCartItemsCommunityLeader(@ApiParam @RequestParam("token") String token,
                                                               @ApiParam(value = "id of the user on whose behalf this op is being performed") @PathVariable("userId") Integer userId) throws AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.communityLeader));
        authenticationService.authenticateCommunityLeader(token, userId);
        User user = userRepository.findById(userId).get();
        CartDto cartDto = cartService.listCartItems(user);
        return new ResponseEntity<>(cartDto, HttpStatus.OK);
    }

    @ApiOperation(value = "get cart items, ROLE = COMMUNITY_LEADER")
    @PutMapping("/update/user/{userId}")
    public ResponseEntity<ApiResponse> updateCartItemCommunityLeader(@ApiParam(value = "modified quantity cart item dto") @RequestBody @Valid AddToCartDto cartDto,
                                                        @ApiParam @RequestParam("token") String token,
                                                        @ApiParam(value = "id of the user on whose behalf this op is being performed") @PathVariable("userId") Integer userId) throws AuthenticationFailException,ProductNotExistException {
        authenticationService.authenticate(token, Collections.singletonList(Role.communityLeader));
        authenticationService.authenticateCommunityLeader(token, userId);
        User user = userRepository.findById(userId).get();
        Product product = productService.getProductById(cartDto.getProductId());
        cartService.updateCartItem(cartDto, user,product);
        return new ResponseEntity<>(new ApiResponse(true, "Product has been updated"), HttpStatus.OK);
    }

    @ApiOperation(value = "get cart items, ROLE = COMMUNITY_LEADER")
    @DeleteMapping("/delete/{cartItemId}/user/{userId}")
    public ResponseEntity<ApiResponse> deleteCartItemCommunityLeader(@ApiParam(value = "id of the item to be removed") @PathVariable("cartItemId") int itemID,
                                                                     @ApiParam @RequestParam("token") String token,
                                                                     @ApiParam(value = "id of the user on whose behalf this op is being performed") @PathVariable("userId") Integer userId) throws AuthenticationFailException,
                                                                                                                                         CartItemNotExistException {
        authenticationService.authenticate(token, Collections.singletonList(Role.communityLeader));
        authenticationService.authenticateCommunityLeader(token, userId);
        int userId1 = userId;
        cartService.deleteCartItem(itemID, userId1);
        return new ResponseEntity<>(new ApiResponse(true, "Item has been removed"), HttpStatus.OK);
    }
}
