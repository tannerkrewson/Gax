package GaxServer;

import com.mongodb.DBCursor;
import com.mongodb.util.JSON;
import java.io.*;
import java.net.Socket;
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
        DBCursor cursor = server.gaxDB.getCollection("games").find();
        while (cursor.hasNext()) {
            JSONObject json = new JSONObject(JSON.serialize(cursor.next()));

            //if the gid passes matches the one in the db
            if (json.getInt("gid") == gid) {
                return true;
            }
        }
        return false;
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
