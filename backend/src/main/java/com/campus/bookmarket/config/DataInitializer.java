package com.campus.bookmarket.config;

import com.campus.bookmarket.dto.AuthDtos;
import com.campus.bookmarket.dto.BookDtos;
import com.campus.bookmarket.dto.TradeDtos;
import com.campus.bookmarket.entity.Book;
import com.campus.bookmarket.entity.BookOrder;
import com.campus.bookmarket.entity.User;
import com.campus.bookmarket.repository.BookRepository;
import com.campus.bookmarket.repository.UserRepository;
import com.campus.bookmarket.service.AuthService;
import com.campus.bookmarket.service.BookService;
import com.campus.bookmarket.service.OrderService;
import com.campus.bookmarket.service.SocialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 演示数据初始化：仅在 t_user 为空时执行，便于评审者一键启动后直接体验。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final AuthService authService;
    private final BookService bookService;
    private final OrderService orderService;
    private final SocialService socialService;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        log.info("初始化演示数据...");

        User admin = createUser("admin", "系统管理员", "主校区", "ADMIN");
        User alice = createUser("alice", "林小满", "主校区", "USER");
        User bob = createUser("bob", "陈子航", "东校区", "USER");
        User carol = createUser("carol", "苏雨桐", "西校区", "USER");

        Book b1 = publish(alice, "数据结构（C语言版）", "严蔚敏", "9787040498258", "高等教育出版社",
                "数据结构", 4, "45.00", "28.00", "无划线，笔记齐全", 320);
        Book b2 = publish(alice, "计算机网络（第7版）", "谢希仁", "9787111544937", "机械工业出版社",
                "计算机网络", 5, "49.00", "35.00", "几乎全新，仅翻阅过", 210);
        Book b3 = publish(bob, "高等数学（上册）第七版", "同济大学数学系", "9787040396638", "高等教育出版社",
                "高等数学", 3, "42.80", "18.00", "有少量笔记，无缺页", 156);
        Book b4 = publish(bob, "操作系统概念（第9版）", "Silberschatz", "9787111630889", "机械工业出版社",
                "操作系统", 4, "89.00", "45.00", "英文影印版，笔记在便签上", 98);
        Book b5 = publish(carol, "线性代数（第六版）", "同济大学数学系", "9787040396645", "高等教育出版社",
                "线性代数", 2, "28.60", "9.90", "书角有磨损，内页完好", 87);
        Book b6 = publish(carol, "新视野大学英语读写教程1", "郑树棠", "9787513591764", "外语教学与研究出版社",
                "大学英语", 4, "45.90", "16.00", "含听力光盘", 143);
        publish(alice, "2026考研数学复习全书", "李永乐", null, "中国政法大学出版社",
                "考研资料", 4, "79.00", "32.00", "含错题本，重点已标注", 264);
        publish(bob, "概率论与数理统计（第四版）", "盛骤", "9787040238969", "高等教育出版社",
                "概率论", 5, "36.50", "22.00", "全新未拆封，多买一本", 76);
        publish(carol, "大学物理（上）", "马文蔚", "9787040463849", "高等教育出版社",
                "大学物理", 3, "43.00", "15.00", "附实验报告模板", 61);
        publish(alice, "算法导论（第三版）", "Cormen", "9787111407010", "机械工业出版社",
                "数据结构", 4, "128.00", "58.00", "硬壳精装，收藏级成色", 402);
        publish(bob, "Java 核心技术 卷I", "Cay Horstmann", "9787111636669", "机械工业出版社",
                "其他", 4, "149.00", "62.00", "课程项目用书，无笔记", 188);
        publish(carol, "大学英语四级真题超详解", "新东方", null, "浙江教育出版社",
                "大学英语", 3, "39.80", "12.00", "含 12 套真题，已做 3 套", 95);

        // 一笔完整交易：bob 买 alice 的书 → 卖家确认 → 买家收货 → 买家评价
        BookOrder order = orderService.create(orderReq(b1.getId(), "FACE_TO_FACE", "图书馆一楼大厅"), bob);
        orderService.confirm(order.getId(), alice);
        orderService.complete(order.getId(), bob);

        TradeDtos.ReviewRequest rv = new TradeDtos.ReviewRequest();
        rv.setOrderId(order.getId());
        rv.setRating(5);
        rv.setContent("书和描述一致，学长还送了复习提纲，面交很准时。");
        socialService.review(rv, bob);

        // 两笔待处理举报，供管理员审核页演示
        TradeDtos.ReportRequest r1 = new TradeDtos.ReportRequest();
        r1.setTargetType("BOOK");
        r1.setTargetId(b5.getId());
        r1.setReason("图片与实物不符，疑似盗版复印本");
        socialService.report(r1, carol);

        TradeDtos.ReportRequest r2 = new TradeDtos.ReportRequest();
        r2.setTargetType("USER");
        r2.setTargetId(bob.getId());
        r2.setReason("多次拍下不付款，沟通态度恶劣");
        socialService.report(r2, alice);

        log.info("演示数据初始化完成，账号：admin/alice/bob/carol，密码均为 123456");
    }

    private User createUser(String username, String nickname, String campus, String role) {
        AuthDtos.RegisterRequest req = new AuthDtos.RegisterRequest();
        req.setUsername(username);
        req.setPassword("123456");
        req.setNickname(nickname);
        req.setCampus(campus);
        req.setPhone("13800000000");
        authService.register(req);
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setRole(role);
        return userRepository.save(user);
    }

    private Book publish(User seller, String title, String author, String isbn, String publisher,
                         String category, int condition, String originalPrice, String price,
                         String desc, int views) {
        BookDtos.BookRequest req = new BookDtos.BookRequest();
        req.setTitle(title);
        req.setAuthor(author);
        req.setIsbn(isbn);
        req.setPublisher(publisher);
        req.setCategory(category);
        req.setConditionLevel(condition);
        req.setOriginalPrice(new BigDecimal(originalPrice));
        req.setPrice(new BigDecimal(price));
        req.setDescription(desc + "。支持校内面交，可小刀。");
        req.setCoverUrl("/api/covers/" + Math.abs(title.hashCode()) + ".svg?t="
                + java.net.URLEncoder.encode(title, java.nio.charset.StandardCharsets.UTF_8));
        Book book = bookService.publish(req, seller);
        book.setViewCount(views);
        return bookRepository.save(book);
    }

    private TradeDtos.OrderRequest orderReq(Long bookId, String tradeType, String place) {
        TradeDtos.OrderRequest req = new TradeDtos.OrderRequest();
        req.setBookId(bookId);
        req.setTradeType(tradeType);
        req.setTradePlace(place);
        return req;
    }
}
