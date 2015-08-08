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
        } else if (userInput.startsWith("download ")){
            int gid = Integer.parseInt(userInput.substring(9));
            boolean abc = GaxClient.gd.downloadGame(gid);
            if (abc){
                boolean def = GaxClient.gd.installGame(gid);
            }
        } else if (userInput.equals("exit")) {
            System.out.println("Closing Gax Client");
            System.exit(0);
        } else {
            System.out.println("Invalid command: " + userInput);
        }
    }

    public boolean askYNQuestion(String question) {
        while (true) {
            System.out.println(question + " (Y/N)");
            String response = cmd.nextLine().toUpperCase().trim();
            if (response.equals("Y")) {
                return true;
            } else if (response.equals("N")) {
                return false;
            }
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
        
        //I put this here because consoleLogin is also called if the session id
        //that is stored in GaxClient.gs is not valid, otherwise i would have
        //put it with next to the consoleLogin call at startup
        consoleSaveID();
    }

    public void consoleSaveID() {
        boolean su = askYNQuestion("Would you like to save your session?");
        if (su) {
            GaxClient.cm.writeCurConfig();
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
