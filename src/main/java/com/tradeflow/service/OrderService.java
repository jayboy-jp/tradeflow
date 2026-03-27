package com.tradeflow.service;

import com.tradeflow.entity.*;
import com.tradeflow.exception.BusinessException;
import com.tradeflow.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final PortfolioRepository portfolioRepository;
    private final WalletRepository walletRepository;
    private final OrderRealtimeStreamService orderRealtimeStreamService;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        StockRepository stockRepository,
                        PortfolioRepository portfolioRepository,
                        WalletRepository walletRepository,
                        OrderRealtimeStreamService orderRealtimeStreamService) {

        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
        this.portfolioRepository = portfolioRepository;
        this.walletRepository = walletRepository;
        this.orderRealtimeStreamService = orderRealtimeStreamService;
    }

    /**
     * STEP 1: PLACE ORDER (Intent + Lock Funds)
     */
    @Transactional
    public Order placeOrder(Long userId,
                            Long stockId,
                            OrderType type,
                            BigDecimal price,
                            Integer quantity) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new BusinessException("STOCK_NOT_FOUND", "Stock not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("WALLET_NOT_FOUND", "Wallet not found"));

        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(quantity));

        if (type == OrderType.BUY) {

            if (wallet.getAvailableBalance().compareTo(totalAmount) < 0) {
                throw new BusinessException("INSUFFICIENT_BALANCE", "Insufficient available balance");
            }

            wallet.setLockedBalance(wallet.getLockedBalance().add(totalAmount));
            walletRepository.save(wallet);

        } else { // SELL

            Portfolio portfolio = portfolioRepository
                    .findByUserAndStock(user, stock)
                    .orElseThrow(() -> new BusinessException("NO_HOLDINGS", "No holdings for this stock"));

            if (portfolio.getQuantity() < quantity) {
                throw new BusinessException("INSUFFICIENT_SHARES", "Insufficient shares to sell");
            }

            // lock shares logically (optional enhancement later)
            // For now we allow SELL without share-locking complexity
        }

        Order order = new Order(user, stock, type, price, quantity, OrderStatus.PENDING);
        Order saved = orderRepository.save(order);
        orderRealtimeStreamService.publishOrderUpdate(userId, "PLACED", saved);
        return saved;
    }

    /**
     * STEP 2: EXECUTE ORDER (Settlement)
     */
    @Transactional
    public Order executeOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("ORDER_NOT_PENDING", "Order not in executable state");
        }

        User user = order.getUser();
        Stock stock = order.getStock();

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("WALLET_NOT_FOUND", "Wallet not found"));

        BigDecimal totalAmount = order.getPrice()
                .multiply(BigDecimal.valueOf(order.getQuantity()));

        if (order.getType() == OrderType.BUY) {
            if (stock.getCurrentPrice().compareTo(order.getPrice()) > 0) {
                throw new BusinessException(
                        "ORDER_PRICE_NOT_REACHED",
                        "BUY order cannot execute until market price is at or below order price"
                );
            }

            wallet.setLockedBalance(wallet.getLockedBalance().subtract(totalAmount));
            wallet.setTotalBalance(wallet.getTotalBalance().subtract(totalAmount));

            Portfolio portfolio = portfolioRepository
                    .findByUserAndStock(user, stock)
                    .orElse(null);

            if (portfolio == null) {
                portfolio = new Portfolio(user, stock, order.getQuantity());
            } else {
                portfolio.setQuantity(portfolio.getQuantity() + order.getQuantity());
            }

            portfolioRepository.save(portfolio);

        } else { // SELL
            if (stock.getCurrentPrice().compareTo(order.getPrice()) < 0) {
                throw new BusinessException(
                        "ORDER_PRICE_NOT_REACHED",
                        "SELL order cannot execute until market price is at or above order price"
                );
            }

            Portfolio portfolio = portfolioRepository
                    .findByUserAndStock(user, stock)
                    .orElseThrow(() -> new BusinessException("NO_HOLDINGS", "No holdings found"));

            if (portfolio.getQuantity() < order.getQuantity()) {
                throw new BusinessException("INSUFFICIENT_SHARES", "Insufficient shares");
            }

            portfolio.setQuantity(portfolio.getQuantity() - order.getQuantity());

            if (portfolio.getQuantity() == 0) {
                portfolioRepository.delete(portfolio);
            } else {
                portfolioRepository.save(portfolio);
            }

            wallet.setTotalBalance(wallet.getTotalBalance().add(totalAmount));
        }

        order.setStatus(OrderStatus.FILLED);

        walletRepository.save(wallet);
        Order saved = orderRepository.save(order);
        orderRealtimeStreamService.publishOrderUpdate(user.getId(), "EXECUTED", saved);
        return saved;
    }

    /**
     * STEP 3: CANCEL ORDER (Release Locked Funds)
     */
    @Transactional
    public Order cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("ORDER_NOT_PENDING", "Only pending orders can be cancelled");
        }

        if (order.getType() == OrderType.BUY) {

            Wallet wallet = walletRepository.findByUser(order.getUser())
                    .orElseThrow(() -> new BusinessException("WALLET_NOT_FOUND", "Wallet not found"));

            BigDecimal totalAmount = order.getPrice()
                    .multiply(BigDecimal.valueOf(order.getQuantity()));

            wallet.setLockedBalance(wallet.getLockedBalance().subtract(totalAmount));
            walletRepository.save(wallet);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        orderRealtimeStreamService.publishOrderUpdate(order.getUser().getId(), "CANCELLED", saved);
        return saved;
    }

    public Order getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException("FORBIDDEN", "Access denied to this order");
        }
        return order;
    }

    public Page<Order> getOrdersByUser(User user, Pageable pageable) {
        return orderRepository.findByUserOrderByIdDesc(user, pageable);
    }

    public Page<Order> getOrdersByUser(User user, OrderStatus status, Pageable pageable) {
        return orderRepository.findByUserAndStatusOrderByIdDesc(user, status, pageable);
    }

    public List<Order> getPendingOrdersByUser(User user) {
        return orderRepository.findByUserAndStatus(user, OrderStatus.PENDING);
    }

    public Page<Order> getFilledOrdersByUser(User user, Pageable pageable) {
        return orderRepository.findByUserAndStatusOrderByIdDesc(user, OrderStatus.FILLED, pageable);
    }

    /**
     * Executes pending orders that became eligible after a market price update.
     */
    @Transactional
    public void executeEligiblePendingOrdersForStock(Long stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new BusinessException("STOCK_NOT_FOUND", "Stock not found"));

        List<Order> pendingOrders = orderRepository.findByStockAndStatusOrderByIdAsc(stock, OrderStatus.PENDING);
        for (Order pending : pendingOrders) {
            boolean eligible =
                    (pending.getType() == OrderType.BUY && stock.getCurrentPrice().compareTo(pending.getPrice()) <= 0)
                            || (pending.getType() == OrderType.SELL && stock.getCurrentPrice().compareTo(pending.getPrice()) >= 0);

            if (eligible) {
                executeOrder(pending.getId());
            }
        }
    }
}