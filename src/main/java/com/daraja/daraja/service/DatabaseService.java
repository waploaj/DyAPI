/*
MIT License

Copyright (c) 2024 Abdallah Galiya Tanzania Arusha
Email abdallah.galiya@gmail.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package com.daraja.daraja.service;

import com.daraja.daraja.utility.ErrorHandlingUtility;

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
    private static ErrorHandlingUtility errorUtil = ErrorHandlingUtility.getInstance();

    public DatabaseService() {
        loadDatabaseProperties();
    }

    // Load database properties from application.properties
    private void loadDatabaseProperties() {
        errorUtil.clearError();
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                errorUtil.setErrorByCode("");
                System.out.println("Sorry, unable to find application.properties");
                return;
            }
            if (errorUtil.checkStatus()) {
                return;
            }
            properties.load(input);
            this.jdbcUrl = properties.getProperty("spring.datasource.url");
            this.username = properties.getProperty("spring.datasource.username");
            this.password = properties.getProperty("spring.datasource.password");
        } catch (Exception e) {
            e.printStackTrace();
            errorUtil.setErrorByCode("ERR10013");
            return;
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

    public List<Map<String, Object>> getValidValues(List<String> columns, String tableName, Map<String, Object> whereConditions) {
        // Construct the SELECT clause
        String columnsClause = String.join(", ", columns);

        // Start building the SQL query
        StringBuilder query = new StringBuilder("SELECT " + columnsClause + " FROM " + tableName);

        // Construct the WHERE clause if there are conditions
        if (!whereConditions.isEmpty()) {
            query.append(" WHERE ");
            List<String> conditionList = new ArrayList<>();
            for (Map.Entry<String, Object> entry : whereConditions.entrySet()) {
                String columnName = entry.getKey();
                Object value = entry.getValue();
                conditionList.add(columnName + " = ?");
            }
            // Join the conditions with AND
            query.append(String.join(" AND ", conditionList));
        }

        // Prepare the parameters for the PreparedStatement
        List<Object> parameters = new ArrayList<>(whereConditions.values());

        // Execute the query and return the results
        return executeQuery(query.toString(), parameters);
    }

}
