package ru.nsu.ccfit.Handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import ru.nsu.ccfit.DBConnection;
import ru.nsu.ccfit.StoredFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;

public class FileDownloadHandler implements HttpHandler {
    private static Gson gson = new GsonBuilder().serializeNulls().create();
    private DBConnection connection;

    public FileDownloadHandler(DBConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }
        exchange.startBlocking();
        try {
            StoredFile file = connection.getFile(Integer.parseInt(exchange.getQueryParameters().get("id").getFirst()), exchange.getQueryParameters().get("name").getFirst());
            if (file == null) {
                CommonHandlers.notFoundHandler(exchange);
                return;
            }
            byte buffer[] = null;
            try (FileInputStream reader = new FileInputStream(
                    System.getProperty("user.dir") + File.separator + "uploads" +
                            File.separator + Integer.parseInt(exchange.getQueryParameters().get("id").getFirst()) +
                            File.separator + exchange.getQueryParameters().get("name").getFirst())) {
                buffer = reader.readAllBytes();
            }
            exchange.setStatusCode(200);
            exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getOriginalName() + "\"");
            exchange.getResponseHeaders().remove(Headers.WWW_AUTHENTICATE);
            exchange.getResponseSender().send(ByteBuffer.wrap(buffer));
        } catch (NullPointerException e) {
            CommonHandlers.wrongFormatHandler(exchange);
        } catch (Exception e) {
            //cause this try can throw IOException in case of large payload but jdk cannot detect that
            CommonHandlers.notFoundHandler(exchange);
        }
    }
}