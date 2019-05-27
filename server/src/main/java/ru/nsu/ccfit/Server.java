package ru.nsu.ccfit;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import ru.nsu.ccfit.Handlers.CommonHandlers;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

public class Server {
    private static DBConnection dbConnection;

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
        if (args.length < 2) {
            System.out.println("Usage [request file] [db settings file] <server address> <server port>");
            System.exit(0);
        }
        if (args.length == 4) {
            host = args[2];
            port = Integer.valueOf(args[3]);
        }
        Properties dbSettings = new Properties();
        try {
            dbSettings.load(new FileReader(args[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        dbConnection = new DBConnection(
                dbSettings.getProperty("address", "127.0.0.1:5432"),
                dbSettings.getProperty("database", "chatservice"),
                dbSettings.getProperty("username", "postgres"),
                dbSettings.getProperty("password", ""));
        RoutingHandler routes = loadSettings(args[0]);
        Undertow server = Undertow.builder().addHttpListener(port, host).setHandler(routes).build();
        server.start();
    }
}
