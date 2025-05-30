package com.marcgamboa.money_transfer.service;

import com.marcgamboa.money_transfer.model.Currency;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

@Service
public class CurrencyConversionService {

    private static final Map<String, BigDecimal> fxRates = Map.of(
            "AUD/USD", new BigDecimal("0.50"),
            "JPY/USD", new BigDecimal("0.0069"),
            "CNY/USD", new BigDecimal("0.14")
    );

    public BigDecimal convert(BigDecimal amount, Currency from, Currency to, String source) {
        if (from.equals(to)) return amount;

        return amount.setScale(4, RoundingMode.HALF_UP).multiply(getExchangeRate(from, to, source));
    }

    public BigDecimal getExchangeRate(Currency from, Currency to, String source) {
        if (from == to) {
            return BigDecimal.ONE;
        }

        BigDecimal usdRate = BigDecimal.ONE;
        BigDecimal sourceRate = BigDecimal.ONE;
        if (!from.equals(Currency.USD)) {
            usdRate = fxRates.get(from + "/USD");
            if (usdRate == null) throw new IllegalArgumentException("Missing FX rate x/USD" + from);
        }

        if(!Set.of(from.name(), to.name()).contains(source)) {
            sourceRate = fxRates.get(source + "/USD");
            usdRate = sourceRate.divide(usdRate, 8, RoundingMode.HALF_UP);
        }

        if (to.equals(Currency.USD)) return usdRate;
        BigDecimal targetRate = fxRates.get(to + "/USD");
        if (targetRate == null) throw new IllegalArgumentException("Missing FX rate x/USD" + to);
        return usdRate.divide(targetRate, 8, RoundingMode.HALF_UP);

    }
} 