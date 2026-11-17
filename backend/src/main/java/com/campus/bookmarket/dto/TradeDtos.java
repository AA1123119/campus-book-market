package com.campus.bookmarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class TradeDtos {

    @Data
    public static class OrderRequest {
        @NotNull(message = "书籍 id 不能为空")
        private Long bookId;
        @NotBlank(message = "交易方式不能为空")
        private String tradeType = "FACE_TO_FACE";
        private String tradePlace;
    }

    @Data
    public static class ReviewRequest {
        @NotNull(message = "订单 id 不能为空")
        private Long orderId;
        @NotNull(message = "评分不能为空")
        private Integer rating;
        private String content;
    }

    @Data
    public static class MessageRequest {
        @NotNull(message = "接收人不能为空")
        private Long toUserId;
        private Long bookId;
        @NotBlank(message = "内容不能为空")
        private String content;
    }

    @Data
    public static class ReportRequest {
        @NotBlank(message = "举报对象类型不能为空")
        private String targetType;
        @NotNull(message = "举报对象不能为空")
        private Long targetId;
        @NotBlank(message = "举报原因不能为空")
        private String reason;
    }

    @Data
    public static class HandleRequest {
        @NotBlank(message = "处理结果不能为空")
        private String status;
        private String remark;
    }
}
