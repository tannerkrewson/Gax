package GaxServer;

import com.mongodb.*;
import com.mongodb.util.JSON;
import java.io.*;
import java.net.*;
import org.json.JSONObject;

public class serverThread implements Runnable {

    String received;
    MongoClient mongoClient;
    DB gaxDB;
    Socket socket;

    public serverThread(String c, MongoClient mc, DB db, Socket s) {
        received = c;
        mongoClient = mc;
        gaxDB = db;
        socket = s;
    }

    @Override
    public void run() {
        //execute command from client
        if (received.equals("test")) {
            clientDataSender("Test received!");
        } else if (received.startsWith("login ")) {
            clientDataSender(login(received.split(" ")[1], received.split(" ")[2]));
        } else if (received.startsWith("register ")) {
            clientDataSender(register(received.split(" ")[1], received.split(" ")[2]));
        } else if (received.equals("games")) {
            clientDataSender(listGames());
        } else if (received.startsWith("getPath ")) {
            String game = received.substring(8);
            clientDataSender(getPath(game));
        } else {
            System.out.println("Unknown client command: " + received);
        }
    }

    public void clientDataSender(String toSend) {
        try {
            PrintStream ps = new PrintStream(socket.getOutputStream());
            System.out.println("Sending: " + toSend);
            ps.println(toSend + "\n");
        } catch (Exception e) {
            System.out.println("Error sending data");
        }

    }

    public String register(String username, String password) {
        try {
            BasicDBObject document = new BasicDBObject();
            document.put("username", username);
            document.put("password", password);
            gaxDB.getCollection("login").insert(document);
            //create JSON to be sent
            JSONObject jo = new JSONObject();
            jo.put("responseToCommand", "register");
            jo.put("success", 1);
            return jo.toString();
        } catch (Exception e) {
            //create JSON to be sent
            JSONObject jo = new JSONObject();
            jo.put("responseToCommand", "register");
            jo.put("success", 0);
            jo.put("error", e.getMessage());
            return jo.toString();
        }
    }

    public String login(String username, String password) {
        System.out.println("Logging in " + username);
        DBCursor cursor = gaxDB.getCollection("login").find();
        //while there is another game in the collection, continue checking
        //next() returns a value and advances itself when called
        while (cursor.hasNext()) {
            DBObject dbo = cursor.next();
            String dbItem = dboToString(dbo, "username");
            if (username.equals(dbItem) && password.equals(dboToString(dbo, "password"))) {
                //create JSON to be sent
                JSONObject jo = new JSONObject();
                jo.put("responseToCommand", "login");
                jo.put("success", 1);
                //send a session id or seomthing?
                return jo.toString();
            }
        }
        JSONObject jo = new JSONObject();
        jo.put("responseToCommand", "login");
        jo.put("success", 0);
        return jo.toString();
    }

    public String getPath(String game) {
        System.out.println("Finding the path of " + game);
        DBCursor cursor = gaxDB.getCollection("games").find();
        //while there is another game in the collection, continue checking
        //next() returns a value and advances itself when called
        while (cursor.hasNext()) {
            DBObject dbo = cursor.next();
            String dbItem = dboToString(dbo, "game");
            if (game.equals(dbItem)) {
                String path = dboToString(dbo, "path");
                System.out.println("Path found: " + path);
                //create JSON to be sent
                JSONObject jo = new JSONObject();
                jo.put("responseToCommand", "getPath");
                jo.put("success", true);
                jo.put("path", path);
                return jo.toString();
            }
        }
        JSONObject jo = new JSONObject();
        jo.put("responseToCommand", "getPath");
        jo.put("success", false);
        return jo.toString();
    }

    public String listGames() {
        DBCursor cursor = gaxDB.getCollection("games").find();
        String listOfGames = "";
        while (cursor.hasNext()) {
            JSONObject json = new JSONObject(JSON.serialize(cursor.next()));
            listOfGames += json.getString("game") + ", ";
        }
        return listOfGames.substring(0, listOfGames.length() - 1);
    }

    public String dboToString(DBObject dbo, String item) {
        JSONObject json = new JSONObject(JSON.serialize(dbo));
        return json.getString(item);
    }
}
