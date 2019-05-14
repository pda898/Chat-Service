package ru.nsu.ccfit.Handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ru.nsu.ccfit.DBConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Base64;

public class FileUploadHandler implements HttpHandler {
    private static Gson gson = new GsonBuilder().serializeNulls().create();
    private DBConnection connection;

    public FileUploadHandler(DBConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        exchange.startBlocking();
        exchange.setMaxEntitySize(3 * 1024 * 1024);
        String token = CommonHandlers.getToken(exchange);
        Integer user = connection.checkToken(token);
        if (user == null) {
            CommonHandlers.authErrorHandler(exchange, "Token error");
        } else {
            try {
                Reader is = new InputStreamReader(exchange.getInputStream());
                JsonObject tmp = new JsonParser().parse(is).getAsJsonObject();
                int size = tmp.get("size").getAsInt();
                File upload = connection.uploadFile(user, tmp.get("name").getAsString(), size, tmp.get("desc").getAsString());
                try (FileOutputStream outputStream = new FileOutputStream(upload)) {
                    String b64File = tmp.get("file").getAsString();
                    outputStream.write(Base64.getDecoder().decode(b64File), 0, size);
                }
                exchange.setStatusCode(200);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                exchange.getResponseHeaders().remove(Headers.WWW_AUTHENTICATE);
                exchange.getResponseSender().send("{\"address\":\"" + upload.getName() + "\"}");
            } catch (NullPointerException e) {
                CommonHandlers.wrongFormatHandler(exchange);
            } catch (Exception e) {
                //cause this try can throw IOException in case of large payload but jdk cannot detect that
                CommonHandlers.notFoundHandler(exchange);
            }
        }
    }
}