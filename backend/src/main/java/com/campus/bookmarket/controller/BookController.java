package com.campus.bookmarket.controller;

import com.campus.bookmarket.auth.CurrentUser;
import com.campus.bookmarket.common.ApiResponse;
import com.campus.bookmarket.common.AuthSupport;
import com.campus.bookmarket.dto.BookDtos;
import com.campus.bookmarket.entity.Book;
import com.campus.bookmarket.entity.User;
import com.campus.bookmarket.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ApiResponse<List<Book>> search(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String category,
                                          @RequestParam(required = false) BigDecimal minPrice,
                                          @RequestParam(required = false) BigDecimal maxPrice,
                                          @RequestParam(required = false) String campus,
                                          @RequestParam(required = false) String sort) {
        return ApiResponse.ok(bookService.search(keyword, category, minPrice, maxPrice, campus, sort));
    }

    @GetMapping("/meta")
    public ApiResponse<Map<String, Object>> meta() {
        return ApiResponse.ok(bookService.categories());
    }

    @GetMapping("/recommend")
    public ApiResponse<List<Map<String, Object>>> recommend(@CurrentUser User user) {
        return ApiResponse.ok(bookService.recommend(user, 6));
    }

    @GetMapping("/mine")
    public ApiResponse<List<Book>> mine(@CurrentUser User user) {
        return ApiResponse.ok(bookService.myBooks(AuthSupport.require(user)));
    }

    @GetMapping("/{id}")
    public ApiResponse<Book> detail(@PathVariable Long id) {
        return ApiResponse.ok(bookService.detail(id));
    }

    @PostMapping
    public ApiResponse<Book> publish(@Valid @RequestBody BookDtos.BookRequest req,
                                     @CurrentUser User user) {
        return ApiResponse.ok(bookService.publish(req, AuthSupport.require(user)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Book> update(@PathVariable Long id,
                                    @Valid @RequestBody BookDtos.BookRequest req,
                                    @CurrentUser User user) {
        return ApiResponse.ok(bookService.update(id, req, AuthSupport.require(user)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> offShelf(@PathVariable Long id, @CurrentUser User user) {
        bookService.offShelf(id, AuthSupport.require(user));
        return ApiResponse.ok();
    }
}
