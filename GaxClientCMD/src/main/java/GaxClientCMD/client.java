package GaxClientCMD;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import org.json.JSONObject;

public class client {

    static Scanner cmd = new Scanner(System.in);
    static String ip = "localhost";
    static int port = 42924;
    static String sessionID;
    static String curUser;

    public static void main(String args[]) {
        String userInput;

        System.out.println("\n\nGax Client Console\n");
        consoleLogin();
        while (true) {
            System.out.println("\nType a command:");
            userInput = getInput();
            if (userInput.equals("help")) {
                System.out.println("Possible commands: help, games, play game, exit");
            } else if (userInput.equals("test")) {
                //System.out.println(sendCommand("test"));
            } else if (userInput.equals("games")) {
                String temp = sendCommand("games").getString("games");
                System.out.println("\nGax Game Catalog (gid, name):");
                System.out.println(temp);
            } else if (userInput.startsWith("play ")) {
                String game = userInput.substring(5);
                runGame(game);
            } else if (userInput.equals("exit")) {
                return;
            } else {
                System.out.println("Invalid command: " + userInput);
            }
        }
    }

    private static String getInput() {
        return cmd.nextLine();
    }

    public static JSONObject sendCommand(String command) {
        try {
            //gets text command, sends json, receives & returns json
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
            e.printStackTrace();
            System.out.println("Send or reply failed");
        }
        return null;
    }

    public static JSONObject baseJSON() {
        //makes a json object with the stuff that will be sent with every command
        JSONObject jo = new JSONObject();
        jo.put("username", curUser);
        jo.put("sessionID", sessionID);
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

    public static void consoleLogin() {
        Boolean loginSuccess = false;
        while (loginSuccess == false) {
            System.out.println("\nLogin with your Gax account.");
            System.out.println("Type register as username to make a new account.");
            System.out.println("Username:");
            String u = getInput();
            if (u.equals("register")) {
                consoleRegister();
                return;
            } else if (u.equals("exit")) {
                System.exit(0);
            }
            System.out.println("Password:");
            String p = getInput();
            loginSuccess = sendLogin(u, p);
        }
    }

    public static void consoleRegister() {
        Boolean regSuccess = false;
        while (regSuccess == false) {
            System.out.println("\nRegister a new Gax account:");
            System.out.println("Username:");
            String newU = getInput();
            System.out.println("Password:");
            String newP = getInput();
            regSuccess = sendReg(newU, newP);
        }
        consoleLogin();
    }

    public static Boolean sendLogin(String username, String password) {
        //making sure the username has no spaces and such
        if (!username.replaceAll("\\s+", "").equals(username)) {
            System.out.println("Username not valid. Please try again.");
            return false;
            //need to do this with the password too
        }
        System.out.println("Logging in...");
        //sends command to server, receives JSON with appropriate response
        JSONObject jo = sendCommand("login " + username + " " + password);
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

    public static Boolean sendReg(String username, String password) {
        //making sure the username has no spaces and such
        if (!username.replaceAll("\\s+", "").equals(username)) {
            System.out.println("Username not valid. Please try again.");
            return false;
        }
        System.out.println("Registering...");
        //sends command to server, receives JSON with appropriate response
        JSONObject jo = sendCommand("register " + username + " " + password);
        if (jo == null) {
            //read comments in commandToJSONFromServer for what this is
            //returning false will loop consoleLogin
            return false;
        }
        System.out.println("You are now registered as " + username + "!");
        System.out.println("You can now login.");
        return true;
    }

    public static void runExecutable(String path) {
        System.out.println("Running " + path);
        try {
            //Runtime runTime = Runtime.getRuntime();
            //runTime.exec(path);
            ProcessBuilder b = new ProcessBuilder(path);
            Process p = b.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkReason(int r) {
        switch (r) {
            case 1:
                System.out.println("Session has expired. Please login");
                consoleLogin();
                return;
            //no break needed because we returned, i think
            case 0:
                System.out.println("WHAT THE FUCK IS GOING OOOOOOOON");
        }
    }
}
