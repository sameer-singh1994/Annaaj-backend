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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

    @ApiOperation(value = "place order, ROLE = USER")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> placeOrder(@ApiParam @RequestParam("token") String token,
                                                  @ApiParam(value = "can be left as it is for now") @RequestParam("sessionId") String sessionId)
        throws ProductNotExistException, AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        User user = authenticationService.getUser(token);
        orderService.placeOrder(user, sessionId);
        return new ResponseEntity<>(new ApiResponse(true, "Order has been placed"), HttpStatus.CREATED);
    }

    @ApiOperation(value = "get all orders, ROLE = USER")
    @GetMapping("/")
    public ResponseEntity<List<Order>> getAllOrders(@ApiParam @RequestParam("token") String token) throws AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        User user = authenticationService.getUser(token);
        List<Order> orderDtoList = orderService.listOrders(user);
        return new ResponseEntity<>(orderDtoList, HttpStatus.OK);
    }

    @ApiOperation(value = "create a checkout session(do not use for now), ROLE = USER, COMMUNITY_LEADER")
    @PostMapping("/create-checkout-session")
    public ResponseEntity<StripeResponse> checkoutList(
        @ApiParam @RequestBody List<CheckoutItemDto> checkoutItemDtoList) throws StripeException {
        Session session = orderService.createSession(checkoutItemDtoList);
        StripeResponse stripeResponse = new StripeResponse(session.getId());
        return new ResponseEntity<>(stripeResponse, HttpStatus.OK);
    }

    @ApiOperation(value = "order delivered to community leader, ROLE = COMMUNITY_LEADER")
    @PostMapping("/delivered-community-leader/{id}")
    public ResponseEntity<ApiResponse> orderDeliveredToCommunityLeader(@ApiParam(value = "order id") @PathVariable("id") Integer id,
                                                                       @ApiParam @RequestParam("token") String token) throws AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.communityLeader));
        orderService.updateOrderStatus(id, OrderStatus.DELIVERED_TO_COMMUNITY_LEADER);
        return new ResponseEntity<>(
            new ApiResponse(true, "Order has been delivered to community leader"), HttpStatus.OK);
    }

    @ApiOperation(value = "order completed(delivered to user), ROLE = USER, COMMUNITY_LEADER")
    @PostMapping("/completed/{id}")
    public ResponseEntity<ApiResponse> orderCompleted(@ApiParam(value = "order id") @PathVariable("id") Integer id,
                                                                       @ApiParam @RequestParam("token") String token) throws AuthenticationFailException {
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

    @ApiOperation(value = "get order from order id, ROLE = USER")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getOrder(@ApiParam(value = "order id") @PathVariable("id") Integer id,
                                           @ApiParam @RequestParam("token") String token) throws AuthenticationFailException {
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

    @ApiOperation(value = "cancel order (only if the order is not yet delivered to community leader), ROLE = USER")
    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse> cancelOrder(@ApiParam(value = "order id") @PathVariable("id") Integer id,
                                                   @ApiParam @RequestParam("token") String token) {
        authenticationService.authenticate(token, Collections.singletonList(Role.user));
        User user = authenticationService.getUser(token);
        Order order = orderService.getOrder(id);
        if (!order.getUser().getId().equals(user.getId())) {
            return new ResponseEntity<>(new ApiResponse(false, "Order does not belong to user"), HttpStatus.FORBIDDEN);
        }
        orderService.cancelOrder(id);
        return new ResponseEntity<>(new ApiResponse(true, "Order has been cancelled"), HttpStatus.OK);
    }

    @ApiOperation(value = "place order for associated user, ROLE = COMMUNITY_LEADER")
    @PostMapping("/add/user/{userId}")
    public ResponseEntity<ApiResponse> placeOrder(@ApiParam @RequestParam("token") String token,
                                                  @ApiParam(value = "can be left as it is for now") @RequestParam("sessionId") String sessionId,
                                                  @ApiParam(value = "id of the user on whose behalf this op is being performed") @PathVariable("userId") Integer userId)
        throws ProductNotExistException, AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.communityLeader));
        authenticationService.authenticateCommunityLeader(token, userId);
        User user = userRepository.findById(userId).get();
        orderService.placeOrder(user, sessionId);
        return new ResponseEntity<>(new ApiResponse(true, "Order has been placed"), HttpStatus.CREATED);
    }

    @ApiOperation(value = "get all orders of associated user, ROLE = COMMUNITY_LEADER")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getAllOrders(@ApiParam @RequestParam("token") String token,
                                                    @ApiParam(value = "id of the user on whose behalf this op is being performed") @PathVariable("userId") Integer userId) throws AuthenticationFailException {
        authenticationService.authenticate(token, Collections.singletonList(Role.communityLeader));
        authenticationService.authenticateCommunityLeader(token, userId);
        User user = userRepository.findById(userId).get();
        List<Order> orderDtoList = orderService.listOrders(user);
        return new ResponseEntity<>(orderDtoList, HttpStatus.OK);
    }

    @ApiOperation(value = "get order by order id of associated user, ROLE = COMMUNITY_LEADER")
    @GetMapping("/{id}/user/{userId}")
    public ResponseEntity<Object> getOrder(@ApiParam(value = "order id") @PathVariable("id") Integer id,
                                           @ApiParam @RequestParam("token") String token,
                                           @ApiParam(value = "id of the user on whose behalf this op is being performed") @PathVariable("userId") Integer userId) throws AuthenticationFailException {
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
