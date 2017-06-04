/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bplib.priceData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Kofola
 */
public class Bar {

//    1. Generic ASCII in M1 Bars (1 Minute Bars):
//        Row Fields:
//    DateTime Stamp;Bar OPEN Bid Quote;Bar HIGH Bid Quote;Bar LOW Bid Quote;Bar CLOSE Bid Quote;Volume
//    TimeZone: Eastern Standard Time (EST) time-zone WITHOUT Day Light Savings 
//    DateTime Stamp Format:
//    YYYYMMDD HHMMSS
//    
//    Legend:
//    YYYY – Year
//    MM – Month (01 to 12)
//    DD – Day of the Month
//    HH – Hour of the day (in 24h format)
//    MM – Minute
//    SS – Second, in this case it will be allways 00
    private final LocalDateTime dateTime;
    private final String dateTimeFormat;
    private final BigDecimal openBid;
    private final BigDecimal highBid;
    private final BigDecimal lowBid;
    private final BigDecimal closeBid;
    private final long volume;

    public Bar(String dateTimeFormat, String dateTime, String openBid, String highBid, String lowBid, String closeBid, String volume) {

        this.dateTimeFormat = dateTimeFormat;
        this.dateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(dateTimeFormat));
        this.openBid = new BigDecimal(openBid);
        this.highBid = new BigDecimal(highBid);
        this.lowBid = new BigDecimal(lowBid);
        this.closeBid = new BigDecimal(closeBid);
        this.volume = Long.valueOf(volume);
    }

    /**
     * @return the dateTime
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /**
     * @return the dateTimeFormat
     */
    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    /**
     * @return the openBid
     */
    public BigDecimal getOpenBid() {
        return openBid;
    }

    /**
     * @return the highBid
     */
    public BigDecimal getHighBid() {
        return highBid;
    }

    /**
     * @return the lowBid
     */
    public BigDecimal getLowBid() {
        return lowBid;
    }

    /**
     * @return the volume
     */
    public long getVolume() {
        return volume;
    }

    /**
     * @return the closeBid
     */
    public BigDecimal getCloseBid() {
        return closeBid;
    }

}
