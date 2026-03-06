package com.tradeflow.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "portfolio",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "stock_id"}))
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Stock stock;

    @Column(nullable = false)
    private Integer quantity;

    public Portfolio() {}

    public Portfolio(User user, Stock stock, Integer quantity) {
        this.user = user;
        this.stock = stock;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Stock getStock() { return stock; }
    public Integer getQuantity() { return quantity; }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}