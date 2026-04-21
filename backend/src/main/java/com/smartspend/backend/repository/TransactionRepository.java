package com.smartspend.backend.repository;

import com.smartspend.backend.entity.Transaction;
import com.smartspend.backend.entity.TransactionType;
import com.smartspend.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByDateDesc(User user);

    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate start, LocalDate end);

    List<Transaction> findByUserAndCategoryIgnoreCaseOrderByDateDesc(User user, String category);

    List<Transaction> findByUserAndTypeOrderByDateDesc(User user, TransactionType type);
}
