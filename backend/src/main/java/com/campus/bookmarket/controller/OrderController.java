package com.campus.bookmarket.controller;

import com.campus.bookmarket.auth.CurrentUser;
import com.campus.bookmarket.common.ApiResponse;
import com.campus.bookmarket.common.AuthSupport;
import com.campus.bookmarket.dto.TradeDtos;
import com.campus.bookmarket.entity.BookOrder;
import com.campus.bookmarket.entity.User;
import com.campus.bookmarket.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<BookOrder> create(@Valid @RequestBody TradeDtos.OrderRequest req,
                                         @CurrentUser User user) {
        return ApiResponse.ok(orderService.create(req, AuthSupport.require(user)));
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<BookOrder> confirm(@PathVariable Long id, @CurrentUser User user) {
        return ApiResponse.ok(orderService.confirm(id, AuthSupport.require(user)));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<BookOrder> complete(@PathVariable Long id, @CurrentUser User user) {
        return ApiResponse.ok(orderService.complete(id, AuthSupport.require(user)));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<BookOrder> cancel(@PathVariable Long id, @CurrentUser User user) {
        return ApiResponse.ok(orderService.cancel(id, AuthSupport.require(user)));
    }

    @GetMapping("/bought")
    public ApiResponse<List<BookOrder>> bought(@CurrentUser User user) {
        return ApiResponse.ok(orderService.bought(AuthSupport.require(user)));
    }

    @GetMapping("/sold")
    public ApiResponse<List<BookOrder>> sold(@CurrentUser User user) {
        return ApiResponse.ok(orderService.sold(AuthSupport.require(user)));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookOrder> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.get(id));
    }
}
