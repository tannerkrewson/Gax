package GaxClientCMD;

import java.io.*;
import org.json.JSONObject;
import org.zeroturnaround.zip.ZipUtil;

public class GameDownloader {

    public boolean downloadGame(int gid) {

        //first tell the server what we about to throw down
        JSONObject jo = GaxClient.sendCommand("download " + gid);

        //check if the game exists and if the server is ready to send to us
        boolean success = jo.getBoolean("success");
        if (!success) {
            System.out.println("Download game failed: Error code " + jo.getInt("reason"));
            return false;
        }

        //connect to server
        GaxClient.sc.disconnectFromServer();
        GaxClient.sc.connectToServer();
        
        //installation directory
        String sdir = GaxClient.cm.ConfigDir + "GaxGames/" + gid + "/";
        String sfile = gid + ".zip";

        byte[] aByte = new byte[1];
        int bytesRead;

        InputStream is = null;

        try {
            is = GaxClient.sc.socket.getInputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (is != null) {

            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            try {
                File dir = new File(sdir);
                File file = new File(sdir + sfile);
                //this will create all of the required folders if need be
                dir.mkdirs();
                //this will not create a new file if one is already there
                file.createNewFile();
                fos = new FileOutputStream(file);
                bos = new BufferedOutputStream(fos);
                //bytesRead = is.read(aByte, 0, aByte.length);
                do {
                    baos.write(aByte);
                    //checks to make sure a byte was read?
                    bytesRead = is.read(aByte);
                } while (bytesRead != -1);

                bos.write(baos.toByteArray());
                System.out.println("File received successfully!");
                bos.flush();
                bos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public boolean installGame(int gid) {

        //we need to check if it exists before we unzip
        System.out.println("Installing game " + gid);
        String outputFolder = GaxClient.cm.ConfigDir + "GaxGames\\" + gid + "\\";
        String zipFile = outputFolder + gid + ".zip";
   
        ZipUtil.unpack(new File(zipFile), new File(outputFolder));

        System.out.println("Finished installing game " + gid);
        
        //add the game to the installedgames list
        GaxClient.installedgames.add(gid);
        
        //write the config, including the list of installedgames
        GaxClient.cm.writeCurConfig();
        return true;
    }
}
