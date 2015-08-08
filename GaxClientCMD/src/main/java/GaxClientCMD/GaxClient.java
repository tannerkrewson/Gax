package GaxClientCMD;

import java.io.*;
import java.net.Socket;
import org.json.JSONObject;

public class GaxClient {

    //ip and port of the server
    static String ip = "localhost";
    static int port = 42924;

    static GaxSession gs = new GaxSession();
    static ConsoleUI cui = new ConsoleUI();
    static ClientMemory cm = new ClientMemory();

    public static void main(String args[]) {
        //console startup
        cui.startup();

        //if the config file exists, it will use the uname and session id
        //from that and skip login
        //in its current state, it will not validate until after the first 
        //command is sent to the server by the client
        boolean success = cm.getSavedConfig();
        if (!success) {
            //this will loop until the user has logged in
            cui.consoleLogin();
        } else if (success){
            //check if the saved session id is still valid
            //the command name doesn't matter, it's just
            //going to check the session id
            sendCommand("void");
        }

        //this will use this class's sendCommand method
        while (true) {
            cui.askForCommand();
        }

    }

    public static JSONObject sendCommand(String command) {
        //gets text command, sends json, receives & returns json
        try {
            Socket socket = new Socket(ip, port);
            System.out.println("");
            PrintStream ps = new PrintStream(socket.getOutputStream());
            System.out.println("Sending command: " + command);
            JSONObject sjo = baseJSON();
            sjo.put("command", command);

            //send the json as a string
            ps.println(sjo.toString());
            System.out.println("Command Sent. Waiting for reply...");
            InputStreamReader ir = new InputStreamReader(socket.getInputStream());
            BufferedReader br = new BufferedReader(ir);
            JSONObject rjo = new JSONObject(br.readLine());

            System.out.println("Response to command " + rjo.getString("responseToCommand") + " received.");
            //check to see if it was successful
            if (rjo.getBoolean("success") == false) {
                //if its not successful we'll check the reason error code.
                checkReason(rjo.getInt("reason"));
                //all uses of this function should check if it returns null
                //because if it does, that means the error code has already
                //been handled and should return to main
                //this probably needs to be changed because it janky
                return null;
            }
            return rjo;
        } catch (Exception e) {
            System.out.println("Can't connect to the server.");
        }
        return null;
    }

    public static JSONObject baseJSON() {
        //makes a json object with the stuff that will be sent with every command
        JSONObject jo = new JSONObject();
        jo.put("username", gs.curUser);
        jo.put("sessionID", gs.sessionID);
        return jo;
    }

    public static void runGame(String game) {
        System.out.println("Opening " + game);
        //sends command to server, receives JSON with appropriate response
        JSONObject jo = sendCommand("getPath " + game);
        if (jo == null) {
            //read comments in commandToJSONFromServer for what this is
            return;
        }
        runExecutable(jo.getString("path"));
    }

    public static void runExecutable(String path) {
        System.out.println("Running " + path);
        try {
            //Runtime runTime = Runtime.getRuntime();
            //runTime.exec(path);
            ProcessBuilder b = new ProcessBuilder(path);
            Process p = b.start();
        } catch (Exception e) {
            System.out.println("Failed to open " + path);
        }
    }

    public static void checkReason(int r) {
        switch (r) {
            case 1:
                System.out.println("Session has expired. Please login");
                cui.consoleLogin();
                return;
            //no break needed because we returned, i think
            case 0:
                System.out.println("WHAT THE FUCK IS GOING OOOOOOOON");
        }
    }
}
