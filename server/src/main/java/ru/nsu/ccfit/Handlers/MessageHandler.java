package ru.nsu.ccfit.Handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ru.nsu.ccfit.DBConnection;
import ru.nsu.ccfit.Message;

import java.io.InputStreamReader;
import java.io.Reader;

public class MessageHandler implements HttpHandler {
    private static Gson gson = new GsonBuilder().serializeNulls().create();
    private DBConnection connection;

    public MessageHandler(DBConnection connection) {
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
        JsonObject tmp = null;
        try {
            tmp = new JsonParser().parse(is).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String token = CommonHandlers.getToken(exchange);
        Integer user = connection.checkToken(token);
        if (user == null) {
            CommonHandlers.authErrorHandler(exchange, "Token error");
        } else {
            try {
                Message message = connection.createMessage(tmp.get("message").getAsString(), user);
                connection.updateToken(token);
                exchange.setStatusCode(200);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseHeaders().remove(Headers.WWW_AUTHENTICATE);
                exchange.getResponseSender().send("{\"id\":" + message.getId() + ",\"message\":\"" + message.getMessage() + "\"}");
            } catch (NullPointerException e) {
                CommonHandlers.wrongFormatHandler(exchange);
            }
        }
    }
}
