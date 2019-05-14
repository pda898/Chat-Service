package ru.nsu.ccfit.Handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ru.nsu.ccfit.DBConnection;

import java.io.InputStreamReader;
import java.io.Reader;

public class RegisterHandler implements HttpHandler {
    private static Gson gson = new GsonBuilder().serializeNulls().create();
    private DBConnection connection;

    public RegisterHandler(DBConnection connection) {
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
            boolean result = connection.createUser(tmp.get("username").getAsString(), tmp.get("password").getAsString());
            if (!result) {
                CommonHandlers.authErrorHandler(exchange, "User already exist");
            } else {
                exchange.setStatusCode(200);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseHeaders().remove(Headers.WWW_AUTHENTICATE);
                exchange.getResponseSender().send("{\"message\":\"Welcome\"}");
            }
        } catch (NullPointerException e) {
            CommonHandlers.wrongFormatHandler(exchange);
        }
    }
}
