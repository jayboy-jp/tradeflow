package com.tradeflow.service;

import com.tradeflow.entity.Stock;
import com.tradeflow.exception.BusinessException;
import com.tradeflow.repository.StockRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @CacheEvict(cacheNames = {"stocks:list", "stocks:byId", "stocks:bySymbol"}, allEntries = true)
    public Stock createStock(String symbol, String name, BigDecimal price) {
        if (stockRepository.findBySymbol(symbol).isPresent()) {
            throw new BusinessException("STOCK_EXISTS", "Stock with symbol " + symbol + " already exists");
        }
        return stockRepository.save(new Stock(symbol, name, price));
    }

    @Cacheable(cacheNames = "stocks:list")
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    @Cacheable(cacheNames = "stocks:byId", key = "#id")
    public Stock getById(Long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new BusinessException("STOCK_NOT_FOUND", "Stock not found"));
    }

    @Cacheable(cacheNames = "stocks:bySymbol", key = "#symbol")
    public Stock getBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new BusinessException("STOCK_NOT_FOUND", "Stock not found: " + symbol));
    }
}