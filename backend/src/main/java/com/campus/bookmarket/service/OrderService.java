package com.campus.bookmarket.service;

import com.campus.bookmarket.common.BusinessException;
import com.campus.bookmarket.dto.TradeDtos;
import com.campus.bookmarket.entity.Book;
import com.campus.bookmarket.entity.BookOrder;
import com.campus.bookmarket.entity.User;
import com.campus.bookmarket.repository.BookRepository;
import com.campus.bookmarket.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;

    /** 下单：书籍进入 LOCKED，避免一物多卖（并发场景由乐观锁/唯一索引兜底） */
    @Transactional
    public BookOrder create(TradeDtos.OrderRequest req, User buyer) {
        Book book = bookRepository.findById(req.getBookId())
                .orElseThrow(() -> new BusinessException(404, "书籍不存在"));
        if (!"ON_SALE".equals(book.getStatus())) {
            throw new BusinessException("该书已被预订或已售出");
        }
        if (book.getSellerId().equals(buyer.getId())) {
            throw new BusinessException("不能购买自己发布的书籍");
        }
        book.setStatus("LOCKED");
        bookRepository.save(book);

        BookOrder order = new BookOrder();
        order.setOrderNo("BM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(1000, 9999));
        order.setBookId(book.getId());
        order.setBookTitle(book.getTitle());
        order.setBuyerId(buyer.getId());
        order.setBuyerName(buyer.getNickname());
        order.setSellerId(book.getSellerId());
        order.setSellerName(book.getSellerName());
        order.setAmount(book.getPrice());
        order.setTradeType(req.getTradeType());
        order.setTradePlace(req.getTradePlace());
        order.setStatus("PENDING");
        return orderRepository.save(order);
    }

    /** 卖家确认：约定面交时间地点 */
    @Transactional
    public BookOrder confirm(Long id, User user) {
        BookOrder order = get(id);
        if (!order.getSellerId().equals(user.getId())) {
            throw new BusinessException(403, "只有卖家可以确认订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可确认");
        }
        order.setStatus("CONFIRMED");
        return order;
    }

    /** 买家确认收货：书籍置为 SOLD，订单完成 */
    @Transactional
    public BookOrder complete(Long id, User user) {
        BookOrder order = get(id);
        if (!order.getBuyerId().equals(user.getId())) {
            throw new BusinessException(403, "只有买家可以确认收货");
        }
        if (!"CONFIRMED".equals(order.getStatus())) {
            throw new BusinessException("卖家尚未确认订单");
        }
        order.setStatus("COMPLETED");
        order.setFinishedAt(LocalDateTime.now());
        bookRepository.findById(order.getBookId()).ifPresent(b -> b.setStatus("SOLD"));
        return order;
    }

    /** 取消：书籍回到在售状态 */
    @Transactional
    public BookOrder cancel(Long id, User user) {
        BookOrder order = get(id);
        if (!order.getBuyerId().equals(user.getId()) && !order.getSellerId().equals(user.getId())) {
            throw new BusinessException(403, "无权取消该订单");
        }
        if ("COMPLETED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new BusinessException("订单已结束，不能取消");
        }
        order.setStatus("CANCELLED");
        bookRepository.findById(order.getBookId()).ifPresent(b -> {
            if (!"SOLD".equals(b.getStatus())) {
                b.setStatus("ON_SALE");
            }
        });
        return order;
    }

    public List<BookOrder> bought(User user) {
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(user.getId());
    }

    public List<BookOrder> sold(User user) {
        return orderRepository.findBySellerIdOrderByCreatedAtDesc(user.getId());
    }

    public BookOrder get(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new BusinessException(404, "订单不存在"));
    }
}
