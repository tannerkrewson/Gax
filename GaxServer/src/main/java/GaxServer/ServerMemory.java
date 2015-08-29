package GaxServer;

import java.sql.ResultSet;
import java.util.ArrayList;

public class ServerMemory {

    private static ArrayList<Game> gl = new ArrayList<>();

    public static void loadAll() {
        System.out.println("Reloading server memory");
        loadGamesList();
    }

    public static ArrayList<Game> gamesList() {
        //will expand this to validate if user is premium and act as such
        return gl;
    }

    private static void loadGamesList() {
        ResultSet rs;
        try {
            System.out.println("Loading games");
            rs = Server.dbc.query("SELECT * FROM GAMES.INFO;");
            while (rs.next()) {
                Game tempgame = new Game();
                tempgame.gid = rs.getInt("gid");
                tempgame.title = rs.getString("title");
                gl.add(tempgame);
            }
            rs.close();
        } catch (Exception ex) {
            System.out.println("Failed to load games list");
            ex.printStackTrace();
        }
    }
}
