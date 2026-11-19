package com.campus.bookmarket.repository;

import com.campus.bookmarket.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByStatusOrderByCreatedAtDesc(String status);

    List<Report> findAllByOrderByCreatedAtDesc();
}
