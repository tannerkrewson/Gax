package GaxClientCMD;

import org.json.JSONObject;

public class GaxSession {

    String sessionID;
    String curUser;

    public Boolean sendLogin(String username, String password) {
        //making sure the username has no spaces and such
        if (!username.replaceAll("\\s+", "").equals(username)) {
            System.out.println("Username not valid. Please try again.");
            return false;
            //need to do this with the password too
        }
        System.out.println("Logging in...");
        //sends command to server, receives JSON with appropriate response
        JSONObject jo = GaxClient.sendCommand("login " + username + " " + password);
        if (jo == null) {
            //read comments in commandToJSONFromServer for what this is
            //returning false will loop consoleLogin
            return false;
        }
        sessionID = jo.getString("sessionID");
        curUser = username;
        System.out.println("Your session ID is " + sessionID);
        System.out.println("You are now logged in as " + username + "!");
        return true;
    }

    public Boolean sendReg(String username, String password) {
        //making sure the username has no spaces and such
        if (!username.replaceAll("\\s+", "").equals(username)) {
            System.out.println("Username not valid. Please try again.");
            return false;
        }
        System.out.println("Registering...");
        //sends command to server, receives JSON with appropriate response
        JSONObject jo = GaxClient.sendCommand("register " + username + " " + password);
        if (jo == null) {
            //read comments in commandToJSONFromServer for what this is
            //returning false will loop consoleLogin
            return false;
        }
        System.out.println("You are now registered as " + username + "!");
        System.out.println("You can now login.");
        return true;
    }
}
