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
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/


package com.daraja.daraja.utility;

import com.daraja.daraja.service.DatabaseService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class ErrorHandlingUtility {

    private static DatabaseService dbservice;

    public static DatabaseService getDatabaseService() {
        if (dbservice == null) {
            dbservice = new DatabaseService();
        }
        return dbservice;
    }

    private static final ErrorHandlingUtility instance = new ErrorHandlingUtility();
    private Map<String, Object> statusMap = new HashMap<>();

    // Constructor initializes with no error
    public ErrorHandlingUtility() {
        statusMap.put("status", "SUCCESS");
        statusMap.put("message", "No errors found.");
    }

    public static ErrorHandlingUtility getInstance() {
        return instance;
    }

    // Method to set an error status
    public void setError(String message) {
        statusMap.put("status", "ERROR");
        statusMap.put("message", message);
    }

    // Method to check if an error exists
    public boolean checkStatus() {
        return statusMap.get("status").equals("ERROR");
    }

    // Method to return the error or success status map as a JSON string
    public String getResponseJson() {
        return UtilityFunctions.convertToJson(statusMap);
    }

    // Optional method to clear error
    public void clearError() {
        statusMap.put("status", "SUCCESS");
        statusMap.put("message", "No errors found.");
    }


    public void setErrorByCode(String errorCode) {
        String _query = "SELECT error_message FROM error_messages WHERE error_code = ?";

        List<Map<String, Object>> results = getDatabaseService().executeQuery(_query, Collections.singletonList(errorCode));
        if(!results.isEmpty()){
            statusMap.put("status","ERROR");
            statusMap.put("message", results.get(0).get("error_message"));
        }
    }

    public String  getErrorByCode(String errorCode) {
        String _query = "SELECT error_message FROM error_messages WHERE error_code = ?";

        List<Map<String, Object>> results = getDatabaseService().executeQuery(_query, Collections.singletonList(errorCode));
        return results.get(0).get("error_message").toString();
    }

}
