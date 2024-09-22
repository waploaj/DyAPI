package com.daraja.daraja.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.InputStream;

public class DatabaseService {

    private String jdbcUrl;
    private String username;
    private String password;

    public DatabaseService() {
        loadDatabaseProperties();
    }

    // Load database properties from application.properties
    private void loadDatabaseProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
                return;
            }
            properties.load(input);
            this.jdbcUrl = properties.getProperty("spring.datasource.url");
            this.username = properties.getProperty("spring.datasource.username");
            this.password = properties.getProperty("spring.datasource.password");
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    // Method to execute a query with parameters and return results
    public List<Map<String, Object>> executeQuery(String query, List<Object> parameters) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);

            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            // Execute query
            resultSet = stmt.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Process ResultSet and fill results
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    row.put(columnName, resultSet.getObject(i));
                }
                results.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions appropriately
        } finally {
            // Close resources
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return results;
    }


    // Method to execute an update (insert, update, delete)
    public int executeUpdate(String query, List<Object> parameters) {
        int rowsAffected = 0;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            // Execute update
            rowsAffected = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }
        return rowsAffected;
    }
}
