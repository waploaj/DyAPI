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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class UtilityFunctions {

    // TODO 1: Fetch API configuration from the database based on api_code
    public static Map<String, Object> fetchApiConfig(String apiCode) {
        // Simulating database query
        Map<String, Object> apiConfig = new HashMap<>();
        if (apiCode.equals("BALANCE_API")) {
            apiConfig.put("apiCode", "BALANCE_API");
            apiConfig.put("className", "com.yourcompany.yourapp.ctl.BalanceCtl");
            apiConfig.put("methodName", "processBalance");
        }
        return apiConfig;
    }

    // TODO 2: Extract request parameters from the HttpServletRequest
    public static Map<String, String> getRequestParameters(HttpServletRequest req) {
        Map<String, String> requestParams = new HashMap<>();
        req.getParameterMap().forEach((key, values) -> {
            requestParams.put(key, values[0]);
        });
        return requestParams;
    }

    // TODO 3: Validate request parameters based on procctlmpg configuration
    public static boolean validateRequestParams(Map<String, Object> apiConfig, Map<String, String> requestParams) {
        // Simulating a check for mandatory "accountId"
        if (!requestParams.containsKey("accountId")) {
            return false;
        }
        return true;
    }

    // TODO: Method to dynamically set request parameters using 'set' methods from procctlmpg
    public static void setRequestParametersDynamically(String targetClassName, Map<String, String> requestParams) throws Exception {
        // Load the target class
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
}
