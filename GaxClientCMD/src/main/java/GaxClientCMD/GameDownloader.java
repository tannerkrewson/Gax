package GaxClientCMD;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.zeroturnaround.zip.ZipUtil;

public class GameDownloader {

    public boolean downloadGame(int gid){

        try {

            //first tell the server what we about to throw down
            JSONObject jo = GaxClient.sendCommand("download " + gid);
            System.out.println(jo.toString());
            //check if the game exists and if the server is ready to send to us
            boolean success = jo.getBoolean("success");
            if (!success) {
            System.out.println("Download game failed: Error code " + jo.getInt("reason"));
            return false;
            }
            URL downloadURL = new URL(jo.getString("url"));

            //URL downloadURL = new URL("http://www.solidbackgrounds.com/images/1920x1080/1920x1080-red-solid-color-background.jpg");
            
            //installation directory
            String sdir = GaxClient.cm.ConfigDir + "GaxGames/" + gid + "/";
            String sfile = gid + ".zip";
            
            File outputFile = new File(sdir + sfile);
            //this will create all of the required folders if need be
            (new File(sdir)).mkdirs();
            //this will not create a new file if one is already there
            outputFile.createNewFile();
            
            HttpURLConnection connection = (HttpURLConnection) downloadURL.openConnection();
            long downloaded = 0;
            //checks if it was paused before or not
            
            if (outputFile.exists()) {
                downloaded = (long) outputFile.length();
                connection.setRequestProperty("Range", "bytes=" + (outputFile.length()) + "-");
            } else {
                connection.setRequestProperty("Range", "bytes=" + downloaded + "-");
            }
            connection.setDoInput(true);
            connection.setDoOutput(true);
            long fileSize = connection.getContentLengthLong();
            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
            FileOutputStream fos = (downloaded == 0) ? new FileOutputStream(outputFile) : new FileOutputStream(outputFile, true);
            BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
            byte[] data = new byte[1024];
            int x = 0;
            int percent = 1;
            while ((x = in.read(data)) > 0) {
                bout.write(data, 0, x);
                downloaded += x;
                if (downloaded >= (fileSize / 100) * percent){
                    System.out.println(percent + "% complete");
                    percent++;
                }
            }
            in.close();
            bout.close();
            /*
            try {
            File outputFile = new File(sdir + sfile);
            //this will create all of the required folders if need be
            (new File(sdir)).mkdirs();
            //this will not create a new file if one is already there
            outputFile.createNewFile();
            URL website = new URL(downloadURL);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } catch (Exception ex) {
            System.out.println("Failed to download url to file");
            ex.printStackTrace();
            return false;
            }
            return true;*/
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
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
