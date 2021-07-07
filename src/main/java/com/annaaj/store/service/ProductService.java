package com.annaaj.store.service;

import com.annaaj.store.config.application.CommunityLeaderConfig;
import com.annaaj.store.dto.product.ProductResponseDtoCommunityLeader;
import com.annaaj.store.dto.product.ProductResponseDtoUser;
import com.annaaj.store.exceptions.ProductNotExistException;
import com.annaaj.store.repository.ProductRepository;
import com.annaaj.store.dto.product.ProductDto;
import com.annaaj.store.model.Category;
import com.annaaj.store.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CommunityLeaderConfig communityLeaderConfig;

    public List<ProductDto> listProductsAdmin() {
        List<Product> products = productRepository.findAll();
        List<ProductDto> productDtos = new ArrayList<>();
        for(Product product : products) {
            ProductDto productDto = getDtoFromProduct(product);
            productDtos.add(productDto);
        }
        return productDtos;
    }

    public List<ProductResponseDtoUser> listProductsUser() {
        List<Product> products = productRepository.findAll();
        List<ProductResponseDtoUser> productDtos = new ArrayList<>();
        for(Product product : products) {
            ProductResponseDtoUser productDto = getUserDtoFromProduct(product);
            productDtos.add(productDto);
        }
        return productDtos;
    }

    public List<ProductResponseDtoCommunityLeader> listProductsCommunityLeader() {
        List<Product> products = productRepository.findAll();
        List<ProductResponseDtoCommunityLeader> productDtos = new ArrayList<>();
        for(Product product : products) {
            ProductResponseDtoCommunityLeader productDto = getCommunityLeaderDtoFromProduct(product);
            productDtos.add(productDto);
        }
        return productDtos;
    }

    public ProductResponseDtoUser getUserDtoFromProduct(Product product) {
        return new ProductResponseDtoUser(product);
    }

    public ProductResponseDtoCommunityLeader getCommunityLeaderDtoFromProduct(Product product) {
        ProductResponseDtoCommunityLeader productResponseDtoCommunityLeader =
            new ProductResponseDtoCommunityLeader(product);
        productResponseDtoCommunityLeader.setProjectedIncentive(getIncentive(product));
        return productResponseDtoCommunityLeader;
    }

    public static ProductDto getDtoFromProduct(Product product) {
        ProductDto productDto = new ProductDto(product);
        return productDto;
    }

    public static Product getProductFromDto(ProductDto productDto, Category category) {
        Product product = new Product(productDto, category);
        return product;
    }

    public void addProduct(ProductDto productDto, Category category) {
        Product product = getProductFromDto(productDto, category);
        productRepository.save(product);
    }

    public void updateProduct(Integer productID, ProductDto productDto, Category category) {
        Product product = getProductFromDto(productDto, category);
        product.setId(productID);
        productRepository.save(product);
    }


    private double getIncentive(Product product) {
        return (product.getPrice() - product.getCostPrice()) * (communityLeaderConfig
            .getIncentivePercentage() / 100.0);
    }

    public Product getProductById(Integer productId) throws ProductNotExistException {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (!optionalProduct.isPresent())
            throw new ProductNotExistException("Product id is invalid " + productId);
        return optionalProduct.get();
    }


}
