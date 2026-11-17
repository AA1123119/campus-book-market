package com.campus.bookmarket.controller;

import com.campus.bookmarket.common.ApiResponse;
import com.campus.bookmarket.dto.BookDtos;
import com.campus.bookmarket.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /** 智能定价：返回建议价、区间、置信度与可解释的理由 */
    @PostMapping("/price-suggestion")
    public ApiResponse<BookDtos.PriceSuggestionResponse> priceSuggestion(
            @RequestBody BookDtos.PriceSuggestionRequest req) {
        return ApiResponse.ok(aiService.suggestPrice(req));
    }

    /** ISBN 扫描识别（模拟扫码枪/OCR 调用） */
    @GetMapping("/isbn/{isbn}")
    public ApiResponse<BookDtos.IsbnResult> isbn(@PathVariable String isbn) {
        return ApiResponse.ok(aiService.isbnLookup(isbn));
    }

    /** 卖点文案生成 */
    @PostMapping("/description")
    public ApiResponse<Map<String, String>> description(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(aiService.generateDescription(body));
    }
}
