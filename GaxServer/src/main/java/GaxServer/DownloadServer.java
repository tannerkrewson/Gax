package GaxServer;

import java.io.*;
import java.net.Socket;
import java.sql.ResultSet;
import org.json.JSONObject;

public class DownloadServer {

    Socket socket;
    serverThread st;

    public DownloadServer(Socket s, serverThread st) {
        socket = s;
        this.st = st;
    }

    public void sendGame(int gid) {
        /*  Current assumed format is GaxGames/gid/gid.zip  */

        //tell the client what it's getting itself into
        JSONObject njo = new JSONObject();
        if (gameExists(gid)) {
            //if the game exists, the client will get ready to receive
            njo.put("responseToCommand", "download " + gid);
            njo.put("success", true);
            st.clientDataSender(njo);
        } else {
            njo.put("responseToCommand", "download " + gid);
            njo.put("success", false);
            njo.put("reason", 2);
            st.clientDataSender(njo);
            return;
        }

        System.out.println("Sending game " + gid + " to client.");
        sendFile("GaxGames/" + gid + "/" + gid + ".zip");
    }

    private boolean gameExists(int gid) {
        try {
            ResultSet rs = server.dbc.query("SELECT * FROM GAMES.INFO "
                    + "WHERE GID = " + gid + ";");
            if (rs.next()) {
                System.out.println("Check gid result was not null :)");
                return true;
            }
            System.out.println("Check gid result was null :(");
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Failed to check gid");
            return false;
        }
    }

    private void sendFile(String path) {
        System.out.println("Sending " + path);
        BufferedOutputStream outToClient = null;
        try {
            outToClient = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (outToClient != null) {
            File myFile = new File(path);
            byte[] mybytearray = new byte[(int) myFile.length()];

            FileInputStream fis = null;

            try {
                fis = new FileInputStream(myFile);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            BufferedInputStream bis = new BufferedInputStream(fis);

            try {
                bis.read(mybytearray, 0, mybytearray.length);
                outToClient.write(mybytearray, 0, mybytearray.length);
                outToClient.flush();
                outToClient.close();
                socket.close();

                // File sent, exit the main method
                System.out.println("File sent successfully!");
                return;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
