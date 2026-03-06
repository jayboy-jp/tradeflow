package com.tradeflow.repository;

import com.tradeflow.entity.Portfolio;
import com.tradeflow.entity.Stock;
import com.tradeflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByUserAndStock(User user, Stock stock);

    List<Portfolio> findByUserOrderByStock_Symbol(User user);
}