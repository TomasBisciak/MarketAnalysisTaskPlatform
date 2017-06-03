/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.model;

/**
 *
 * @author Kofola
 */
public class TradeDataPropertiesWrapper {

    private TradeDataProperties tradeDataProperties;

    public TradeDataPropertiesWrapper() {

    }

    public TradeDataPropertiesWrapper(TradeDataProperties tradeDataProperties) {
        this.tradeDataProperties = tradeDataProperties;
    }

    /**
     * @return the tradeDataProperties
     */
    public TradeDataProperties getTradeDataProperties() {
        return tradeDataProperties;
    }

    /**
     * @param tradeDataProperties the tradeDataProperties to set
     */
    public void setTradeDataProperties(TradeDataProperties tradeDataProperties) {
        this.tradeDataProperties = tradeDataProperties;
    }

}
