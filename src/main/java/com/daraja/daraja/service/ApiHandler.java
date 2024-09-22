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

import com.daraja.daraja.utility.UtilityFunctions;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiHandler extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Step 1: Extract the API code from the URL
        List<Map<String, Object>>  api_preconfig = extractApiCodeFromUrl(req.getRequestURI());
        String apiCode = "";
        if(!api_preconfig.get(0).containsKey("status")) {
            if (!api_preconfig.isEmpty() && api_preconfig.get(0).get("api_code") != null) {
                apiCode = api_preconfig.get(0).get("api_code").toString().trim();
                if (!apiCode.equalsIgnoreCase("")) {
                    // TODO: call validation based on API lifespan and validation required.
                }
            }
        }else{
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,api_preconfig.get(0).get("message").toString());
        }


        // TODO Step 2: Fetch API configuration based on api_code
        List<Map<String, Object>>  apiFetchConfig = UtilityFunctions.fetchApiConfig(apiCode);
        System.out.println(apiFetchConfig);
        if (apiFetchConfig.get(0).containsKey("status") ) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, api_preconfig.get(0).get("message").toString());
            return;
        }

        // TODO Step 3: Fetch and validate request parameters
        Map<String, String> requestParams = UtilityFunctions.getRequestParameters((javax.servlet.http.HttpServletRequest) req);
        if (!UtilityFunctions.validateRequestParams(apiFetchConfig, requestParams)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request parameters");
            return;
        }

        // Step 4: Dynamically set request parameters using the 'set' methods from procctlmpg
        try {
            // Fetch the class that contains the set methods from procctlcfg
            String targetClassName = (String) apiFetchConfig.get(0).get("className");
            //something to note here we many have a common class that check-
            //TABLE define data filed, columnn data length and all of table properties
            //To check if it is new or existing Table and now  to perform operation call-
            //by call the data operaion and manipulation of call -
            //saving class which will save the record to the deiine the data properties
            //and having common class to perfome such a task

            // Use UtilityFunctions to dynamically set parameters before invoking BO
            UtilityFunctions.setRequestParametersDynamically(targetClassName, requestParams);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error setting request parameters");
            return;
        }

        // TODO Step 4: Dynamically invoke the class and method specified in procctlcfg
        String className = (String) apiFetchConfig.get(0).get("className");
        String methodName = (String) apiFetchConfig.get(0).get("methodName");

        // TODO Step 5: Invoke the method dynamically
        try {
            UtilityFunctions.invokeMethod(className, methodName, requestParams, (javax.servlet.http.HttpServletResponse) resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error while invoking API");
        }
    }

    private List<Map<String, Object>> extractApiCodeFromUrl(String url) {
        List<Map<String, Object>> apiPreconfigList = new ArrayList<>();
        // Create a new map
        Map<String, Object> config = new HashMap<>();
        config.put("status", "ERROR");
        config.put("message", "Failed to load getApiCode");

        String requestPath = UtilityFunctions.getAfterV1(url);
        List<Map<String, Object>> results = UtilityFunctions.getApiCode(requestPath);

        // Process all results
        if (!results.isEmpty()) {
            apiPreconfigList.addAll(results);
        }else{
            apiPreconfigList.add(config);
        }

        return apiPreconfigList;
    }

}


