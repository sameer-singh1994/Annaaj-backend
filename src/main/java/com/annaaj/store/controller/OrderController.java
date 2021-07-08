package com.annaaj.store.controller;

import com.annaaj.store.enums.OrderStatus;
import com.annaaj.store.enums.Role;
import com.annaaj.store.exceptions.AuthenticationFailException;
import com.annaaj.store.exceptions.OrderNotFoundException;
import com.annaaj.store.exceptions.ProductNotExistException;
import com.annaaj.store.model.Order;
import com.annaaj.store.model.User;
import com.annaaj.store.repository.UserRepository;
import com.annaaj.store.service.AuthenticationService;
import com.annaaj.store.service.OrderService;
import com.annaaj.store.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.annaaj.store.common.ApiResponse;
import com.annaaj.store.dto.checkout.CheckoutItemDto;
import com.annaaj.store.dto.checkout.StripeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> placeOrder(@RequestParam("token") String token, @RequestParam("sessionId") String sessionId)
        throws ProductNotExistException, AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        User user = authenticationService.getUser(token);
        orderService.placeOrder(user, sessionId);
        return new ResponseEntity<>(new ApiResponse(true, "Order has been placed"), HttpStatus.CREATED);
    }

    @GetMapping("/")
    public ResponseEntity<List<Order>> getAllOrders(@RequestParam("token") String token) throws AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        User user = authenticationService.getUser(token);
        List<Order> orderDtoList = orderService.listOrders(user);
        return new ResponseEntity<>(orderDtoList, HttpStatus.OK);
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<StripeResponse> checkoutList(@RequestBody List<CheckoutItemDto> checkoutItemDtoList) throws StripeException {
        Session session = orderService.createSession(checkoutItemDtoList);
        StripeResponse stripeResponse = new StripeResponse(session.getId());
        return new ResponseEntity<>(stripeResponse, HttpStatus.OK);
    }

    @PostMapping("/delivered-community-leader/{id}")
    public ResponseEntity<ApiResponse> orderDeliveredToCommunityLeader(@PathVariable("id") Integer id,
                                                                       @RequestParam("token") String token) throws AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.communityLeader));
        orderService.updateOrderStatus(id, OrderStatus.DELIVERED_TO_COMMUNITY_LEADER);
        return new ResponseEntity<>(
            new ApiResponse(true, "Order has been delivered to community leader"), HttpStatus.OK);
    }

    @PostMapping("/completed/{id}")
    public ResponseEntity<ApiResponse> orderCompleted(@PathVariable("id") Integer id,
                                                                       @RequestParam("token") String token) throws AuthenticationFailException {
        authenticationService.authenticate(token, Arrays.asList(Role.user, Role.communityLeader));
        Order order = orderService.getOrder(id);
        if (order.getOrderStatus().equals(OrderStatus.COMPLETED)) {
            return new ResponseEntity<>(
                new ApiResponse(true, "Order has been received by the user already"), HttpStatus.OK);
        }
        orderService.updateOrderStatus(id, OrderStatus.COMPLETED);
        User communityLeader = authenticationService.getUser(token);
        userService.updateCommunityLeaderIncentive(communityLeader, order);
        return new ResponseEntity<>(
            new ApiResponse(true, "Order has been received by the user"), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOrder(@PathVariable("id") Integer id, @RequestParam("token") String token) throws AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        User user = authenticationService.getUser(token);
        try {
            Order order = orderService.getOrder(id);
            if (!user.getId().equals(order.getUser().getId())) {
            return new ResponseEntity<>("The order does not belong to the user", HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(order,HttpStatus.OK);
        }
        catch (OrderNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }

    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse> cancelOrder(@PathVariable("id") Integer id, @RequestParam("token") String token) {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        User user = authenticationService.getUser(token);
        Order order = orderService.getOrder(id);
        if (!order.getUser().getId().equals(user.getId())) {
            return new ResponseEntity<>(new ApiResponse(false, "Order does not belong to user"), HttpStatus.FORBIDDEN);
        }
        orderService.cancelOrder(id);
        return new ResponseEntity<>(new ApiResponse(true, "Order has been cancelled"), HttpStatus.OK);
    }

    @PostMapping("/add/user/{userId}")
    public ResponseEntity<ApiResponse> placeOrder(@RequestParam("token") String token,
                                                  @RequestParam("sessionId") String sessionId, @PathVariable("userId") Integer userId)
        throws ProductNotExistException, AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.communityLeader));
        authenticationService.authenticateCommunityLeader(token, userId);
        User user = userRepository.findById(userId).get();
        orderService.placeOrder(user, sessionId);
        return new ResponseEntity<>(new ApiResponse(true, "Order has been placed"), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getAllOrders(@RequestParam("token") String token, @PathVariable("userId") Integer userId) throws AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.communityLeader));
        authenticationService.authenticateCommunityLeader(token, userId);
        User user = userRepository.findById(userId).get();
        List<Order> orderDtoList = orderService.listOrders(user);
        return new ResponseEntity<>(orderDtoList, HttpStatus.OK);
    }

    @GetMapping("/{id}/user/{userId}")
    public ResponseEntity<Object> getOrder(@PathVariable("id") Integer id,
                                           @RequestParam("token") String token, @PathVariable("userId") Integer userId) throws AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.communityLeader));
        authenticationService.authenticateCommunityLeader(token, userId);
        User user = userRepository.findById(userId).get();
        try {
            Order order = orderService.getOrder(id);
            if (!user.getId().equals(order.getUser().getId())) {
                return new ResponseEntity<>("The order does not belong to the user", HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(order,HttpStatus.OK);
        }
        catch (OrderNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
        }

    }
}
