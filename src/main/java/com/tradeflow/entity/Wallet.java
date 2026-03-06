package com.tradeflow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal totalBalance;

    @Column(nullable = false)
    private BigDecimal lockedBalance;

    @Version
    private Long version;

    public Wallet() {}

    public Wallet(User user) {
        this.user = user;
        this.totalBalance = BigDecimal.ZERO;
        this.lockedBalance = BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public BigDecimal getTotalBalance() { return totalBalance; }
    public BigDecimal getLockedBalance() { return lockedBalance; }

    public BigDecimal getAvailableBalance() {
        return totalBalance.subtract(lockedBalance);
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public void setLockedBalance(BigDecimal lockedBalance) {
        this.lockedBalance = lockedBalance;
    }
}