package GaxClientCMD;

import java.util.Scanner;

public class ConsoleUI {

    Scanner cmd = new Scanner(System.in);

    public void startup() {
        System.out.println("\n\nGax Client Console\n");
    }

    public void askForCommand() {
        System.out.println("\nType a command:");
        processCommand(cmd.nextLine());
    }

    public void processCommand(String userInput) {
        if (userInput.equals("help")) {
            System.out.println("Possible commands: help, games, play game, exit");
        } else if (userInput.equals("test")) {
            //System.out.println(sendCommand("test"));
        } else if (userInput.equals("games")) {
            String temp = GaxClient.sendCommand("games").getString("games");
            System.out.println("\nGax Game Catalog (gid, name):");
            System.out.println(temp);
        } else if (userInput.startsWith("play ")) {
            String game = userInput.substring(5);
            GaxClient.runGame(game);
        } else if (userInput.equals("exit")) {
            System.out.println("Closing Gax Client");
            System.exit(0);
        } else {
            System.out.println("Invalid command: " + userInput);
        }
    }

    public void consoleLogin() {
        Boolean loginSuccess = false;
        while (loginSuccess == false) {
            System.out.println("\nLogin with your Gax account.");
            System.out.println("Type register as username to make a new account.");
            System.out.println("Username:");
            String u = cmd.nextLine();
            if (u.equals("register")) {
                consoleRegister();
                return;
            } else if (u.equals("exit")) {
                System.exit(0);
            }
            System.out.println("Password:");
            String p = cmd.nextLine();
            loginSuccess = GaxClient.gs.sendLogin(u, p);
        }
    }

    public void consoleRegister() {
        Boolean regSuccess = false;
        while (regSuccess == false) {
            System.out.println("\nRegister a new Gax account:");
            System.out.println("Username:");
            String newU = cmd.nextLine();
            System.out.println("Password:");
            String newP = cmd.nextLine();
            regSuccess = GaxClient.gs.sendReg(newU, newP);
        }
        consoleLogin();
    }
}
