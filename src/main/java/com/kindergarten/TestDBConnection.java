package com.kindergarten;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDBConnection {

    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/dbminikindergarten";
        String user = "postgres";
        String password = "maycem";

        System.out.println("Test de connexion à PostgreSQL...");
        System.out.println("URL: " + url);
        System.out.println("User: " + user);

        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ CONNEXION RÉUSSIE !");
            conn.close();
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver PostgreSQL non trouvé !");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ ÉCHEC de connexion !");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
