package GaxServer;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.util.Date;
import com.mongodb.util.JSON;
import java.net.Socket;
import java.util.Random;
import org.json.JSONObject;

public class sessionManager {

    public String newSession(DB gaxDB, String username, String clientIP) {
        Date date = new Date();
        String sid = generateID(clientIP);
        BasicDBObject document = new BasicDBObject();
        document.put("username", username);
        document.put("date", date);
        document.put("sessionID", sid);
        gaxDB.getCollection("sessions").insert(document);
        return sid;
    }

    public Boolean checkSession(DB gaxDB, String username, String sessionid) {
        DBCursor cursor = gaxDB.getCollection("sessions").find();
        //while there is another game in the collection, continue checking
        //next() returns a value and advances itself when called
        while (cursor.hasNext()) {
            DBObject dbo = cursor.next();
            String dbItem = dboToString(dbo, "username");
            //if the username matches and the sessionid matches
            if (username.equals(dbItem) && sessionid.equals(dboToString(dbo, "sessionID"))) {
                System.out.println("Session is good!");
                return true;
            }
            //if theyre not equal, prompt login
        }
        //after its searched through the whole database we can assume the user needs to login
        return false;
    }

    public static String generateID(String IP) {
        Random rand = new Random();
        String id = Integer.toString(rand.nextInt(2000000));
        //don't mind the insanity
        //String id = String.valueOf((Double.parseDouble(IP.replaceAll("[^\\d]", ""))) * 31 / 2 - 69).replaceAll("[^\\d]", "");
        return id;
    }

    public String dboToString(DBObject dbo, String item) {
        JSONObject json = new JSONObject(JSON.serialize(dbo));
        return json.getString(item);
    }
}
