package ru.nsu.ccfit;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import ru.nsu.ccfit.Handlers.CommonHandlers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

public class Server {
    private static DBConnection dbConnection = new DBConnection();

    private static RoutingHandler loadSettings(String path) {
        RoutingHandler routes = new RoutingHandler().setFallbackHandler(CommonHandlers::notFoundHandler)
                .setInvalidMethodHandler(CommonHandlers::wrongMethodHandler);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)))) {
            String route = reader.readLine();
            while (route != null) {
                String[] parsedRoute = route.split("\\s+");
                if (parsedRoute.length != 3) {
                    throw new IllegalArgumentException();
                }
                routes.add(parsedRoute[0], parsedRoute[1], (HttpHandler) Class.forName(parsedRoute[2]).getDeclaredConstructor(dbConnection.getClass()).newInstance(dbConnection));
                route = reader.readLine();
            }
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return routes;
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;
        if (args.length == 0) {
            System.out.println("Usage [request file] <server address> <server port>");
            System.exit(0);
        }
        if (args.length == 3) {
            host = args[1];
            port = Integer.valueOf(args[2]);
        }

        RoutingHandler routes = loadSettings(args[0]);
        Undertow server = Undertow.builder().addHttpListener(port, host).setHandler(routes).build();
        server.start();
    }
}
