package GaxServer;

import java.io.*;
import java.net.*;

public class server {
    
    static String dbip = "192.168.1.150";
    static int port = 42924;
    
    static DatabaseConnection dbc = new DatabaseConnection();

    public static void main(String args[]) {
        
        System.out.println("\n\nGax Server \n");
        System.out.println("Starting up...");
        //starts the console thread
        Runnable ct = new consoleThread();
        new Thread(ct).start();
        try {
            
            //refresh the databases if it can find them (only need to do this once)
            boolean rd = dbc.connect();
            if (!rd) {
                System.out.println("Error connecting to database.");
                return;
            }
            //load the server memory, this can be done manually via console
            serverMemory.loadAll();

            //start looking for clients
            ServerSocket server = new ServerSocket(port);
            while (true) {
                try {
                    //wait for a connection on server.accept
                    System.out.println("Waiting for connection...");
                    Runnable r = new serverThread(server.accept());

                    //make a new thread for the socket
                    new Thread(r).start();

                } catch (Exception e) {
                    e.printStackTrace(System.out);
                    System.out.println("Server ended connection");
                }
            }
        } catch (IOException e) {
            if (e.getMessage().startsWith("Address already in use")) {
                System.out.println("Server is already running!");
                System.exit(0);
            }
            e.printStackTrace(System.out);
        }
    }
}
