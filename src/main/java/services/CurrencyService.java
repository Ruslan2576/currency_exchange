package services;

import models.CurrencyExchangeDTO;
import models.ExchangeRates;
import repository.CurrencyRepository;
import models.Currency;

import java.sql.SQLException;
import java.util.List;

public class CurrencyService {
    private final CurrencyRepository currencyRepository = new CurrencyRepository();

    public List<Currency> getAllCurrencies() throws SQLException {
        return currencyRepository.getAllCurrencies();
    }

    public void insertCurrency(String code, String fullName, String sign) throws SQLException{
        currencyRepository.insertCurrency(code, fullName, sign);
    }

    public List<ExchangeRates> getAllExchangeRates() throws SQLException {
        return currencyRepository.getAllExchangeRates();
    }

    public Currency getCurrencyByCode(String code) throws SQLException {
        return currencyRepository.getCurrencyByCode(code);
    }

    public ExchangeRates getExchangeRateByCode(String code) throws SQLException {
        return currencyRepository.getExchangeRateByCode(code);
    }

    public void insertExchangeRate(String baseCurrencyCode, String targetCurrencyCode, String rate) throws SQLException {
        currencyRepository.insertExchangeRate(baseCurrencyCode, targetCurrencyCode, rate);
    }

    public void changeRate(ExchangeRates exchangeRateByCode, double rate) throws SQLException {
        currencyRepository.changeRate(exchangeRateByCode, rate);
    }

    public CurrencyExchangeDTO calculateCurrencyExchange(String from, String to, double amount) throws SQLException {
        // 1. Прямая котировка from -> to
        ExchangeRates direct = getExchangeRateByCode(from + to);
        if (direct != null) {
            return new CurrencyExchangeDTO(direct.getBaseCurrency(), direct.getTargetCurrency(),
                    direct.getRate(), amount, amount * direct.getRate());

        }

        // 2. Обратная котировка to -> from
        ExchangeRates reverse = getExchangeRateByCode(to + from);
        if (reverse != null) {

            return new CurrencyExchangeDTO(reverse.getTargetCurrency(), reverse.getBaseCurrency(),
                    reverse.getRate(), amount, amount / reverse.getRate());
        }

        // 3. Кросс-курс через USD
        ExchangeRates fromToUsd = getExchangeRateByCode("USD" + from);
        ExchangeRates toToUsd = getExchangeRateByCode("USD" + to);

        if (fromToUsd != null || toToUsd != null) {
            // Для перевода из from в to через USD:
            // from -> USD -> to
            double inUsd = toToUsd.getRate() / fromToUsd.getRate();
            double result = amount * inUsd;

            return new CurrencyExchangeDTO(getCurrencyByCode(from), getCurrencyByCode(to),
                    result / amount, amount, result);
        }

        return null;
    }
}
