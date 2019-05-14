package ru.nsu.ccfit.Handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ru.nsu.ccfit.DBConnection;

public class LogoutHandler implements HttpHandler {
    private DBConnection connection;

    public LogoutHandler(DBConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String token = CommonHandlers.getToken(exchange);
        Integer user = connection.checkToken(token);
        if (user == null) {
            CommonHandlers.authErrorHandler(exchange, "Old token");
        } else {
            connection.unvalidateToken(token);
            exchange.setStatusCode(200);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send("{\"message\":\"bye!\"}");
        }
    }
}
