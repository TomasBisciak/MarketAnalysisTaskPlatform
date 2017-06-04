/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bplib.tradeData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Kofola
 */
public class Trade {

    public static final int TRADE_TYPE_SHORT = 0;
    public static final int TRADE_TYPE_LONG = 1;

    private final int type;
    private final String marketName;
    private final BigDecimal amount;
    private final int leverage;

    private final BigDecimal stopLoss;
    private final BigDecimal target;
    private final BigDecimal entryPrice;
    private final BigDecimal exitPrice;

    private final LocalDateTime openDateTime;
    private final LocalDateTime closeDateTime;

    public Trade(int type, String marketName, BigDecimal amount, int leverage, BigDecimal stopLoss, BigDecimal target, BigDecimal entryPrice, BigDecimal exitPrice, String openDateTime, String closeDateTime) {
        this.type = type;
        this.marketName = marketName;
        this.amount = amount;
        this.leverage = leverage;
        this.stopLoss = stopLoss;
        this.target = target;
        this.entryPrice = entryPrice;
        this.exitPrice = exitPrice;
        this.openDateTime = LocalDateTime.parse(openDateTime, DateTimeFormatter.ofPattern("yyyyMMdd HHmmss"));
        this.closeDateTime = LocalDateTime.parse(closeDateTime, DateTimeFormatter.ofPattern("yyyyMMdd HHmmss"));
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @return the marketName
     */
    public String getMarketName() {
        return marketName;
    }

    /**
     * @return the amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * @return the leverage
     */
    public int getLeverage() {
        return leverage;
    }

    /**
     * @return the stopLoss
     */
    public BigDecimal getStopLoss() {
        return stopLoss;
    }

    /**
     * @return the target
     */
    public BigDecimal getTarget() {
        return target;
    }

    /**
     * @return the entryPrice
     */
    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    /**
     * @return the exitPrice
     */
    public BigDecimal getExitPrice() {
        return exitPrice;
    }

    /**
     * @return the openDateTime
     */
    public LocalDateTime getOpenDateTime() {
        return openDateTime;
    }

    /**
     * @return the closeDateTime
     */
    public LocalDateTime getCloseDateTime() {
        return closeDateTime;
    }

}
