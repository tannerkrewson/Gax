package GaxClientCMD;

import java.io.*;
import java.util.ArrayList;
import org.json.JSONObject;

public class GaxClient {

    //ip and port of the server
    static String ip = "localhost";
    static int port = 42924;

    static GaxSession gs = new GaxSession();
    static ConsoleUI cui = new ConsoleUI();
    static ClientMemory cm = new ClientMemory();
    static GameDownloader gd = new GameDownloader();
    static FileReader fr = new FileReader(); //this is not the java.io one
    static ServerCommunicator sc = new ServerCommunicator();

    static boolean autoLogin;
    static ArrayList<Integer> installedgames = new ArrayList();

    public static void main(String args[]) throws InterruptedException {

        /* TODO: General GUI object that console and New Gui will extend */
        //console startup
        cui.startup();

        //attempt to connect to the server
        sc.connectToServer();

        //if the config file exists, it will use the uname and session id
        //from that and skip login
        //in its current state, it will not validate until after the first 
        //command is sent to the server by the client
        boolean success = cm.getSavedConfig();

        //if we successfully loaded the config and autoLogin was enabled
        if (success && autoLogin) {
            //check if the saved session id is still valid
            //the command name doesn't matter, it's just
            //going to check the session id
            sendCommand("void");
            //otherwise (e.g. the config file wasn't found or autoLogin wasn't picked)
        } else {
            //this will loop until the user has logged in
            cui.consoleLogin();
        }

        //this will use this class's sendCommand method
        while (true) {
            cui.askForCommand();

            try {
                sc.socket.close();
            } catch (Exception ex) {
                System.out.println("Socket already closed?");
                ex.printStackTrace(System.out);
            }
        }
    }

    public static JSONObject sendCommand(String command) {
        sc.connectToServer();

        //gets text command, sends json, receives & returns json
        System.out.println("Sending command: " + command);
        JSONObject sjo = baseJSON();
        sjo.put("command", command);

        if (!sc.sendJSON(sjo)) {
            //already handled?
            return null;
        }

        return sc.receiveJSON();
    }

    public static JSONObject baseJSON() {
        //makes a json object with the stuff that will be sent with every command
        JSONObject jo = new JSONObject();
        jo.put("username", gs.curUser);
        jo.put("sessionID", gs.sessionID);
        return jo;
    }

    public static void runGame(int gid) {
        System.out.println("Opening game " + gid);

        //check if it is installed
        for (Integer pgid : installedgames) {
            if (pgid == gid) {
                JSONObject jo;
                try {
                    //grab the exe path from the game's gax.json
                    jo = fr.readJSONFile(cm.ConfigDir + "GaxGames\\" + gid + "\\gax.json");

                    //run the exe path that we got from the game's files above
                    runExecutable(cm.ConfigDir + "GaxGames\\" + gid + "\\" + jo.getString("startPath"));
                } catch (IOException ex) {
                    ex.printStackTrace(System.out);
                    //the gax.json probably doesn't exists, we'll ask to install
                    break;
                }
                return;
            }
        }

        if (cui.askYNQuestion("Game not installed. Would you like to install it now?")) {
            System.out.println("THIS CODE WAS COPY PASTED FROM CONSOLEUI");
            boolean abc = GaxClient.gd.downloadGame(gid);
            if (abc) {
                boolean def = GaxClient.gd.installGame(gid);
            }
        }

        /* TODO: Tell the server we're playing the game,
         Track the exe to count gameplay time,
         etc.
         */
    }

    public static void runExecutable(String path) {
        System.out.println("Running " + path);
        try {
            //Runtime runTime = Runtime.getRuntime();
            //runTime.exec(path);
            ProcessBuilder b = new ProcessBuilder(path);
            Process p = b.start();
        } catch (Exception e) {
            System.out.println("Failed to open game: " + e.getLocalizedMessage());
        }
    }

    public static void checkReason(int r) {
        switch (r) {
            case 2:
                //void session check error code                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
                //actually its generic nvm
                return;
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
