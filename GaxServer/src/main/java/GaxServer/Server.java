package GaxServer;

import java.io.*;
import java.net.*;

public class Server {

    private int serverPort;

    //static because we only need one db connection?
    static DatabaseConnection dbc = new DatabaseConnection();
    
    public Server(int port){
        serverPort = port;
    }

    public void startup() {
        System.out.println("\n\nGax Server \n");
        System.out.println("Starting up...");
    }

    public void startConsoleThread() {
        Runnable ct = new ConsoleThread();
        new Thread(ct).start();
    }

    public void connectToDatabase() {
        boolean rd = dbc.connect();
        if (!rd) {
            System.out.println("Error connecting to database.");
            System.exit(0);
        }
    }

    public void loadMemory() {
        //refresh the databases if it can find them (only need to do this once)
        //load the server memory, this can be done manually via console
        ServerMemory.loadAll();
    }

    public void waitForClients() {
        try {
            //start looking for clients
            ServerSocket servsock = new ServerSocket(serverPort);
            while (true) {
                try {
                    //wait for a connection on server.accept
                    System.out.println("Waiting for connection...");
                    Runnable r = new ServerThreadRunner(servsock.accept());

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
    
    public void setServerPort(int port){
        serverPort = port;
    }
    
    public int getServerPort(){
       return serverPort;
    }
}
