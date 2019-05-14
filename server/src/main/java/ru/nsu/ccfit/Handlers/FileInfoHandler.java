package ru.nsu.ccfit.Handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ru.nsu.ccfit.DBConnection;


public class FileInfoHandler implements HttpHandler {
    private static Gson gson = new GsonBuilder().serializeNulls().create();
    private DBConnection connection;

    public FileInfoHandler(DBConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String token = CommonHandlers.getToken(exchange);
        Integer user = connection.checkToken(token);
        if (user == null) {
            CommonHandlers.authErrorHandler(exchange, "Token error");
        } else {
            try {

                exchange.setStatusCode(200);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseHeaders().remove(Headers.WWW_AUTHENTICATE);
                exchange.getResponseSender().send(gson.toJson(connection.getFilesInfo(user)));
            } catch (NullPointerException e) {
                CommonHandlers.wrongFormatHandler(exchange);
            } catch (Exception e) {
                //cause this try can throw IOException in case of large payload but jdk cannot detect that
                CommonHandlers.notFoundHandler(exchange);
            }
        }
    }
}