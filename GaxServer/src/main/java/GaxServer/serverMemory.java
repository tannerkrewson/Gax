package GaxServer;

import com.mongodb.DBCursor;
import com.mongodb.util.JSON;
import org.json.JSONObject;

public class serverMemory {

    private static String[][] gl;

    public static void loadAll() {
        System.out.println("Reloading server memory");
        loadGamesList();
    }

    public static String[][] gamesList() {
        //will expand this to validate if user is premium and act as such
        return gl;
    }

    private static void loadGamesList() {
        DBCursor cursor = server.gaxDB.getCollection("games").find();
        //determine and set the size of the array, which is number of games and 2
        gl = new String[(int) server.gaxDB.getCollection("games").getCount()][2];
        
        //loop to write all of the available games to our array
        int i = 0;
        while (cursor.hasNext()) {
            JSONObject json = new JSONObject(JSON.serialize(cursor.next()));
            gl[i][0] = json.getString("gid");
            gl[i][1] = json.getString("name");
            i++;
        }
    }
}
