package GaxClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import org.json.JSONObject;

public class ServerCommunicator {

    Socket socket;

    public boolean sendJSON(JSONObject sjo) {
        //gets text command, sends json, receives & returns json
        try {
            PrintStream ps = new PrintStream(socket.getOutputStream());
            //send the json as a string
            ps.println(sjo.toString());
        } catch (Exception ex) {
            System.out.println("Error sending JSON to server");
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public JSONObject receiveJSON() {
        InputStreamReader ir = null;
        JSONObject rjo;
        try {
            ir = new InputStreamReader(socket.getInputStream());
            BufferedReader br = new BufferedReader(ir);
            rjo = new JSONObject(br.readLine());
            System.out.println("Response to command " + rjo.getString("responseToCommand") + " received.");

            /* TODO: Rewrite this fucking checkReason shit */
            //check to see if it was successful
            /*
             if (rjo.getBoolean("success") == false) {
             //if its not successful we'll check the reason error code.
             GaxClient.checkReason(rjo.getInt("reason"));
             //all uses of this function should check if it returns null
             //because if it does, that means the error code has already
             //been handled and should return to main
             //this probably needs to be changed because it janky
             return null;
             }
             */
            ir.close();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            System.out.println("Error receiving JSON to server");
            return null;
        }
        return rjo;
    }

    public void connectToServer() {
        //attempt to connect to the server
        boolean connectedToServer = Client.sc.initConnection();
        while (!connectedToServer) {
            if (Client.cui.askYNQuestion("Try to connect again?")) {
                connectedToServer = Client.sc.initConnection();
            } else {
                System.exit(0);
            }
        }
    }

    public boolean initConnection() {
        try {
            socket = new Socket(Client.ip, Client.port);
            System.out.println("Successfully connected to server!");
        } catch (java.net.ConnectException e) {
            System.out.println("Couldn't connect to server.");
            return false;
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
            return false;
        }
        return true;
    }

    public void disconnectFromServer() {
        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println("Failed to close socket");
            ex.printStackTrace();
        }
    }
}
