package GaxServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    Connection connection = null;
    String dbip = "localhost";
    int dbport = 5432;

    public boolean connect() {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://" + dbip + ":" + dbport + "/gax", "postgres",
                    "gaxsuperuser");
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return false;
        }
        if (connection != null) {
            System.out.println("You made it, take control your database now!");
            return true;
        } else {
            System.out.println("Failed to make connection!");
            return false;
        }
    }

    public ResultSet query(String query) throws Exception {
        Statement stmt;
        connection.setAutoCommit(false);
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        //rs.close();
        //stmt.close();
        //connection.close();
        System.out.println("Query completed successfully");
        return rs;
    }

    public void update(String query) throws Exception {
        Statement stmt;
        connection.setAutoCommit(false);
        stmt = connection.createStatement();
        stmt.executeUpdate(query);
        connection.commit();
        //rs.close();
        //stmt.close();
        //connection.close();
        System.out.println("Update completed successfully");
    }
}
