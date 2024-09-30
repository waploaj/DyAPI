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
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class UtilityFunctions {

    private static DatabaseService dbservice;
    private static ErrorHandlingUtility errorUtil = ErrorHandlingUtility.getInstance();

    private UtilityFunctions() {
    }

    public static DatabaseService getDatabaseService() {
        if (dbservice == null) {
            dbservice = new DatabaseService();
        }
        return dbservice;
    }

    private static final String SQL_INJECTION_PATTERN =
            ".*(['\";--<>]|\\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|UNION|WHERE|OR|AND|BETWEEN|LIKE|HAVING|JOIN)\\b).*";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // TODO 1: Fetch API configuration from the database based on api_code
    public static List<Map<String, Object>>  fetchApiConfig(String apiCode) {
        // Simulating database query
        errorUtil.clearError();
        List<Map<String, Object>> apiFetchConfig = new ArrayList<>();

        if (!apiCode.trim().equalsIgnoreCase("")|| !(apiCode == null)) {
            List<Map<String, Object>> results = UtilityFunctions.getApiFetchConfig(apiCode);

            if (!results.isEmpty()) {
                apiFetchConfig.addAll(results);
                if(apiFetchConfig.get(0).get("post_method").toString().trim().toLowerCase().equalsIgnoreCase("post")){
                    //TODO this is post method we have to work on it
                }
            }else{
                errorUtil.setErrorByCode("ERR10002");
            }

        }
        return apiFetchConfig;
    }

    // TODO 2: Extract request parameters from the HttpServletRequest


    public static Map<String, String> getRequestParameters(HttpServletRequest req) {
        errorUtil.clearError();
        Map<String, String> requestParams = new HashMap<>();

        try {
            StringBuilder jsonPayload = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonPayload.append(line);
            }

            // Assuming the request body is in JSON format
            if (jsonPayload.length() > 0) {
                ObjectMapper objectMapper = new ObjectMapper();
                // Parse JSON into a map
                requestParams = objectMapper.readValue(jsonPayload.toString(), Map.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorUtil.setErrorByCode("ERR10005");
        }

        return requestParams;
    }


    // TODO 3: Validate request parameters based on procctlmpg configuration
    public static boolean validateRequestParams(List<Map<String, Object>> apiFetchConfig, Map<String, String> requestParams) {
        // Simulating a check for mandatory "element"
        //TODO to check for validation based on field validation class
        errorUtil.clearError();
        try
        {
            List<Map<String, Object>> apiFetchRequestParam = getRequestParam(apiFetchConfig.get(0).get("api_code").toString());
            System.out.println(apiFetchConfig);
            if(requestParams.containsKey("user_id")){
                validateUserIdAndProceed(requestParams.get("user_id"));
            }else{
                errorUtil.setErrorByCode("ERR10009");
            }

            if (errorUtil.checkStatus()) {
                return false;
            }

            if (apiFetchConfig.get(0).get("post_method").toString().trim().equalsIgnoreCase("post")) {
                for (int i = 0; i < apiFetchRequestParam.size(); i++) {
                    if (apiFetchRequestParam.get(i).get("is_mandatory").equals("1")) {
                        if (!requestParams.containsKey(apiFetchRequestParam.get(i).get("request_param").toString())) {
                            errorUtil.setError(errorUtil.getErrorByCode("ERR10004") +" -> "+
                                    apiFetchRequestParam.get(i).get("request_param").toString());
                            return false;
                        }

                    }
                }
            }
            //return true;
        }catch (Exception e){
            e.printStackTrace();
            errorUtil.setErrorByCode("ERR10006");
            return false;
        }
        return true;
    }

    // TODO: Method to dynamically set request parameters using 'set' methods from procctlmpg
    public static void setRequestParametersDynamically(String targetClassName, Map<String, String> requestParams) throws Exception {
        // Load the target class
        errorUtil.clearError();
        Class<?> targetClass = Class.forName(targetClassName);
        Object targetInstance = targetClass.getDeclaredConstructor().newInstance();

        // Simulate fetching the 'set' methods from procctlmpg
        // For example, procctlmpg could return set methods like setAccountId(), setAmount(), etc.
        Map<String, String> setMethods = fetchSetMethodsFromProcctlmpg(targetClassName);

        // Loop through each request parameter and invoke the corresponding set method dynamically
        for (Map.Entry<String, String> param : requestParams.entrySet()) {
            String paramName = param.getKey(); // e.g., "accountId"
            String paramValue = param.getValue(); // e.g., "12345"

            // Find the corresponding 'set' method
            String setMethodName = setMethods.get(paramName); // e.g., "setAccountId"
            if (setMethodName != null) {
                // Invoke the set method
                Method setMethod = targetClass.getMethod(setMethodName, String.class); // Assuming all params are String
                setMethod.invoke(targetInstance, paramValue);
            }
        }
    }

    // TODO: Simulate fetching set methods from procctlmpg table
    public static Map<String, String> fetchSetMethodsFromProcctlmpg(String targetClassName) {
        // Simulate a database lookup where procctlmpg defines mappings between request parameter names and set methods
        // For simplicity, let's assume we are dealing with BalanceCtl class as an example
        // Example: {"accountId": "setAccountId", "amount": "setAmount"}
        return Map.of("accountId", "setAccountId", "amount", "setAmount");
    }

    // Helper function to capitalize the first letter (e.g., accountId -> setAccountId)
    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // TODO 4: Fetch the set and get methods for request parameters from procctlmpg table
    // Simulating dynamic request parameter setting based on procctlmpg (config maintained in DB)
    public static void setRequestParamsDynamically(Object instance, Map<String, String> params) throws Exception {
        // Simulating fetching from procctlmpg for mandatory set methods
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String methodName = "set" + capitalize(entry.getKey());
            Method setterMethod = instance.getClass().getMethod(methodName, String.class);
            setterMethod.invoke(instance, entry.getValue());
        }
    }

    // TODO 5: Use reflection to invoke the method defined in procctlcfg
    public static void invokeMethod(String className, String methodName, Map<String, String> params, HttpServletResponse resp) throws Exception {
        // Load the class and method dynamically
        Class<?> clazz = Class.forName(className);
        Object instance = clazz.getDeclaredConstructor().newInstance();

        // Set request parameters dynamically using set methods
        setRequestParamsDynamically(instance, params);

        // Invoke the specified method with request parameters
        Method method = clazz.getMethod(methodName, Map.class, HttpServletResponse.class);
        method.invoke(instance, params, resp);
    }





    public static String getAfterV1(String url) {
        String prefix = "/v1/";
        // Convert the input string to lower case for case-insensitive comparison
        String lowerUrl = url.toLowerCase();
        int index = lowerUrl.indexOf(prefix);

        if (index != -1) {
            return url.substring(index + prefix.length());
        } else {
            //TODO having main table to store the error message define
            //each error with an errorcode
            //having common class that retrieve errorcode and display to user
            return "Invalid Request";
        }
    }

    public static List<Map<String, Object>> executeQuery(String query, List<Object> parameters) {
        DatabaseService dbService = getDatabaseService();
        try  {
            return dbService.executeQuery(query, parameters);
        } catch (Exception e) {
            // Handle SQL exception
            e.printStackTrace();
            return null;
        }
    }

    public static List<Map<String, Object>> getApiCode(String _afterVersion) {
        String requestPath = "";
        if(!containsSqlInjection(_afterVersion)){
            requestPath = _afterVersion;
        }
        String _query = "SELECT * FROM PUBLIC.PREAPICONFIG WHERE PATH = ?";

        List<Map<String, Object>> results = getDatabaseService().executeQuery(_query, Collections.singletonList(requestPath));

        return results;
    }

    public static List<Map<String, Object>> getApiFetchConfig(String api_code) {
        Long requestPath = 0L;
        if(!containsSqlInjection(api_code)){
            requestPath = Long.parseLong(api_code) ;
        }
        String _query = "SELECT * FROM APIFETCHCONFIG WHERE API_CODE = ?";

        List<Map<String, Object>> results = getDatabaseService().executeQuery(_query, Collections.singletonList(requestPath));

        return results;
    }

    public static List<Map<String, Object>> getRequestParam(String api_code){
        Long requestPath = 0L;
        if(!containsSqlInjection(api_code)){
            requestPath = Long.parseLong(api_code) ;
        }
        String _query = "SELECT * FROM APIFETCHPARAM WHERE API_CODE = ?";

        List<Map<String, Object>> results = getDatabaseService().executeQuery(_query, Collections.singletonList(requestPath));

        return results;
    }


    public static String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}"; // Return empty JSON in case of error
        }
    }






    public static boolean containsSqlInjection(String input) {
        if (input == null || input.isEmpty()) {
            return false; // No SQL injection possible
        }
        try {
            Pattern pattern = Pattern.compile(SQL_INJECTION_PATTERN, Pattern.CASE_INSENSITIVE);
            return pattern.matcher(input).matches();
        } catch (PatternSyntaxException e) {
           // e.printStackTrace(); // Log the error for debugging
            return false; // Default to safe if regex fails
        }
    }


    public static boolean validateUserIdAndProceed(String userId) throws Exception {
        errorUtil.clearError();
        if (userId == null || userId.trim().isEmpty()) {
            errorUtil.setErrorByCode("ERR10007");
            return false;
        }

        if (containsSqlInjection(userId)) {
            errorUtil.setErrorByCode("ERR10008");
            return false;
        }

        if (errorUtil.checkStatus()) {
            return false;
        }

        List<Map<String, Object>> userDetails = getUserDetailsById(userId);

        if (userDetails == null || userDetails.isEmpty()) {
            errorUtil.setErrorByCode("ERR10009");
            return false;
        }

        if (errorUtil.checkStatus()) {
            return false;
        }

        Map<String, Object> userDetail = userDetails.get(0);
        String userStatus = userDetail.get("user_status").toString();
        Object blockDate = userDetail.get("block_date");

        if (userStatus.equalsIgnoreCase("blocked") || blockDate != null) {
            errorUtil.setErrorByCode("ERR10010");
            return false;
        }

        if (errorUtil.checkStatus()) {
            return false;
        }
        if (userStatus.toLowerCase().trim().equalsIgnoreCase("active") && blockDate == null) {
            // Update the number_of_requests
            updateUserRequestCount(userId);
            if (errorUtil.checkStatus()) {
                return  false;
            }

        } else {
            errorUtil.setError("ERR10010");
            return false;
        }
        return true;
    }

    public static void updateUserRequestCount(String userId) throws  SQLException{
        String query = "UPDATE user_software_usage SET number_of_requests = number_of_requests + 1 WHERE user_id = ?";
        try {
            getDatabaseService().executeUpdate(query, Collections.singletonList(userId));
        }catch (Exception e){
            e.printStackTrace();
            errorUtil.setErrorByCode("ERR10011");
        }
    }

    public static List<Map<String, Object>> getUserDetailsById(String userId) {
        if(!containsSqlInjection(userId)) {
            String _query = "SELECT user_status, block_date, number_of_requests FROM user_software_usage WHERE user_id = ?";
            return getDatabaseService().executeQuery(_query, Collections.singletonList(userId));
        }else {
            errorUtil.setErrorByCode("ERR10008");
            return Collections.emptyList();
        }
    }




}
