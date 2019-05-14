package ru.nsu.ccfit;

import java.io.File;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DBConnection {
    private static final long TIMEOUT = 30 * 60 * 1000L;
    private Connection c = null;
    private MessageDigest digest;

    public DBConnection() {
        try {
            digest = MessageDigest.getInstance("SHA-256");
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/chatservice",
                            "postgres", "");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

    public void close() {
        try {
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getUserPassword(String name) {
        try (PreparedStatement statement = c.prepareStatement("SELECT password FROM profiles WHERE nickname = ?;")) {
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String result = rs.getString(1);
                rs.close();
                return result;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean checkUserPassword(String name, String password) {
        String userPassword = getUserPassword(name);
        return userPassword.equals(Arrays.toString(digest.digest(password.getBytes())));
    }

    public boolean createUser(String name, String password) {
        if (getUserPassword(name) != null)
            return false;
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO profiles (nickname, signature, password, \"isAdmin\") VALUES (?,'',?,FALSE);")) {
            statement.setString(1, name);
            statement.setString(2, Arrays.toString(digest.digest(password.getBytes())));
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public LoginInfo loginUser(String name, String password) {
        if (!checkUserPassword(name, password))
            return null;
        int user_id = 0;
        try (PreparedStatement statement = c.prepareStatement(
                "SELECT id FROM profiles WHERE nickname = ?;")) {
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();
            rs.next();
            user_id = rs.getInt(1);
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String token = generateToken(name + password);
        try (PreparedStatement statement = c.prepareStatement(
                "INSERT INTO tokens (user_id, token, last_active) VALUES (?,?,?);")) {
            statement.setInt(1, user_id);
            statement.setString(2, token);
            statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
            return new LoginInfo(token, user_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUser(int uid) {
        try (PreparedStatement statement = c.prepareStatement(
                "SELECT id, nickname, signature FROM profiles WHERE id = ?;")) {
            statement.setInt(1, uid);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                User ret = new User(rs.getInt(1), rs.getString(2), rs.getString(3));
                rs.close();
                return ret;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAllUsers() {
        ArrayList<User> users = null;
        try (PreparedStatement statement = c.prepareStatement(
                "SELECT id, nickname, signature FROM profiles;")) {
            ResultSet rs = statement.executeQuery();
            users = new ArrayList<>();
            while (rs.next()) {
                User ret = new User(rs.getInt(1), rs.getString(2), rs.getString(3));
                users.add(ret);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    private String generateToken(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes()).toString();
    }

    public Integer checkToken(String token) {
        if (token == null) {
            return null;
        }
        try (PreparedStatement statement = c.prepareStatement(
                "SELECT user_id,last_active FROM tokens WHERE token = ?;")) {
            statement.setString(1, token);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                Integer ret = rs.getInt(1);
                Timestamp last = rs.getTimestamp(2);
                rs.close();
                if (last.before(new Timestamp(System.currentTimeMillis() - TIMEOUT))) {
                    unvalidateToken(token);
                    return null;
                }
                return ret;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void unvalidateToken(String token) {
        try (PreparedStatement delete_statement = c.prepareStatement(
                "DELETE FROM tokens WHERE token = ?;")) {
            delete_statement.setString(1, token);
            delete_statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateToken(String token) {
        try (PreparedStatement statement = c.prepareStatement(
                "UPDATE tokens SET last_active = ? WHERE token = ?;")) {
            statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            statement.setString(2, token);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(token);
            e.printStackTrace();
        }
    }

    public Message createMessage(String text, int authorID) {
        int currID = -1;
        try (PreparedStatement statement = c.prepareStatement("SELECT add_message(?,?);")) {
            statement.setInt(1, authorID);
            statement.setString(2, text);
            ResultSet rs = statement.executeQuery();
            rs.next();
            currID = rs.getInt(1);
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Message(currID, text, authorID, null);
    }

    public List<Message> getAllMessages(int offset, int count) {
        ArrayList<Message> result = null;
        try (PreparedStatement statement = c.prepareStatement("SELECT id,user_id,nickname,message FROM messages WHERE id >= ? ORDER BY id LIMIT ? ;")) {
            statement.setInt(1, offset);
            statement.setInt(2, count);
            ResultSet rs = statement.executeQuery();
            result = new ArrayList<>();
            while (rs.next()) {
                result.add(new Message(rs.getInt(1), rs.getString(4), rs.getInt(2), rs.getString(3)));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public File uploadFile(int uid, String originalName, int size, String desc) {
        try (PreparedStatement statement = c.prepareStatement("SELECT add_file(?,?,?,?,?);")) {
            statement.setInt(1, uid);
            statement.setString(2, originalName);
            String filename = UUID.randomUUID().toString();
            statement.setString(3, filename);
            statement.setInt(4, size);
            statement.setString(5, desc);
            ResultSet rs = statement.executeQuery();
            rs.next();
            int res = rs.getInt(1);
            if (res != 0) {
                rs.close();
                return null;
            }
            File file = new File(System.getProperty("user.dir") + File.separator + "uploads" + File.separator + uid + File.separator + filename);
            file.getParentFile().mkdirs();
            rs.close();
            return file;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StoredFile getFile(int uid, String name) {
        try (PreparedStatement statement = c.prepareStatement("SELECT name,original_name,filesize,description FROM files JOIN file_connections fc on files.id = fc.file_id WHERE user_id = ? AND name = ?;")) {
            statement.setInt(1, uid);
            statement.setString(2, name);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return new StoredFile(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<StoredFile> getFilesInfo(int uid) {
        ArrayList<StoredFile> files = null;
        try (PreparedStatement statement = c.prepareStatement("SELECT name,original_name,filesize,description FROM files JOIN file_connections fc on files.id = fc.file_id WHERE user_id = ?;")) {
            statement.setInt(1, uid);
            ResultSet rs = statement.executeQuery();
            files = new ArrayList<>(5);
            while (rs.next()) {
                files.add(new StoredFile(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return files;
    }

    public void deleteFile(int uid, String name) {
        try (PreparedStatement statement = c.prepareStatement("DELETE FROM files WHERE (id) IN (SELECT id FROM files JOIN file_connections fc on files.id = fc.file_id WHERE user_id = ? AND name = ?);")) {
            statement.setInt(1, uid);
            statement.setString(2, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
