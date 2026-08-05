package repository;

import models.Currency;
import models.ExchangeRates;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static config.Config.getConnection;

public class CurrencyRepository {

    public List<Currency> getAllCurrencies() throws SQLException {
        var sql = "SELECT * FROM Currencies";

        try (var connection = getConnection();
             var preparedStatement = connection.prepareStatement(sql);
             var resultSet = preparedStatement.executeQuery()) {

            List<Currency> currencies = new ArrayList<>();
            while (resultSet.next()) {
                Currency currency = new Currency();
                currency.setId(resultSet.getInt("id"));
                currency.setCode(resultSet.getString("code"));
                currency.setFullName(resultSet.getString("fullName"));
                currency.setSign(resultSet.getString("sign"));

                currencies.add(currency);
            }

            return currencies;
        }
    }

    public void insertCurrency(String code, String fullName, String sign) throws SQLException {
        var sql = "INSERT INTO Currencies (code, fullName, sign) VALUES (?, ?, ?)";

        try (var connection = getConnection();
             var preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, code);
            preparedStatement.setString(2, fullName);
            preparedStatement.setString(3, sign);

            preparedStatement.executeUpdate();
        }
    }

    public List<ExchangeRates> getAllExchangeRates() throws SQLException {
        String sql = "SELECT e.id, e.rate, c1.id, c1.fullName, c1.code, c1.sign, c2.id, c2.fullName, c2.code, c2.sign " +
                "FROM ExchangeRates e " +
                "JOIN Currencies c1 on c1.id = e.baseCurrencyId " +
                "JOIN Currencies c2 on c2.id = e.targetCurrencyId";

        try (var connection = getConnection();
             var preparedStatement = connection.prepareStatement(sql);
             var resultSet = preparedStatement.executeQuery()) {

            List<ExchangeRates> ratesList = new ArrayList<>();

            while (resultSet.next()) {
                ExchangeRates exchange = getExchange(resultSet);

                ratesList.add(exchange);
            }

            return ratesList;
        }
    }


    public Currency getCurrencyByCode(String code) throws SQLException {
        String sql = "SELECT * FROM Currencies WHERE code = ?";

        try (var connection = getConnection();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, code);

            var resultSet = preparedStatement.executeQuery();

            Currency currency = null;
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String fullName = resultSet.getString("fullName");
                String currentCode = resultSet.getString("code");
                String sign = resultSet.getString("sign");

                currency = new Currency(id, currentCode, fullName, sign);
            }

            return currency;
        }
    }

    public ExchangeRates getExchangeRateByCode(String code) throws SQLException {
        String firstCurrencyCode = code.substring(0, 3);
        String secondCurrencyCode = code.substring(3);


        String sql = "SELECT e.id, e.rate, c1.id, c1.fullName, c1.code, c1.sign, c2.id, c2.fullName, c2.code, c2.sign " +
                "FROM ExchangeRates e " +
                "JOIN Currencies c1 ON c1.id = e.baseCurrencyId " +
                "JOIN Currencies c2 ON c2.id = e.targetCurrencyId " +
                "WHERE c1.code = ? AND c2.code = ?";

        try (var connection = getConnection();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, firstCurrencyCode);
            preparedStatement.setString(2, secondCurrencyCode);

            var resultSet = preparedStatement.executeQuery();

            ExchangeRates exchangeRates = null;
            if (resultSet.next()) {
                exchangeRates = getExchange(resultSet);
            }

            return exchangeRates;
        }
    }


    public void insertExchangeRate(String baseCurrencyCode, String targetCurrencyCode, String rate) throws SQLException {
        double parsedDouble = Double.parseDouble(rate);

        List<Integer> ides = getExchangeIdes(baseCurrencyCode, targetCurrencyCode);

        String sql = "INSERT INTO ExchangeRates (baseCurrencyId, targetCurrencyId, rate)" +
                "VALUES (?, ?, ?)";

        try (var connection = getConnection();
             var preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, ides.get(0));
            preparedStatement.setInt(2, ides.get(1));
            preparedStatement.setDouble(3, parsedDouble);
            preparedStatement.executeUpdate();
        }
    }

    private List<Integer> getExchangeIdes(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        String sql1 = "SELECT * FROM Currencies WHERE code = ?";

        String sql2 = "SELECT * FROM Currencies WHERE code = ?";

        try (var connection = getConnection()) {

            var preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setString(1, baseCurrencyCode);
            var resultSet1 = preparedStatement1.executeQuery();

            int baseId = -1;
            if (resultSet1.next()) {
                baseId = resultSet1.getInt("id");
            }

            var preparedStatement2 = connection.prepareStatement(sql2);
            preparedStatement2.setString(1, targetCurrencyCode);
            var resultSet2 = preparedStatement2.executeQuery();

            int targetId = -1;
            if (resultSet2.next()) {
                targetId = resultSet2.getInt("id");
            }

            return List.of(baseId, targetId);
        }
    }


    private static ExchangeRates getExchange(ResultSet resultSet) throws SQLException {
        int idExchangeRates = resultSet.getInt("e.id");
        double rate = resultSet.getDouble("e.rate");

        int baseCurrencyId = resultSet.getInt("c1.id");
        String baseCurrencyFullName = resultSet.getString("c1.fullName");
        String baseCurrencyCode = resultSet.getString("c1.code");
        String baseCurrencySign = resultSet.getString("c1.sign");

        int targetCurrencyId = resultSet.getInt("c2.id");
        String targetCurrencyFullName = resultSet.getString("c2.fullName");
        String targetCurrencyCode = resultSet.getString("c2.code");
        String targetCurrencySign = resultSet.getString("c2.sign");


        Currency baseCurrency =
                new Currency(baseCurrencyId, baseCurrencyCode, baseCurrencyFullName, baseCurrencySign);

        Currency targetCurrency =
                new Currency(targetCurrencyId, targetCurrencyCode, targetCurrencyFullName, targetCurrencySign);

        return new ExchangeRates(idExchangeRates, baseCurrency, targetCurrency, rate);
    }

    public void changeRate(ExchangeRates exchangeRateByCode, double rate) throws SQLException {
        String sql = "UPDATE ExchangeRates SET rate = ? WHERE id = ?";

        try (var connection = getConnection();
             var preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDouble(1, rate);
            preparedStatement.setInt(2, exchangeRateByCode.getId());

            preparedStatement.executeUpdate();
        }
    }
}
