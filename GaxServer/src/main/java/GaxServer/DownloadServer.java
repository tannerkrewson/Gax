package GaxServer;

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
        //tell the client what it's getting itself into
        JSONObject njo = new JSONObject();
        njo.put("responseToCommand", "download " + gid);
        njo.put("success", true);
        System.out.println("//" + gid + "//");
        njo.put("path", "//" + gid + "//");
        njo.put("filename", "game.zip");
        st.clientDataSender(njo);

        System.out.println("Sending game " + gid + " to client.");
        sendFile("game.zip");
    }

    private void sendFile(String path) {
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
