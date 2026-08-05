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

@WebServlet("/exchangeRate/*")
public class ConcreteExchangeRate extends HttpServlet {
    private final CurrencyService currencyService = new CurrencyService();
    private final ObjectMapper objectMapper = new ObjectMapper();


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();

            // валюту не указали в адресе
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }


            ExchangeRates exchangeRate = currencyService.getExchangeRateByCode(pathInfo.substring(1));

            if (exchangeRate == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.getWriter().write(objectMapper.writeValueAsString(exchangeRate));
            response.setStatus(HttpServletResponse.SC_OK);


        } catch (SQLException e) {
            System.out.println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/x-www-form-urlencoded");
        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.trim().equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Currency pair is required\"}");
                return;
            }

            // Читаем rate из тела запроса вручную
            String rate = req.getParameter("rate");

            if (rate == null || rate.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Rate parameter is required\"}");
                return;
            }

            double newRate = Double.parseDouble(rate.trim());

            ExchangeRates exchangeRateByCode = currencyService.getExchangeRateByCode(pathInfo.substring(1));
            if (exchangeRateByCode == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Exchange rate not found\"}");
                return;
            }

            currencyService.changeRate(exchangeRateByCode, newRate);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Rate updated successfully\"}");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Database error\"}");
        } catch (NumberFormatException formatException) {
            System.out.println(formatException.getMessage());
        }
    }
}
