package GaxClientCMD;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.json.JSONObject;
import org.zeroturnaround.zip.ZipUtil;

public class GameDownloader {

    public boolean downloadGame(int gid) {

        //first tell the server what we about to throw down
        JSONObject jo = GaxClient.sendCommand("download " + gid);
        System.out.println(jo.toString());
        //check if the game exists and if the server is ready to send to us
        boolean success = jo.getBoolean("success");
        if (!success) {
            System.out.println("Download game failed: Error code " + jo.getInt("reason"));
            return false;
        }
        String downloadURL = jo.getString("url");

        //installation directory
        String sdir = GaxClient.cm.ConfigDir + "GaxGames/" + gid + "/";
        String sfile = gid + ".zip";

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
