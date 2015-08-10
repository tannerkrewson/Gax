package GaxServer;

import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.util.ArrayList;
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
        if (sManager.checkSession(username, sessionID)) {
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
            server.dbc.update("INSERT INTO USERS.LOGIN (USERNAME,PASSWORD) "
                    + "VALUES ('" + username + "','" + password + "')");
            //send response to client
            JSONObject jo = new JSONObject();
            jo.put("responseToCommand", "register");
            jo.put("success", true);
            return jo;
        } catch (Exception e) {
            e.printStackTrace();
            //create JSON to be sent
            JSONObject jo = new JSONObject();
            jo.put("responseToCommand", "register");
            jo.put("success", false);
            jo.put("reason", 2);
            return jo;
        }
    }

    public JSONObject login(String username, String password) {
        System.out.println("Logging in " + username);
        try {
            ResultSet rs = server.dbc.query("SELECT * FROM USERS.LOGIN "
                    + "WHERE USERNAME = '" + username + "' AND "
                    + "PASSWORD = '" + password + "';");
            //if the username and password match, we're good to go
            if (rs.next()) {
                //register a session id                
                String cip = socket.getLocalSocketAddress().toString();
                String sid = sManager.newSession(username, cip);

                //create JSON to be sent
                JSONObject jo = new JSONObject();
                jo.put("responseToCommand", "login");
                jo.put("sessionID", sid);
                jo.put("success", true);
                return jo;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Failed to check login");
        }
        JSONObject jo = new JSONObject();
        jo.put("responseToCommand", "login");
        jo.put("success", false);
        jo.put("reason", 2);
        return jo;
    }

    public JSONObject listGames() {
        ArrayList<GaxGame> gl = serverMemory.gamesList();
        String listOfGames = "";
        for (GaxGame tempgame : gl) {
            listOfGames += tempgame.gid + "," + tempgame.title + "\n";
        }
        JSONObject jo = new JSONObject();
        jo.put("responseToCommand", "games");
        jo.put("success", true);
        jo.put("games", listOfGames);
        return jo;
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
