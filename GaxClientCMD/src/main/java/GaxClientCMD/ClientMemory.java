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
            System.out.println(gid);
            ja.put(gid);
        }
        
        jo.put("installedgames", ja);
        saveConfig(jo);
    }

    //returns true if it successfully wrote the saved data to the memory, otherwise false
    public boolean getSavedConfig() {

        //trys to read config
        String config = readTextFile(ConfigDir + ConfigFile);

        //if its empty/not there, then our work here is done, otherwise we'll continue
        if (config == null) {
            System.out.println("Saved config not detected");
            return false;
        }
        System.out.println("Saved config detected");

        //reads the saved config and converts to json object
        JSONObject jo = new JSONObject(config);

        //puts the data from the config into the memory, and the GaxSession variables
        GaxClient.gs.curUser = jo.getString("username");
        GaxClient.gs.sessionID = jo.getString("sessionid");
        JSONArray tempja = jo.getJSONArray("installedgames");
        if (tempja != null) {
            for (int i = 0; i < tempja.length(); i++) {
                System.out.println(tempja.getInt(i));
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

    public String readTextFile(String fullPath) {

        try (BufferedReader br = new BufferedReader(new FileReader(fullPath))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        } catch (IOException e) {

        }
        return null;
    }
}
