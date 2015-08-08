package GaxServer;

import com.mongodb.*;
import com.mongodb.util.JSON;
import java.io.*;
import java.net.*;
import org.json.JSONObject;

public class serverThread implements Runnable {

    Socket socket;
    sessionManager sManager = new sessionManager();
    DownloadServer ds;

    public serverThread(Socket s) {
        socket = s;
    }

    @Override
    public void run() {
        //create streams
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(socket.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            //converts received string to JSONObject and passes it along
            execJSONCommand(new JSONObject(br.readLine()));
            isr.close();
            br.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //get the command from the client
    //validate the client's session
    //execute the command
    public void execJSONCommand(JSONObject jo) {
        //extract the command
        String received = jo.getString("command");

        //skip validation for commands that can be executed without a valid session
        if (!received.startsWith("login ") && !received.startsWith("register ")) {

            //check to see if our client has a valid session
            boolean vs = checkValidSession(jo);

            //if they don't then we tell them, otherwise execute the requested command
            if (vs == false) {
                //tell the client that it needs to relogin
                JSONObject sjo = new JSONObject();
                sjo.put("responseToCommand", received);
                sjo.put("success", false);
                sjo.put("reason", 1);
                //send it out
                clientDataSender(sjo);
                return;
            }
        }
        //execute command from client
        if (received.startsWith("test ")) {

        } else if (received.startsWith("login ")) {
            clientDataSender(login(received.split(" ")[1], received.split(" ")[2]));
        } else if (received.startsWith("register ")) {
            clientDataSender(register(received.split(" ")[1], received.split(" ")[2]));
        } else if (received.equals("games")) {
            clientDataSender(listGames());
        } else if (received.startsWith("getPath ")) {
            String game = received.substring(8);
            clientDataSender(getPath(game));
        } else if (received.startsWith("download ")) {
            String gid = received.substring(9);
            
            ds = new DownloadServer(socket, this);
            ds.sendGame(Integer.parseInt(gid));
        } else {
            System.out.println("Unknown client command: " + received);
            JSONObject njo = new JSONObject();
            njo.put("responseToCommand", received);
            njo.put("success", false);
            njo.put("reason", 2);
            clientDataSender(njo);
        }
    }

    public boolean checkValidSession(JSONObject jo) {
        //watch out, it could be null!!
        String username = jo.getString("username");
        String sessionID = jo.getString("sessionID");
        if (sManager.checkSession(server.gaxDB, username, sessionID)) {
            System.out.println("Session ID " + sessionID + " for " + username + " is valid!");
            return true;
        } else {
            System.out.println("Session ID " + sessionID + " for " + username + " is invalid!");
            return false;
        }
    }

    public void clientDataSender(JSONObject toSend) {
        try {
            String cmd = toSend.toString();
            PrintStream ps = new PrintStream(socket.getOutputStream());
            System.out.println("Sending: " + cmd);
            ps.println(cmd + "\n");
        } catch (Exception e) {
            System.out.println("Error sending data");
        }

    }

    public JSONObject register(String username, String password) {
        try {
            BasicDBObject document = new BasicDBObject();
            document.put("username", username);
            document.put("password", password);
            server.gaxDB.getCollection("login").insert(document);
            //create JSON to be sent
            JSONObject jo = new JSONObject();
            jo.put("responseToCommand", "register");
            jo.put("success", 1);
            return jo;
        } catch (Exception e) {
            //create JSON to be sent
            JSONObject jo = new JSONObject();
            jo.put("responseToCommand", "register");
            jo.put("success", false);
            jo.put("error", e.getMessage());
            return jo;
        }
    }

    public JSONObject login(String username, String password) {
        System.out.println("Logging in " + username);
        DBCursor cursor = server.gaxDB.getCollection("login").find();
        //while there is another game in the collection, continue checking
        //next() returns a value and advances itself when called
        while (cursor.hasNext()) {
            DBObject dbo = cursor.next();
            String dbItem = dboToString(dbo, "username");

            //if the username and password match, we're good to go
            if (username.equals(dbItem) && password.equals(dboToString(dbo, "password"))) {

                //register a session id                
                String cip = socket.getLocalSocketAddress().toString();
                String sid = sManager.newSession(server.gaxDB, username, cip);

                //create JSON to be sent
                JSONObject jo = new JSONObject();
                jo.put("responseToCommand", "login");
                jo.put("sessionID", sid);
                jo.put("success", true);
                return jo;
            }
        }
        JSONObject jo = new JSONObject();
        jo.put("responseToCommand", "login");
        jo.put("success", false);
        jo.put("reason", 2);
        return jo;
    }

    public JSONObject getPath(String game) {
        System.out.println("Finding the path of " + game);
        DBCursor cursor = server.gaxDB.getCollection("games").find();
        //while there is another game in the collection, continue checking
        //next() returns a value and advances itself when called
        while (cursor.hasNext()) {
            DBObject dbo = cursor.next();
            String dbItem = dboToString(dbo, "name");
            if (game.equals(dbItem)) {
                String path = dboToString(dbo, "path");
                System.out.println("Path found: " + path);
                //create JSON to be sent
                JSONObject jo = new JSONObject();
                jo.put("responseToCommand", "getPath");
                jo.put("success", true);
                jo.put("path", path);
                return jo;
            }
        }
        JSONObject jo = new JSONObject();
        jo.put("responseToCommand", "getPath");
        jo.put("success", false);
        return jo;
    }

    public JSONObject listGames() {
        String[][] gl = serverMemory.gamesList();
        String listOfGames = "";
        for (int i = 0; i < gl.length; i++) {
            listOfGames += gl[i][0] + "," + gl[i][1] + "\n";
        }
        JSONObject jo = new JSONObject();
        jo.put("responseToCommand", "games");
        jo.put("success", true);
        jo.put("games", listOfGames);
        return jo;
    }

    public String dboToString(DBObject dbo, String item) {
        JSONObject json = new JSONObject(JSON.serialize(dbo));
        return json.getString(item);
    }

    /*
     public static JSONObject baseJSON() {
     //makes a json object with the stuff that will be sent with every command
     JSONObject jo = new JSONObject();
     jo.put("s", curUser);
     jo.put("sessionID", sessionID);
     return jo;
     }*/
}
