package GaxServer;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import java.sql.ResultSet;
import java.util.UUID;
import org.json.JSONObject;

public class sessionManager {

    //note that the user is logged in and verified when this is ran
    public String newSession(String username, String clientIP) {

        try {
            //check for old session in the db and delete them
            ResultSet rs = server.dbc.query("SELECT * FROM USERS.LOGIN WHERE USERNAME = '" + username + "';");
            rs.close();
            /* we shouldn't need to delete the old sessionid
             while (rs.next()) {
             //if the username matches, delete that old shit
             if (username.equals(rs.getString("username"))) {
             System.out.println("Deleting old session: " + rs.getString("sessionid"));
             server.dbc.query("UPDATE USERS.LOGIN set SESSIONID = '' WHERE USERNAME = " + username + ";");
             }
             }
             */
            
            //put the session in the database
            String sid = generateID();
            System.out.println("UPDATE USERS.LOGIN SET SESSIONID = '" + sid + "' WHERE USERNAME = '" + username + "';");
            server.dbc.update("UPDATE USERS.LOGIN SET SESSIONID = '" + sid + "' WHERE USERNAME = '" + username + "';");

            //return the session id as a string
            return sid;
        } catch (Exception ex) {
            System.out.println("Failed to get new session");
            ex.printStackTrace();
            return null;
        }
    }

    public Boolean checkSession(String username, String sessionid) {
        try {
            ResultSet rs = server.dbc.query("SELECT * FROM USERS.LOGIN "
                    + "WHERE USERNAME = '" + username + "' AND "
                    + "SESSIONID = '" + sessionid + "';");
            if (rs.next()) {
                System.out.println("Check session result was not null :)");
                return true;
            }
            System.out.println("Check session result was null :(");
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Failed to check session");
            return false;
        }
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
