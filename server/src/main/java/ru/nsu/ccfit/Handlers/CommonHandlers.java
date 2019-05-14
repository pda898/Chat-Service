package ru.nsu.ccfit.Handlers;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class CommonHandlers {
    public static void wrongFormatHandler(HttpServerExchange exchange) {
        exchange.setStatusCode(400);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseHeaders().remove(Headers.WWW_AUTHENTICATE);
        exchange.getResponseSender().send("Error");
    }

    public static void authErrorHandler(HttpServerExchange exchange, String error) {
        exchange.setStatusCode(401);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send(error);
    }

    public static void notFoundHandler(HttpServerExchange exchange) {
        exchange.setStatusCode(404);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseHeaders().remove(Headers.WWW_AUTHENTICATE);
        exchange.getResponseSender().send("Error");

    }

    public static void wrongMethodHandler(HttpServerExchange exchange) {
        exchange.setStatusCode(405);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseHeaders().remove(Headers.WWW_AUTHENTICATE);
        exchange.getResponseSender().send("Error");
    }

    public static String getToken(HttpServerExchange exchange) {
        if (!exchange.getRequestHeaders().contains(Headers.AUTHORIZATION)) {
            return null;
        }
        return exchange.getRequestHeaders().get(Headers.AUTHORIZATION).getFirst().substring(6);
    }
}
