package GaxServer;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.Date;
import com.mongodb.util.JSON;
import java.util.Random;
import java.util.UUID;
import org.json.JSONObject;

public class sessionManager {

    //note that the user is logged in and verified when this is ran
    public String newSession(DB gaxDB, String username, String clientIP) {

        //check for old session in the db and delete them
        DBCollection col = gaxDB.getCollection("sessions");
        DBCursor cursor = col.find();
        //while there is another document in the collection, continue checking
        //next() returns a value and advances itself when called
        while (cursor.hasNext()) {
            DBObject dbo = cursor.next();
            String dbItem = dboToString(dbo, "username");
            //if the username matches, delete that old shit
            if (username.equals(dbItem)) {
                System.out.println("Deleting old session: " + dbo.toString());
                col.remove(dbo);
            }
        }

        //put the session in the database
        Date date = new Date();
        String sid = generateID();
        BasicDBObject document = new BasicDBObject();
        document.put("username", username);
        document.put("ip", clientIP);
        document.put("date", date);
        document.put("sessionID", sid);
        gaxDB.getCollection("sessions").insert(document);

        //return the session id as a string
        return sid;
    }

    public Boolean checkSession(DB gaxDB, String username, String sessionid) {
        DBCursor cursor = gaxDB.getCollection("sessions").find();
        //while there is another document in the collection, continue checking
        //next() returns a value and advances itself when called
        while (cursor.hasNext()) {
            DBObject dbo = cursor.next();
            String dbItem = dboToString(dbo, "username");

            //if the username matches and the sessionid matches
            //we'll add a more complicated check, like timebomb, eventually
            if (username.equals(dbItem) && sessionid.equals(dboToString(dbo, "sessionID"))) {
                System.out.println("Session is good!");
                return true;
            }
            //if theyre not equal, prompt login
        }
        //after its searched through the whole database we can assume the user needs to login
        return false;
    }

    public static String generateID() {
        String id = UUID.randomUUID().toString().replaceAll("-", "");
        return id;
    }

    public String dboToString(DBObject dbo, String item) {
        JSONObject json = new JSONObject(JSON.serialize(dbo));
        return json.getString(item);
    }
}
