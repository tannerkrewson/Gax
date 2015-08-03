package GaxServer;

import com.mongodb.*;
import com.mongodb.util.JSON;
import java.io.*;
import java.net.*;
import org.json.JSONObject;

public class server {

    static MongoClient mongoClient;
    static DB gaxDB;
    static Socket socket;

    public static void main(String args[]) {
        try {
            System.out.println("Gax Server \n");
            System.out.println("Starting up...");
            //refresh the databases if it can find them (do i need to do this more than once?)
            boolean rd = refreshDatabases();
            if (!rd) {
                System.out.println("Error connecting to database.");
                clientDataSender("Server couldn't connect to the database.");
                return;
            }
            ServerSocket server = new ServerSocket(42924);
            //all this shit needs to be in its own thread
            while (true) {
                try {
                    //wait for a connection
                    System.out.println("Waiting for connection...");
                    socket = server.accept();
                    //create streams
                    InputStreamReader isr = null;
                    try {
                        //waits for command here?
                        isr = new InputStreamReader(socket.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //use the data if it exists
                    if (isr != null) {
                        BufferedReader br = new BufferedReader(isr);
                        execCommand(br.readLine());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Server ended connection");
                }
            }
        } catch (IOException e) {
            System.out.println("293786");
            e.printStackTrace();
        }
    }

    public static void execCommand(String received) {
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

    public static void clientDataSender(String toSend) {
        try {
            PrintStream ps = new PrintStream(socket.getOutputStream());
            System.out.println("Sending: " + toSend);
            ps.println(toSend + "\n");
        } catch (Exception e) {
            System.out.println("Error sending data");
        }

    }

    public static boolean refreshDatabases() {
        System.out.println("Refreshing databases...");
        try {
            mongoClient = new MongoClient("localhost");
            gaxDB = mongoClient.getDB("gax");
            try {
                if (gaxDB.collectionExists("games")) {
                    return true;
                }
                System.out.println("Catastrophe");
            } catch (Exception e) {
                return false;
            }
            return true;
        } catch (MongoException e) {
            System.out.println("264566");
            e.printStackTrace();
            return false;
        }
    }

    public static String register(String username, String password) {
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

    public static String login(String username, String password) {
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

    public static String getPath(String game) {
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

    public static String listGames() {
        DBCursor cursor = gaxDB.getCollection("games").find();
        String listOfGames = "";
        while (cursor.hasNext()) {
            JSONObject json = new JSONObject(JSON.serialize(cursor.next()));
            listOfGames += json.getString("game") + ", ";
        }
        return listOfGames.substring(0, listOfGames.length() - 1);
    }

    public static String dboToString(DBObject dbo, String item) {
        JSONObject json = new JSONObject(JSON.serialize(dbo));
        return json.getString(item);
    }
}
