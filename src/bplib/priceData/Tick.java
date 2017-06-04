/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bplib.priceData;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author Kofola
 */
public class Tick {

    //overloads for volume and other
    //support for
    //2. Generic ASCII in Ticks:
    //Row Fields:
    //DateTime Stamp,Bid Quote,Ask Quote,Volume
    //TimeZone: Eastern Standard Time (EST) time-zone WITHOUT Day Light Savings 
    //DateTime Stamp Format:
    //YYYYMMDD HHMMSSNNN
    //
    //Legend:
    //YYYY – Year
    //MM – Month (01 to 12)
    //DD – Day of the Month
    //HH – Hour of the day (in 24h format)
    //MM – Minute
    //SS – Second
    //NNN – Millisecond
    //overloads
    private final LocalDateTime dateTime;
    private final String dateTimeFormat;
    private final BigDecimal bid;
    private final BigDecimal ask;
    private final long volume;

    public Tick(LocalDateTime dateTime, String dateTimeFormat, BigDecimal bid, BigDecimal ask, long volume) {
        this.dateTime = dateTime;
        this.dateTimeFormat = dateTimeFormat;
        this.bid = bid;
        this.ask = ask;
        this.volume = volume;
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
     * @return the bid
     */
    public BigDecimal getBid() {
        return bid;
    }

    /**
     * @return the ask
     */
    public BigDecimal getAsk() {
        return ask;
    }

    /**
     * @return the volume
     */
    public long getVolume() {
        return volume;
    }

}
