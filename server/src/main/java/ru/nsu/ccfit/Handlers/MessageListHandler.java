package ru.nsu.ccfit.Handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ru.nsu.ccfit.DBConnection;


public class MessageListHandler implements HttpHandler {
    private static Gson gson = new GsonBuilder().serializeNulls().create();
    private DBConnection connection;

    public MessageListHandler(DBConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String token = CommonHandlers.getToken(exchange);
        Integer user = connection.checkToken(token);
        if (user == null) {
            CommonHandlers.authErrorHandler(exchange, "Token error");
        } else {
            connection.updateToken(token);
            int offset = 0, count = 10;
            if (exchange.getQueryParameters().get("offset") != null) {
                if (exchange.getQueryParameters().get("offset").size() != 0) {
                    offset = Integer.valueOf(exchange.getQueryParameters().get("offset").getFirst());
                }
            }
            if (exchange.getQueryParameters().get("count") != null) {
                if (exchange.getQueryParameters().get("count").size() != 0) {
                    count = Integer.valueOf(exchange.getQueryParameters().get("count").getFirst());
                }
            }
            exchange.setStatusCode(200);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseHeaders().remove(Headers.WWW_AUTHENTICATE);
            exchange.getResponseSender().send(gson.toJson(connection.getAllMessages(offset, count)));
        }
    }
}
