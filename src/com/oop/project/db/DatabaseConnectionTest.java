package com.oop.project.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DatabaseConnectionTest {

    public static void main(String[] args) {
        String sql = "SELECT current_database(), current_user";

        try (
                Connection connection = ConnectionFactory.open(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                System.out.println("Database connection successful.");
                System.out.println("Database: " + result.getString(1));
                System.out.println("User: " + result.getString(2));
            }
        } catch (SQLException | IllegalStateException e) {
            System.err.println("Database connection test failed.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
