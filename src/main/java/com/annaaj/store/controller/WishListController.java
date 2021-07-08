package com.annaaj.store.controller;


import com.annaaj.store.enums.Role;
import com.annaaj.store.service.AuthenticationService;
import com.annaaj.store.service.ProductService;
import com.annaaj.store.common.ApiResponse;
import com.annaaj.store.dto.product.ProductDto;
import com.annaaj.store.model.Product;
import com.annaaj.store.model.User;
import com.annaaj.store.model.WishList;
import com.annaaj.store.service.WishListService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/wishlist")
public class WishListController {

        @Autowired
        private WishListService wishListService;

        @Autowired
        private AuthenticationService authenticationService;

        @ApiOperation(value = "get wishlist(only for user), ROLE = USER")
        @GetMapping("/{token}")
        public ResponseEntity<List<ProductDto>> getWishList(@ApiParam @PathVariable("token") String token) {
                authenticationService.authenticate(token, Collections.singletonList(Role.user));
                int user_id = authenticationService.getUser(token).getId();
                List<WishList> body = wishListService.readWishList(user_id);
                List<ProductDto> products = new ArrayList<ProductDto>();
                for (WishList wishList : body) {
                        products.add(ProductService.getDtoFromProduct(wishList.getProduct()));
                }

                return new ResponseEntity<>(products, HttpStatus.OK);
        }

        @ApiOperation(value = "add item in wishlist(only for user), ROLE = USER")
        @PostMapping("/add")
        public ResponseEntity<ApiResponse> addWishList(
            @ApiParam(value = "product object which needs to be added to wishlist") @RequestBody Product product,
            @ApiParam @RequestParam("token") String token) {
                authenticationService.authenticate(token, Collections.singletonList(Role.user));
                User user = authenticationService.getUser(token);
                WishList wishList = new WishList(user, product);
                wishListService.createWishlist(wishList);
                return new ResponseEntity<>(new ApiResponse(true, "Add to wishlist"), HttpStatus.CREATED);

        }


}
