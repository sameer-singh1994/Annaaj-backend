package com.annaaj.store.controller;

import com.annaaj.store.dto.product.ProductResponseDtoCommunityLeader;
import com.annaaj.store.dto.product.ProductResponseDtoUser;
import com.annaaj.store.service.CategoryService;
import com.annaaj.store.service.ProductService;
import com.annaaj.store.common.ApiResponse;
import com.annaaj.store.dto.product.ProductDto;
import com.annaaj.store.model.Category;
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

    @GetMapping("/")
    public ResponseEntity<List<ProductDto>> getProducts() {
        List<ProductDto> body = productService.listProductsAdmin();
        return new ResponseEntity<List<ProductDto>>(body, HttpStatus.OK);
    }

    @GetMapping("/users")
    public ResponseEntity<List<ProductResponseDtoUser>> getProductsUser() {
        List<ProductResponseDtoUser> body = productService.listProductsUser();
        return new ResponseEntity<List<ProductResponseDtoUser>>(body, HttpStatus.OK);
    }

    @GetMapping("/community-leaders")
    public ResponseEntity<List<ProductResponseDtoCommunityLeader>> getProductsCommunityLeader() {
        List<ProductResponseDtoCommunityLeader> body = productService.listProductsCommunityLeader();
        return new ResponseEntity<List<ProductResponseDtoCommunityLeader>>(body, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addProduct(@RequestBody ProductDto productDto) {
        Optional<Category> optionalCategory = categoryService.readCategory(productDto.getCategoryId());
        if (!optionalCategory.isPresent()) {
            return new ResponseEntity<ApiResponse>(new ApiResponse(false, "category is invalid"), HttpStatus.CONFLICT);
        }
        Category category = optionalCategory.get();
        productService.addProduct(productDto, category);
        return new ResponseEntity<ApiResponse>(new ApiResponse(true, "Product has been added"), HttpStatus.CREATED);
    }

    @PostMapping("/update/{productID}")
    public ResponseEntity<ApiResponse> updateProduct(@PathVariable("productID") Integer productID, @RequestBody @Valid ProductDto productDto) {
        Optional<Category> optionalCategory = categoryService.readCategory(productDto.getCategoryId());
        if (!optionalCategory.isPresent()) {
            return new ResponseEntity<ApiResponse>(new ApiResponse(false, "category is invalid"), HttpStatus.CONFLICT);
        }
        Category category = optionalCategory.get();
        productService.updateProduct(productID, productDto, category);
        return new ResponseEntity<ApiResponse>(new ApiResponse(true, "Product has been updated"), HttpStatus.OK);
    }
}
