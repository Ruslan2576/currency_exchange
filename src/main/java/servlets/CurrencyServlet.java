package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.Currency;
import services.CurrencyService;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;


@WebServlet("/currencies")
public class CurrencyServlet extends HttpServlet {
    private final CurrencyService currencyService = new CurrencyService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            List<Currency> allCurrencies = currencyService.getAllCurrencies();
            response.setStatus(HttpServletResponse.SC_OK);

            // Конвертируем список в json и отправляем
            String json = objectMapper.writeValueAsString(allCurrencies);


            PrintWriter writer = response.getWriter();
            writer.write(json);

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        String fullName = request.getParameter("fullName");
        String sign = request.getParameter("sign");

        // Проверка на null и пустые значения
        if (code == null || code.trim().isEmpty() ||
                fullName == null || fullName.trim().isEmpty() ||
                sign == null || sign.trim().isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"All fields are required\"}");
            return; // Важно! Прерываем выполнение
        }


        try {
            // Вставляем валюту в базу
            currencyService.insertCurrency(code.trim(), fullName.trim(), sign.trim());

            // Успешно добавили валюту
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"message\": \"Currency added successfully\"}");

        } catch (SQLException e) {
            e.printStackTrace();

            // Обработка дубликата
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Duplicate entry") || errorMessage.contains("UNIQUE constraint failed")) {
                response.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                response.getWriter().write("{\"error\": \"Currency with code " + code + "' already exists\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Database error: " + e.getMessage() + "\"}");
            }
        }
    }
}
