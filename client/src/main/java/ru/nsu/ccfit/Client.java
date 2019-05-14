package ru.nsu.ccfit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Client {
    private String host;
    private OkHttpClient client;
    private String token;
    private int uid;
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Message> messages = new ArrayList<>();
    private Type userListType = new TypeToken<ArrayList<User>>() {
    }.getType();
    private Type messageListType = new TypeToken<ArrayList<Message>>() {
    }.getType();
    private Type storedFileType = new TypeToken<ArrayList<StoredFile>>() {
    }.getType();
    private Gson g = new Gson();
    private int offset = 0;
    private Timer updateTimer = new Timer(500, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            updateMessages();
            updateUsers();
        }
    });

    public void setServer(String server) {
        this.host = host;
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(15);
        this.client = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .dispatcher(dispatcher)
                .build();
    }

    public boolean register(String username, String password) throws IOException {
        HttpUrl route = HttpUrl.parse(host + "/register");
        Request request = new Request.Builder()
                .url(route)
                .post(RequestBody.create(MediaType.parse("application/json"), "{\"username\":\"" + username + "\",\"password\"" + password + "}"))
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() != 200) {
            return false;
        }
        response.close();
        return true;
    }

    public boolean login(String username, String password) throws IOException {
        HttpUrl route = HttpUrl.parse(host + "/login");
        Request request = new Request.Builder()
                .url(route)
                .post(RequestBody.create(MediaType.parse("application/json"), "{\"username\":\"" + username + "\",\"password\"" + password + "}"))
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == 200) {
            JsonObject body = new JsonParser().parse(response.body().string()).getAsJsonObject();
            token = body.get("token").getAsString();
            uid = body.get("id").getAsInt();
            updateTimer.start();
            response.close();
            return true;
        }
        response.close();
        return false;
    }

    public void sendMessage(String message) throws IOException {
        HttpUrl route = HttpUrl.parse(host + "/messages");
        Request request = new Request.Builder()
                .url(route)
                .post(RequestBody.create(MediaType.parse("application/json"), "{\"message\":\"" + message + "\"}"))
                .addHeader("Authorization", "Token " + token)
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() != 200) {
            throw new IllegalArgumentException();
        }
        response.close();
    }

    private void updateUsers() {
        HttpUrl route = HttpUrl.parse(host + "/users");
        Request request = new Request.Builder()
                .url(route)
                .get()
                .addHeader("Authorization", "Token " + token)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 200) {
                String body = response.body().string();
                body = body.substring(body.indexOf('['), body.length() - 1);
                users = g.fromJson(body, userListType);
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateMessages() {
        ArrayList<Message> buffer = new ArrayList<>();
        int i = 0;
        while ((i == 0) || (buffer.size() != 0)) {
            buffer = new ArrayList<>();
            HttpUrl route = HttpUrl.parse(host + "/messages?offset=" + (100 * i + offset) + "&count=100");
            Request request = new Request.Builder()
                    .url(route)
                    .get()
                    .addHeader("Authorization", "Token " + token)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.code() == 200) {
                    String body = response.body().string();
                    buffer = g.fromJson(body, messageListType);
                    synchronized (messages) {
                        messages.addAll(buffer);
                    }
                    offset += buffer.size();
                } else {
                    throw new IllegalArgumentException();
                }
                i++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<User> getUsers() throws IOException {
        return users;
    }

    public List<Message> getMessages() throws IOException {
        ArrayList<Message> ret = new ArrayList<>(messages.size());
        synchronized (messages) {
            ret.addAll(messages);
            messages.clear();
        }
        return ret;
    }

    public boolean uploadFile(File file, String desc) throws IOException {
        HttpUrl route = HttpUrl.parse(host + "/files");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder fileContent = new StringBuilder();
        while (reader.ready()) {
            fileContent.append(reader.readLine());
        }
        Request request = new Request.Builder()
                .url(route)
                .post(RequestBody.create(MediaType.parse("application/json"), "{name:\"" + file.getName() +
                        "\",size:" + file.length() +
                        ",desc:\"" + desc +
                        "\",file:\"" + Base64.getEncoder().encodeToString(fileContent.toString().getBytes()) + "\"}"))
                .addHeader("Authorization", "Token " + token)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 200) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<StoredFile> getFiles() throws IOException {
        ArrayList<StoredFile> buffer = new ArrayList<>();
        int i = 0;
        while ((i == 0) || (buffer.size() != 0)) {
            buffer = new ArrayList<>();
            HttpUrl route = HttpUrl.parse(host + "/files");
            Request request = new Request.Builder()
                    .url(route)
                    .get()
                    .addHeader("Authorization", "Token " + token)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.code() == 200) {
                    String body = response.body().string();
                    buffer = g.fromJson(body, storedFileType);
                } else {
                    throw new IllegalArgumentException();
                }
                i++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buffer;
    }

    public int getUID() {
        return uid;
    }

    public void disconnect() throws IOException, IllegalArgumentException {
        HttpUrl route = HttpUrl.parse(host + "/logout");
        Request request = new Request.Builder()
                .url(route)
                .post(RequestBody.create(MediaType.parse("application/json"), ""))
                .addHeader("Authorization", "Token " + token)
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() != 200) {
            throw new IllegalArgumentException();
        }
        response.close();

    }

    public String getHost() {
        return host;
    }

    public boolean deleteFile(String name) {
        HttpUrl route = HttpUrl.parse(host + "/files/" + uid + "/" + name);
        Request request = new Request.Builder()
                .url(route)
                .post(RequestBody.create(MediaType.parse("application/json"), ""))
                .addHeader("Authorization", "Token " + token)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 200) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
