package com.annaaj.store.controller;

import com.annaaj.store.dto.product.ProductResponseDtoCommunityLeader;
import com.annaaj.store.dto.product.ProductResponseDtoUser;
import com.annaaj.store.enums.Role;
import com.annaaj.store.service.AuthenticationService;
import com.annaaj.store.service.CategoryService;
import com.annaaj.store.service.ProductService;
import com.annaaj.store.common.ApiResponse;
import com.annaaj.store.dto.product.ProductDto;
import com.annaaj.store.model.Category;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    ProductService productService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    AuthenticationService authenticationService;

    @ApiOperation(value = "get all products, ROLE = ADMIN")
    @GetMapping("/")
    public ResponseEntity<List<ProductDto>> getProducts(@ApiParam @RequestParam("token") String token) {
        authenticationService.authenticate(token, Collections.singletonList(Role.admin));
        List<ProductDto> body = productService.listProductsAdmin();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @ApiOperation(value = "get all products")
    @GetMapping("/users")
    public ResponseEntity<List<ProductResponseDtoUser>> getProductsUser() {
        List<ProductResponseDtoUser> body = productService.listProductsUser();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @ApiOperation(value = "get all products, for COMMUNITY_LEADER (will contain incentive info)")
    @GetMapping("/community-leaders")
    public ResponseEntity<List<ProductResponseDtoCommunityLeader>> getProductsCommunityLeader() {
        List<ProductResponseDtoCommunityLeader> body = productService.listProductsCommunityLeader();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @ApiOperation(value = "add product, ROLE = ADMIN")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addProduct(
        @ApiParam("id can be left as it is") @RequestBody ProductDto productDto,
        @ApiParam @RequestParam("token") String token) {
        authenticationService.authenticate(token, Collections.singletonList(Role.admin));
        Optional<Category> optionalCategory = categoryService.readCategory(productDto.getCategoryId());
        if (!optionalCategory.isPresent()) {
            return new ResponseEntity<>(new ApiResponse(false, "category is invalid"), HttpStatus.CONFLICT);
        }
        Category category = optionalCategory.get();
        productService.addProduct(productDto, category);
        return new ResponseEntity<>(new ApiResponse(true, "Product has been added"), HttpStatus.CREATED);
    }

    @ApiOperation(value = "update product, ROLE = ADMIN")
    @PostMapping("/update/{productID}")
    public ResponseEntity<ApiResponse> updateProduct(
        @ApiParam(value = "id of the product to be updated") @PathVariable("productID") Integer productID,
        @ApiParam(value = "updated info of product") @RequestBody @Valid ProductDto productDto,
        @ApiParam @RequestParam("token") String token) {
        authenticationService.authenticate(token, Collections.singletonList(Role.admin));
        Optional<Category> optionalCategory = categoryService.readCategory(productDto.getCategoryId());
        if (!optionalCategory.isPresent()) {
            return new ResponseEntity<>(new ApiResponse(false, "category is invalid"), HttpStatus.CONFLICT);
        }
        Category category = optionalCategory.get();
        productService.updateProduct(productID, productDto, category);
        return new ResponseEntity<>(new ApiResponse(true, "Product has been updated"), HttpStatus.OK);
    }
}
