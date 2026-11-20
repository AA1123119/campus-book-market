package com.campus.bookmarket.controller;

import com.campus.bookmarket.auth.CurrentUser;
import com.campus.bookmarket.common.ApiResponse;
import com.campus.bookmarket.common.AuthSupport;
import com.campus.bookmarket.dto.TradeDtos;
import com.campus.bookmarket.entity.Message;
import com.campus.bookmarket.entity.Report;
import com.campus.bookmarket.entity.Review;
import com.campus.bookmarket.entity.User;
import com.campus.bookmarket.service.SocialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SocialController {

    private final SocialService socialService;

    @PostMapping("/reviews")
    public ApiResponse<Review> review(@Valid @RequestBody TradeDtos.ReviewRequest req,
                                      @CurrentUser User user) {
        return ApiResponse.ok(socialService.review(req, AuthSupport.require(user)));
    }

    @GetMapping("/users/{id}/profile")
    public ApiResponse<Map<String, Object>> profile(@PathVariable Long id) {
        return ApiResponse.ok(socialService.userProfile(id));
    }

    @PostMapping("/messages")
    public ApiResponse<Message> send(@Valid @RequestBody TradeDtos.MessageRequest req,
                                     @CurrentUser User user) {
        return ApiResponse.ok(socialService.send(req, AuthSupport.require(user)));
    }

    @GetMapping("/messages/{peerId}")
    public ApiResponse<List<Message>> conversation(@PathVariable Long peerId,
                                                   @CurrentUser User user) {
        return ApiResponse.ok(socialService.conversation(peerId, AuthSupport.require(user)));
    }

    @GetMapping("/messages/unread")
    public ApiResponse<List<Message>> unread(@CurrentUser User user) {
        return ApiResponse.ok(socialService.unread(AuthSupport.require(user)));
    }

    @PostMapping("/reports")
    public ApiResponse<Report> report(@Valid @RequestBody TradeDtos.ReportRequest req,
                                      @CurrentUser User user) {
        return ApiResponse.ok(socialService.report(req, AuthSupport.require(user)));
    }

    @GetMapping("/admin/reports")
    public ApiResponse<List<Report>> reports(@RequestParam(required = false) String status,
                                             @CurrentUser User user) {
        return ApiResponse.ok(socialService.reports(status, AuthSupport.require(user)));
    }

    @PostMapping("/admin/reports/{id}/handle")
    public ApiResponse<Report> handle(@PathVariable Long id,
                                      @Valid @RequestBody TradeDtos.HandleRequest req,
                                      @CurrentUser User user) {
        return ApiResponse.ok(socialService.handle(id, req, AuthSupport.require(user)));
    }

    @GetMapping("/admin/dashboard")
    public ApiResponse<Map<String, Object>> dashboard(@CurrentUser User user) {
        return ApiResponse.ok(socialService.dashboard(AuthSupport.require(user)));
    }
}
