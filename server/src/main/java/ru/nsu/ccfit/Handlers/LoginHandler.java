package ru.nsu.ccfit.Handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ru.nsu.ccfit.DBConnection;
import ru.nsu.ccfit.LoginInfo;

import java.io.InputStreamReader;
import java.io.Reader;

public class LoginHandler implements HttpHandler {
    private static Gson gson = new GsonBuilder().serializeNulls().create();
    private DBConnection connection;

    public LoginHandler(DBConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        exchange.startBlocking();
        Reader is = new InputStreamReader(exchange.getInputStream());
        JsonObject tmp = new JsonParser().parse(is).getAsJsonObject();
        try {
            LoginInfo token = connection.loginUser(tmp.get("username").getAsString(), tmp.get("password").getAsString());
            if (token == null) {
                CommonHandlers.authErrorHandler(exchange, "Wrong login/password");
            } else {
                exchange.setStatusCode(200);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseHeaders().remove(Headers.WWW_AUTHENTICATE);
                exchange.getResponseSender().send("{id:" + token.getId() + "\"token\":\"" + token.getToken() + "\"}");
            }
        } catch (NullPointerException e) {
            CommonHandlers.wrongFormatHandler(exchange);
        }
    }
}