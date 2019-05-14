package ru.nsu.ccfit.Handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ru.nsu.ccfit.DBConnection;
import ru.nsu.ccfit.User;

public class UserInfoHandler implements HttpHandler {
    private static Gson gson = new GsonBuilder().serializeNulls().create();
    private DBConnection connection;

    public UserInfoHandler(DBConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String token = CommonHandlers.getToken(exchange);
        Integer user = connection.checkToken(token);
        if (user == null) {
            CommonHandlers.authErrorHandler(exchange, "Old token");
        } else {
            connection.updateToken(token);
            User target = connection.getUser(Integer.valueOf(exchange.getQueryParameters().get("id").getFirst()));
            if (target == null) {
                CommonHandlers.notFoundHandler(exchange);
            } else {
                exchange.setStatusCode(200);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseHeaders().remove(Headers.WWW_AUTHENTICATE);
                exchange.getResponseSender().send(gson.toJson(target));
            }
        }
    }
}
