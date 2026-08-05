package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.Currency;
import services.CurrencyService;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/currency/*")
public class ConcreteCurrencyServlet extends HttpServlet {
    private final CurrencyService currencyService = new CurrencyService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, SecurityException {
        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            pathInfo = pathInfo.substring(1);
            Currency currency = currencyService.getCurrencyByCode(pathInfo);

            if (currency == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.getWriter().write(objectMapper.writeValueAsString(currency));


        } catch (SQLException e) {
            System.out.println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
