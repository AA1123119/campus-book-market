package com.campus.bookmarket.service;

import com.campus.bookmarket.common.BusinessException;
import com.campus.bookmarket.dto.TradeDtos;
import com.campus.bookmarket.entity.*;
import com.campus.bookmarket.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SocialService {

    private final ReviewRepository reviewRepository;
    private final MessageRepository messageRepository;
    private final ReportRepository reportRepository;
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Transactional
    public Review review(TradeDtos.ReviewRequest req, User from) {
        BookOrder order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
        if (!"COMPLETED".equals(order.getStatus())) {
            throw new BusinessException("订单完成后才能评价");
        }
        if (!order.getBuyerId().equals(from.getId()) && !order.getSellerId().equals(from.getId())) {
            throw new BusinessException(403, "无权评价该订单");
        }
        if (reviewRepository.existsByOrderIdAndFromUserId(order.getId(), from.getId())) {
            throw new BusinessException("你已评价过该订单");
        }
        if (req.getRating() < 1 || req.getRating() > 5) {
            throw new BusinessException("评分需在 1-5 之间");
        }
        Long toUserId = order.getBuyerId().equals(from.getId()) ? order.getSellerId() : order.getBuyerId();

        Review review = new Review();
        review.setOrderId(order.getId());
        review.setFromUserId(from.getId());
        review.setFromUserName(from.getNickname());
        review.setToUserId(toUserId);
        review.setRating(req.getRating());
        review.setContent(req.getContent());
        reviewRepository.save(review);

        // 信用分随评价动态调整：好评 +1（上限 120），差评 -2（下限 0）
        userRepository.findById(toUserId).ifPresent(u -> {
            int delta = req.getRating() >= 4 ? 1 : req.getRating() <= 2 ? -2 : 0;
            u.setCreditScore(Math.max(0, Math.min(120, u.getCreditScore() + delta)));
        });
        return review;
    }

    public Map<String, Object> userProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        List<Review> reviews = reviewRepository.findByToUserIdOrderByCreatedAtDesc(userId);
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(5.0);
        Map<String, Object> res = new LinkedHashMap<>();
        // 该接口不要求登录，不能直接返回 User 实体（会带出 password / phone）
        Map<String, Object> brief = new LinkedHashMap<>();
        brief.put("id", user.getId());
        brief.put("username", user.getUsername());
        brief.put("nickname", user.getNickname());
        brief.put("campus", user.getCampus());
        brief.put("creditScore", user.getCreditScore());
        brief.put("createdAt", user.getCreatedAt());
        res.put("user", brief);
        res.put("reviews", reviews);
        res.put("avgRating", Math.round(avg * 10) / 10.0);
        return res;
    }

    @Transactional
    public Message send(TradeDtos.MessageRequest req, User from) {
        if (req.getToUserId().equals(from.getId())) {
            throw new BusinessException("不能给自己发消息");
        }
        Message msg = new Message();
        msg.setFromUserId(from.getId());
        msg.setToUserId(req.getToUserId());
        msg.setBookId(req.getBookId());
        msg.setContent(req.getContent());
        return messageRepository.save(msg);
    }

    @Transactional
    public List<Message> conversation(Long peerId, User me) {
        List<Message> list = messageRepository
                .findByFromUserIdAndToUserIdOrFromUserIdAndToUserIdOrderByCreatedAtAsc(
                        me.getId(), peerId, peerId, me.getId());
        list.stream()
                .filter(m -> m.getToUserId().equals(me.getId()) && !Boolean.TRUE.equals(m.getReadFlag()))
                .forEach(m -> m.setReadFlag(true));
        return list;
    }

    public List<Message> unread(User me) {
        return messageRepository.findByToUserIdAndReadFlagFalse(me.getId());
    }

    @Transactional
    public Report report(TradeDtos.ReportRequest req, User from) {
        Report report = new Report();
        report.setTargetType(req.getTargetType());
        report.setTargetId(req.getTargetId());
        if ("BOOK".equals(req.getTargetType())) {
            report.setTargetTitle(bookRepository.findById(req.getTargetId())
                    .map(Book::getTitle).orElse("未知书籍"));
        } else {
            report.setTargetTitle(userRepository.findById(req.getTargetId())
                    .map(User::getNickname).orElse("未知用户"));
        }
        report.setReporterId(from.getId());
        report.setReporterName(from.getNickname());
        report.setReason(req.getReason());
        report.setStatus("PENDING");
        return reportRepository.save(report);
    }

    public List<Report> reports(String status, User admin) {
        requireAdmin(admin);
        return status == null || status.isBlank()
                ? reportRepository.findAllByOrderByCreatedAtDesc()
                : reportRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /** 审核处理：通过则下架违规书籍或封禁用户（信用分清零） */
    @Transactional
    public Report handle(Long id, TradeDtos.HandleRequest req, User admin) {
        requireAdmin(admin);
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "举报记录不存在"));
        if (!"PENDING".equals(report.getStatus())) {
            throw new BusinessException("该举报已处理，不能重复操作");
        }
        report.setStatus(req.getStatus());
        report.setHandleRemark(req.getRemark());
        if ("APPROVED".equals(req.getStatus())) {
            if ("BOOK".equals(report.getTargetType())) {
                bookRepository.findById(report.getTargetId()).ifPresent(b -> {
                    b.setStatus("OFF_SHELF");
                    userRepository.findById(b.getSellerId()).ifPresent(u ->
                            u.setCreditScore(Math.max(0, u.getCreditScore() - 5)));
                });
            } else {
                userRepository.findById(report.getTargetId()).ifPresent(u -> u.setCreditScore(0));
            }
        }
        return report;
    }

    public Map<String, Object> dashboard(User admin) {
        requireAdmin(admin);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("onSaleBooks", bookRepository.countByStatus("ON_SALE"));
        res.put("soldBooks", bookRepository.countByStatus("SOLD"));
        res.put("totalOrders", orderRepository.count());
        res.put("completedOrders", orderRepository.countByStatus("COMPLETED"));
        res.put("pendingReports", reportRepository.countByStatus("PENDING"));
        res.put("totalUsers", userRepository.count());
        return res;
    }

    private void requireAdmin(User user) {
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw new BusinessException(403, "需要管理员权限");
        }
    }
}
