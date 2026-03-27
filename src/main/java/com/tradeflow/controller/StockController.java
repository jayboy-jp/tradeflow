package com.tradeflow.controller;

import com.tradeflow.dto.StockResponse;
import com.tradeflow.dto.CreateStockRequest;
import com.tradeflow.dto.UpdateStockPriceRequest;
import com.tradeflow.entity.Stock;
import com.tradeflow.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stocks")
@Tag(name = "Stocks", description = "Stock catalog and market data")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    @Operation(summary = "List all stocks")
    public List<StockResponse> getAllStocks() {
        return stockService.getAllStocks().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get stock by ID")
    public StockResponse getById(@PathVariable Long id) {
        return toResponse(stockService.getById(id));
    }

    @GetMapping("/symbol/{symbol}")
    @Operation(summary = "Get stock by symbol")
    public StockResponse getBySymbol(@PathVariable String symbol) {
        return toResponse(stockService.getBySymbol(symbol));
    }

    @PostMapping
    @Operation(summary = "Create a new stock (admin)")
    public ResponseEntity<StockResponse> createStock(@Valid @RequestBody CreateStockRequest request) {
        Stock stock = stockService.createStock(request.symbol(), request.name(), request.price());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(stock));
    }

    @PatchMapping("/{id}/price")
    @Operation(summary = "Update stock market price and auto-match eligible pending orders")
    public StockResponse updatePrice(@PathVariable Long id, @Valid @RequestBody UpdateStockPriceRequest request) {
        Stock updated = stockService.updateCurrentPrice(id, request.price());
        return toResponse(updated);
    }

    private StockResponse toResponse(Stock s) {
        return new StockResponse(s.getId(), s.getSymbol(), s.getName(), s.getCurrentPrice());
    }
}
