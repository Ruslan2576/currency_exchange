package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.ExchangeRates;
import services.CurrencyService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private final CurrencyService currencyService = new CurrencyService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            List<ExchangeRates> ratesList = currencyService.getAllExchangeRates();
            response.setStatus(HttpServletResponse.SC_OK);

            String ratesJson = objectMapper.writeValueAsString(ratesList);

            response.getWriter().write(ratesJson);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/x-www-form-urlencoded");

        try {
            String baseCurrencyCode = request.getParameter("baseCurrencyCode");
            String targetCurrencyCode = request.getParameter("targetCurrencyCode");
            String rate = request.getParameter("rate");

            if (baseCurrencyCode == null || baseCurrencyCode.trim().isEmpty() ||
                    targetCurrencyCode == null || targetCurrencyCode.trim().isEmpty() ||
                    rate == null || rate.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            currencyService.insertExchangeRate(baseCurrencyCode.trim(), targetCurrencyCode.trim(), rate.trim());
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setHeader("Location", "/exchangeRate/" + baseCurrencyCode + targetCurrencyCode);

        } catch (SQLException e) {
            System.out.println(e.getMessage());

            String message = e.getMessage();
            if (message.contains("Duplicate entry") || message.contains("UNIQUE constraint failed")) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
