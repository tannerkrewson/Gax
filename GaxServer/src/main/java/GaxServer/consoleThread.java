package GaxServer;

import java.util.Scanner;

public class consoleThread implements Runnable{

    static Scanner cmd = new Scanner(System.in);
    
    @Override
    public void run() {
        String userInput;
        while (true) {
            System.out.println("\nListening for console commands.");
            userInput = cmd.nextLine();
            if (userInput.equals("help")) {
                System.out.println("Possible commands: exit");
            } else if (userInput.equals("exit")) {
                System.exit(0);
            } else {
                System.out.println("Invalid command: " + userInput);
            }
        }
    }
    
}
