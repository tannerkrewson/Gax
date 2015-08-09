package GaxClientCMD;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONObject;

public class ClientMemory {

    String ConfigDir = System.getProperty("user.home") + "\\Gax\\";
    String ConfigFile = "config.json";

    public void writeCurConfig() {
        JSONObject jo = new JSONObject();
        jo.put("Gax Client Config", "version");
        jo.put("username", GaxClient.gs.curUser);
        jo.put("sessionid", GaxClient.gs.sessionID);

        JSONArray ja = new JSONArray();
        for (Integer gid : GaxClient.installedgames) {
            ja.put(gid);
        }
        
        jo.put("installedgames", ja);
        saveConfig(jo);
    }

    //returns true if it successfully wrote the saved data to the memory, otherwise false
    public boolean getSavedConfig() {

        JSONObject jo;
        try {
            jo = GaxClient.fr.readJSONFile(ConfigDir + ConfigFile);
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
            return false;
        }

        //puts the data from the config into the memory, and the GaxSession variables
        GaxClient.gs.curUser = jo.getString("username");
        GaxClient.gs.sessionID = jo.getString("sessionid");
        JSONArray tempja = jo.getJSONArray("installedgames");
        if (tempja != null) {
            for (int i = 0; i < tempja.length(); i++) {
                GaxClient.installedgames.add(tempja.getInt(i));
            }
        }
        return true;
    }

    public void deleteSavedConfig() {
        try {
            Files.delete(Paths.get(ConfigDir, ConfigFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveConfig(JSONObject obj) {
        File dir = new File(ConfigDir);
        File file = new File(ConfigDir + ConfigFile);
        try {
            //this will create all of the required folders if need be
            dir.mkdirs();

            //this will not create a new file if one is already there
            file.createNewFile();

            //write the JSON to the file
            FileWriter fw = new FileWriter(file);
            fw.write(obj.toString());
            System.out.println("Wrote JSON to file");
            System.out.println(obj);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
