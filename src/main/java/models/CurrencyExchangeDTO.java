package models;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrencyExchangeDTO {
    private Currency baseCurrency;
    private Currency targetCurrency;
    private BigDecimal rate;
    private BigDecimal amount;
    private BigDecimal convertedAmount;

    public CurrencyExchangeDTO() {
    }

    public CurrencyExchangeDTO(Currency base, Currency target, double rate, double amount, double converted) {
        this.baseCurrency = base;
        this.targetCurrency = target;
        setRate(rate);
        setAmount(amount);
        setConvertedAmount(converted);
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(Currency baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(Currency targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = round(rate);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = round(amount);
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public void setConvertedAmount(double convertedAmount) {
        this.convertedAmount = round(convertedAmount);
    }

    private BigDecimal round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
