package GaxClientCMD;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import org.json.JSONObject;

public class client {

    static Scanner cmd = new Scanner(System.in);
    static String ip = "192.168.1.150";
    static int port = 42924;
    
    public static void main(String args[]) {
        String userInput;
        
        System.out.println("\n\nGax Client Console\n");
        consoleLogin();
        while (true) {
            System.out.println("\nType a command:");
            userInput = getInput();
            if (userInput.equals("help")) {
                System.out.println("Possible commands: help, test, play game, exit");
            } else if (userInput.equals("test")) {
                System.out.println(sendCommand("test"));
            } else if (userInput.equals("games")) {
                System.out.println(sendCommand("games"));
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

    public static String getInput() {
        return cmd.nextLine();
    }

    public static String sendCommand(String command) {
        //legacy?
        try {
            Socket socket = new Socket(ip, port);
            System.out.println("");
            PrintStream ps = new PrintStream(socket.getOutputStream());
            System.out.println("Sending command: " + command);
            ps.println(command);
            System.out.println("Command Sent. Waiting for reply...");
            InputStreamReader ir = new InputStreamReader(socket.getInputStream());
            BufferedReader br = new BufferedReader(ir);
            return br.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Send or reply failed");
        }
        System.out.println("helptxt");
        return null;
    }
    
    public static JSONObject commandToJSONFromServer(String command) {
        try {
            Socket socket = new Socket(ip, port);
            System.out.println("");
            PrintStream ps = new PrintStream(socket.getOutputStream());
            System.out.println("Sending command: " + command);
            ps.println(command);
            System.out.println("Command Sent. Waiting for reply...");
            InputStreamReader ir = new InputStreamReader(socket.getInputStream());
            BufferedReader br = new BufferedReader(ir);
            JSONObject jo = new JSONObject(br.readLine());
            System.out.println("Response to command " + jo.getString("responseToCommand") + " received.");
            return jo;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Send or reply failed");
        }
        System.out.println("help");
        return null;
    }

    public static void runGame(String game) {
        System.out.println("Opening " + game);
        //sends command to server, receives JSON with appropriate response
        JSONObject jo = commandToJSONFromServer("getPath " + game);
        if (jo.getBoolean("success") == true) {
            runExecutable(jo.getString("path"));
        } else {
            System.out.println("Failed to open " + game);
        }
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
        }
        System.out.println("Logging in...");
        //sends command to server, receives JSON with appropriate response
        JSONObject jo = commandToJSONFromServer("login " + username + " " + password);
        switch (jo.getInt("success")) {
            //success
            case 1:
                System.out.println("You are now logged in as " + username + "!");
                return true;
            //unknown error
            case 0:
                System.out.println("Unknown login error");
                return false;
            //wrong u or p
            case 2:
                System.out.println("Wrong username or password. Try again.");
                return false;
        }
        return false;
    }
    
    public static Boolean sendReg(String username, String password) {
        //making sure the username has no spaces and such
        if (!username.replaceAll("\\s+", "").equals(username)) {
            System.out.println("Username not valid. Please try again.");
            return false;
        }
        System.out.println("Registering...");
        //sends command to server, receives JSON with appropriate response
        JSONObject jo = commandToJSONFromServer("register " + username + " " + password);
        switch (jo.getInt("success")) {
            //success
            case 1:
                System.out.println("You are now registered as " + username + "!");
                System.out.println("You can now login.");
                return true;
            //unknown error
            case 0:
                System.out.println("Unknown registering error");
                return false;
            //wrong u or p
            case 2:
                System.out.println("Invalid username or password. Try again.");
                return false;
        }
        return false;
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
}
