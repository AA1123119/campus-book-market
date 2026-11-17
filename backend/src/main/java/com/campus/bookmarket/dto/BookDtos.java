package com.campus.bookmarket.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

public class BookDtos {

    @Data
    public static class BookRequest {
        @NotBlank(message = "书名不能为空")
        private String title;
        private String author;
        private String isbn;
        private String publisher;
        private String category;
        @Min(value = 1, message = "成色取 1-5")
        @Max(value = 5, message = "成色取 1-5")
        private Integer conditionLevel = 4;
        private BigDecimal originalPrice;
        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.01", message = "价格必须大于 0")
        private BigDecimal price;
        private String description;
        private String coverUrl;
    }

    @Data
    public static class PriceSuggestionRequest {
        private String isbn;
        private String category;
        private BigDecimal originalPrice;
        private Integer conditionLevel = 4;
        private String title;
    }

    @Data
    public static class PriceSuggestionResponse {
        private BigDecimal suggestedPrice;
        private BigDecimal lowBound;
        private BigDecimal highBound;
        /** 0-1，模型置信度 */
        private Double confidence;
        /** 定价依据说明，用于前端展示“AI 定价理由” */
        private String reason;
        private String model;
    }

    @Data
    public static class IsbnResult {
        private String isbn;
        private String title;
        private String author;
        private String publisher;
        private BigDecimal originalPrice;
        private String category;
        private String coverUrl;
    }
}
