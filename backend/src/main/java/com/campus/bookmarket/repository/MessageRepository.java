package com.campus.bookmarket.repository;

import com.campus.bookmarket.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByFromUserIdAndToUserIdOrFromUserIdAndToUserIdOrderByCreatedAtAsc(
            Long a1, Long b1, Long a2, Long b2);

    List<Message> findByToUserIdAndReadFlagFalse(Long toUserId);
}
