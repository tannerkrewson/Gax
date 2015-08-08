package GaxServer;

import com.mongodb.*;
import java.io.*;
import java.net.*;

public class server {

    static MongoClient mongoClient;
    static DB gaxDB;
    static String dbip = "192.168.1.150";
    static int port = 42924;

    public static void main(String args[]) {
        System.out.println("Gax Server \n");
        System.out.println("Starting up...");
        //starts the console thread
        Runnable ct = new consoleThread();
        new Thread(ct).start();
        try {
            //refresh the databases if it can find them (only need to do this once)
            boolean rd = refreshDatabases();
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

    public static boolean refreshDatabases() {
        try {
            System.out.println("Connecting to database...");
            mongoClient = new MongoClient(dbip);
            gaxDB = mongoClient.getDB("gax");
            return true;
        } catch (MongoException e) {
            System.out.println("Error connecting to the database.");
            return false;
        }
    }
}
