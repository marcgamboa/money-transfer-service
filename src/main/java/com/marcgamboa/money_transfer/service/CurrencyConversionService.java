package com.marcgamboa.money_transfer.service;

import com.marcgamboa.money_transfer.model.Currency;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class CurrencyConversionService {

    private static final Map<String, BigDecimal> fxRates = Map.of(
            "USD/AUD", new BigDecimal("0.50"),
            "USD/JPY", new BigDecimal("0.0069"),
            "USD/CNY", new BigDecimal("0.14")
    );

    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        if (from.equals(to)) return amount;

        return amount.setScale(4, RoundingMode.HALF_UP).multiply(getExchangeRate(from, to));
    }

    public BigDecimal getExchangeRate(Currency from, Currency to) {
        if (from == to) {
            return BigDecimal.ONE;
        }

        BigDecimal usdRate = BigDecimal.ONE;
        if (!from.equals(Currency.USD)) {
            usdRate = fxRates.get("USD/" + from);
            if (usdRate == null) throw new IllegalArgumentException("Missing FX rate USD/" + from);
        }

        if (to.equals(Currency.USD)) return usdRate;
        BigDecimal targetRate = fxRates.get("USD/" + to);
        if (targetRate == null) throw new IllegalArgumentException("Missing FX rate USD/" + to);
        return usdRate.divide(targetRate, 8, RoundingMode.HALF_UP);

    }
} 