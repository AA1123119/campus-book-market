package com.campus.bookmarket.service;

import com.campus.bookmarket.common.BusinessException;
import com.campus.bookmarket.dto.BookDtos;
import com.campus.bookmarket.entity.Book;
import com.campus.bookmarket.entity.BookOrder;
import com.campus.bookmarket.entity.User;
import com.campus.bookmarket.repository.BookRepository;
import com.campus.bookmarket.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;

    public List<Book> search(String keyword, String category, BigDecimal minPrice,
                             BigDecimal maxPrice, String campus, String sort) {
        String kw = blankToNull(keyword);
        List<Book> list = bookRepository.search(kw, blankToNull(category), minPrice, maxPrice, blankToNull(campus));
        if ("priceAsc".equals(sort)) {
            list.sort((a, b) -> a.getPrice().compareTo(b.getPrice()));
        } else if ("priceDesc".equals(sort)) {
            list.sort((a, b) -> b.getPrice().compareTo(a.getPrice()));
        } else if ("hot".equals(sort)) {
            list.sort((a, b) -> Integer.compare(b.getViewCount(), a.getViewCount()));
        }
        return list;
    }

    @Transactional
    public Book detail(Long id) {
        Book book = getById(id);
        book.setViewCount(book.getViewCount() + 1);
        return book;
    }

    public Book getById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new BusinessException(404, "书籍不存在"));
    }

    public Book publish(BookDtos.BookRequest req, User user) {
        Book book = new Book();
        applyRequest(book, req);
        book.setSellerId(user.getId());
        book.setSellerName(user.getNickname());
        book.setCampus(user.getCampus());
        book.setStatus("ON_SALE");
        book.setViewCount(0);
        return bookRepository.save(book);
    }

    @Transactional
    public Book update(Long id, BookDtos.BookRequest req, User user) {
        Book book = getById(id);
        if (!book.getSellerId().equals(user.getId())) {
            throw new BusinessException(403, "只能修改自己发布的书籍");
        }
        if (!"ON_SALE".equals(book.getStatus())) {
            throw new BusinessException("当前状态不可编辑");
        }
        applyRequest(book, req);
        return book;
    }

    @Transactional
    public void offShelf(Long id, User user) {
        Book book = getById(id);
        if (!book.getSellerId().equals(user.getId())) {
            throw new BusinessException(403, "只能下架自己发布的书籍");
        }
        if ("LOCKED".equals(book.getStatus())) {
            throw new BusinessException("该书籍有进行中的订单，暂不能下架");
        }
        book.setStatus("OFF_SHELF");
    }

    public List<Book> myBooks(User user) {
        return bookRepository.findBySellerIdOrderByCreatedAtDesc(user.getId());
    }

    /**
     * 个性化推荐：基于用户历史购买/发布书籍的偏好分类，加权浏览量排序；
     * 无历史记录时退化为全站热销榜（冷启动策略）。
     */
    public List<Map<String, Object>> recommend(User user, int limit) {
        List<Book> candidates = new ArrayList<>();
        boolean coldStart = true;
        if (user != null) {
            List<BookOrder> history = orderRepository.findByBuyerIdOrderByCreatedAtDesc(user.getId());
            List<String> categories = new ArrayList<>();
            for (BookOrder order : history) {
                bookRepository.findById(order.getBookId()).ifPresent(b -> {
                    if (b.getCategory() != null && !categories.contains(b.getCategory())) {
                        categories.add(b.getCategory());
                    }
                });
            }
            for (String c : categories) {
                candidates.addAll(bookRepository.findTop10ByCategoryAndStatusOrderByViewCountDesc(c, "ON_SALE"));
                coldStart = false;
            }
        }
        if (candidates.isEmpty()) {
            candidates.addAll(bookRepository.findTop10ByStatusOrderByViewCountDesc("ON_SALE"));
            coldStart = true;
        }
        List<Map<String, Object>> res = new ArrayList<>();
        for (Book b : candidates) {
            if (user != null && b.getSellerId().equals(user.getId())) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("book", b);
            m.put("reason", coldStart ? "全站热销，大家都在卖/买" : "根据你最近关注的《" + b.getCategory() + "》类书籍推荐");
            m.put("score", Math.min(99, 60 + b.getViewCount() / 3));
            res.add(m);
            if (res.size() >= limit) {
                break;
            }
        }
        return res;
    }

    public Map<String, Object> categories() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("categories", List.of("高等数学", "数据结构", "计算机网络", "操作系统", "线性代数",
                "大学英语", "大学物理", "概率论", "考研资料", "其他"));
        res.put("campuses", List.of("主校区", "东校区", "西校区", "南校区"));
        return res;
    }

    private void applyRequest(Book book, BookDtos.BookRequest req) {
        book.setTitle(req.getTitle());
        book.setAuthor(req.getAuthor());
        book.setIsbn(req.getIsbn());
        book.setPublisher(req.getPublisher());
        book.setCategory(req.getCategory());
        book.setConditionLevel(req.getConditionLevel() == null ? 4 : req.getConditionLevel());
        book.setOriginalPrice(req.getOriginalPrice());
        book.setPrice(req.getPrice());
        book.setDescription(req.getDescription());
        book.setCoverUrl(req.getCoverUrl());
        if (book.getCoverUrl() == null || book.getCoverUrl().isBlank()) {
            try {
                book.setCoverUrl("/api/covers/" + Math.abs(book.getTitle().hashCode()) + ".svg?t="
                        + java.net.URLEncoder.encode(book.getTitle(), java.nio.charset.StandardCharsets.UTF_8));
            } catch (Exception ignored) {
                book.setCoverUrl("/api/covers/1.svg");
            }
        }
    }

    private String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
