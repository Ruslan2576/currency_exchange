package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.CurrencyExchangeDTO;
import services.CurrencyService;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    private final CurrencyService currencyService = new CurrencyService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws  IOException {
        try {
            String from = req.getParameter("from");
            String to = req.getParameter("to");
            String amount = req.getParameter("amount");

            if (from == null || from.trim().isEmpty() ||
                    to == null || to.trim().isEmpty() ||
                    amount == null || amount.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"message\": \"All field are required\"}");
                return;
            }

            int parsedAmount = Integer.parseInt(amount);

            CurrencyExchangeDTO exchanger = currencyService.calculateCurrencyExchange(from, to, parsedAmount);

            if (exchanger == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"message\": \"Currency isn't found\"");
                return;
            }

            resp.getWriter().write(objectMapper.writeValueAsString(exchanger));
            resp.setStatus(HttpServletResponse.SC_OK);

        } catch (NumberFormatException formatException) {
            System.out.println(formatException.getMessage());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\": \"Database error" + e.getMessage() + "\"}");
        }
    }
}
