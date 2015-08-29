package GaxClient;

import java.io.*;
import java.nio.file.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class ClientMemory {

    String ConfigDir = System.getProperty("user.home") + "\\Gax\\";
    String ConfigFile = "config.json";

    public void writeCurConfig() {
        JSONObject jo = new JSONObject();
        jo.put("Gax Client Config", "version");
        jo.put("username", Client.gs.curUser);
        jo.put("sessionid", Client.gs.sessionID);
        jo.put("autoLogin", Client.autoLogin);

        JSONArray ja = new JSONArray();
        for (Integer gid : Client.installedgames) {
            ja.put(gid);
        }
        
        jo.put("installedgames", ja);
        saveConfig(jo);
    }

    //returns true if it successfully wrote the saved data to the memory, otherwise false
    public boolean getSavedConfig() {

        JSONObject jo;
        try {
            jo = Client.fr.readJSONFile(ConfigDir + ConfigFile);
        } catch (NoSuchFileException ex){
            System.out.println("Config file not found");
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        //puts the data from the config into the memory, and the GaxSession variables
        Client.gs.curUser = jo.getString("username");
        Client.gs.sessionID = jo.getString("sessionid");
        Client.autoLogin = jo.getBoolean("autoLogin");
        JSONArray tempja = jo.getJSONArray("installedgames");
        if (tempja != null) {
            for (int i = 0; i < tempja.length(); i++) {
                Client.installedgames.add(tempja.getInt(i));
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
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
