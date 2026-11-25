-- ============================================================
-- 书适圈 · 校园二手书智能交易平台  数据库脚本 (MySQL 8.0)
-- 说明：开发环境使用 H2 + JPA 自动建表；生产/答辩演示使用本脚本
-- ============================================================
CREATE DATABASE IF NOT EXISTS bookmarket
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bookmarket;

DROP TABLE IF EXISTS t_report;
DROP TABLE IF EXISTS t_message;
DROP TABLE IF EXISTS t_review;
DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_book;
DROP TABLE IF EXISTS t_user;

-- 用户表
CREATE TABLE t_user (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  username     VARCHAR(32)  NOT NULL COMMENT '登录名（学号）',
  password     VARCHAR(64)  NOT NULL COMMENT 'SHA-256 加盐摘要',
  nickname     VARCHAR(32)  DEFAULT NULL COMMENT '昵称',
  campus       VARCHAR(32)  DEFAULT NULL COMMENT '校区',
  phone        VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
  role         VARCHAR(16)  NOT NULL DEFAULT 'USER' COMMENT 'USER/ADMIN',
  credit_score INT          NOT NULL DEFAULT 100 COMMENT '信用分 0-120',
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- 书籍表
CREATE TABLE t_book (
  id              BIGINT        NOT NULL AUTO_INCREMENT,
  title           VARCHAR(128)  NOT NULL COMMENT '书名',
  author          VARCHAR(64)   DEFAULT NULL,
  isbn            VARCHAR(32)   DEFAULT NULL,
  publisher       VARCHAR(64)   DEFAULT NULL,
  category        VARCHAR(32)   DEFAULT NULL COMMENT '课程/分类',
  condition_level INT           NOT NULL DEFAULT 4 COMMENT '成色 1-5',
  original_price  DECIMAL(10,2) DEFAULT NULL,
  price           DECIMAL(10,2) NOT NULL COMMENT '售价',
  description     VARCHAR(500)  DEFAULT NULL,
  cover_url       VARCHAR(255)  DEFAULT NULL,
  status          VARCHAR(16)   NOT NULL DEFAULT 'ON_SALE' COMMENT 'ON_SALE/LOCKED/SOLD/OFF_SHELF',
  seller_id       BIGINT        NOT NULL,
  seller_name     VARCHAR(32)   DEFAULT NULL,
  campus          VARCHAR(32)   DEFAULT NULL,
  view_count      INT           NOT NULL DEFAULT 0,
  created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_book_isbn (isbn),
  KEY idx_book_category (category),
  KEY idx_book_status (status),
  KEY idx_book_seller (seller_id),
  CONSTRAINT fk_book_seller FOREIGN KEY (seller_id) REFERENCES t_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书籍';

-- 订单表
CREATE TABLE t_order (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  order_no    VARCHAR(32)   NOT NULL COMMENT '业务订单号',
  book_id     BIGINT        NOT NULL,
  book_title  VARCHAR(128)  DEFAULT NULL COMMENT '冗余书名，避免详情页跨表',
  buyer_id    BIGINT        NOT NULL,
  buyer_name  VARCHAR(32)   DEFAULT NULL,
  seller_id   BIGINT        NOT NULL,
  seller_name VARCHAR(32)   DEFAULT NULL,
  amount      DECIMAL(10,2) NOT NULL,
  trade_type  VARCHAR(16)   DEFAULT 'FACE_TO_FACE' COMMENT 'FACE_TO_FACE/SELF_PICKUP',
  trade_place VARCHAR(64)   DEFAULT NULL,
  status      VARCHAR(16)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/CONFIRMED/COMPLETED/CANCELLED',
  created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  finished_at DATETIME      DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_order_buyer (buyer_id),
  KEY idx_order_seller (seller_id),
  CONSTRAINT fk_order_book FOREIGN KEY (book_id) REFERENCES t_book (id),
  CONSTRAINT fk_order_buyer FOREIGN KEY (buyer_id) REFERENCES t_user (id),
  CONSTRAINT fk_order_seller FOREIGN KEY (seller_id) REFERENCES t_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

-- 评价表
CREATE TABLE t_review (
  id             BIGINT       NOT NULL AUTO_INCREMENT,
  order_id       BIGINT       NOT NULL,
  from_user_id   BIGINT       NOT NULL,
  from_user_name VARCHAR(32)  DEFAULT NULL,
  to_user_id     BIGINT       NOT NULL,
  rating         INT          NOT NULL COMMENT '1-5 星',
  content        VARCHAR(300) DEFAULT NULL,
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_review_order_from (order_id, from_user_id),
  KEY idx_review_to_user (to_user_id),
  CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES t_order (id),
  CONSTRAINT fk_review_from FOREIGN KEY (from_user_id) REFERENCES t_user (id),
  CONSTRAINT fk_review_to FOREIGN KEY (to_user_id) REFERENCES t_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易评价';

-- 站内消息表
CREATE TABLE t_message (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  from_user_id BIGINT       NOT NULL,
  to_user_id   BIGINT       NOT NULL,
  book_id      BIGINT       DEFAULT NULL,
  content      VARCHAR(500) NOT NULL,
  read_flag    TINYINT(1)   NOT NULL DEFAULT 0,
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_msg_pair (from_user_id, to_user_id),
  KEY idx_msg_to (to_user_id, read_flag),
  CONSTRAINT fk_msg_from FOREIGN KEY (from_user_id) REFERENCES t_user (id),
  CONSTRAINT fk_msg_to FOREIGN KEY (to_user_id) REFERENCES t_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息';

-- 举报表
CREATE TABLE t_report (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  target_type   VARCHAR(16)  DEFAULT 'BOOK' COMMENT 'BOOK/USER',
  target_id     BIGINT       DEFAULT NULL,
  target_title  VARCHAR(128) DEFAULT NULL,
  reporter_id   BIGINT       NOT NULL,
  reporter_name VARCHAR(32)  DEFAULT NULL,
  reason        VARCHAR(300) DEFAULT NULL,
  status        VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  handle_remark VARCHAR(300) DEFAULT NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_report_status (status),
  CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES t_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报';
