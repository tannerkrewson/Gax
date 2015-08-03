package GaxServer;

import com.mongodb.*;
import java.io.*;
import java.net.*;

public class server {

    static MongoClient mongoClient;
    static DB gaxDB;
    static Socket socket;

    public static void main(String args[]) {
        //starts the console thread
        Runnable ct = new consoleThread();
        new Thread(ct).start();
        try {
            System.out.println("Gax Server \n");
            System.out.println("Starting up...");
            //refresh the databases if it can find them (only need to do this once)
            boolean rd = refreshDatabases();
            if (!rd) {
                System.out.println("Error connecting to database.");
                return;
            }
            ServerSocket server = new ServerSocket(42924);
            while (true) {
                try {
                    //wait for a connection
                    System.out.println("Waiting for connection...");
                    socket = server.accept();
                    //probably need to put the rest of this in the thread
                    //create streams
                    InputStreamReader isr = null;
                    try {
                        isr = new InputStreamReader(socket.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //use the data if it exists
                    if (isr != null) {
                        BufferedReader br = new BufferedReader(isr);
                        //old way
                        //execCommand(br.readLine());
                        Runnable r = new serverThread(br.readLine(), mongoClient, gaxDB, socket);
                        new Thread(r).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Server ended connection");
                }
            }
        } catch (IOException e) {
            System.out.println("293786");
            e.printStackTrace();
        }
    }

    public static boolean refreshDatabases() {
        System.out.println("Refreshing databases...");
        try {
            mongoClient = new MongoClient("localhost");
            gaxDB = mongoClient.getDB("gax");
            try {
                if (gaxDB.collectionExists("games")) {
                    return true;
                }
                System.out.println("Catastrophe");
            } catch (Exception e) {
                return false;
            }
            return true;
        } catch (MongoException e) {
            System.out.println("264566");
            e.printStackTrace();
            return false;
        }
    }
}
