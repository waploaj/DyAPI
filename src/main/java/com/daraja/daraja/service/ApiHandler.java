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

import com.daraja.daraja.utility.ApiResponse;
import com.daraja.daraja.utility.UtilityFunctions;
import com.daraja.daraja.utility.ErrorHandlingUtility;
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

    ErrorHandlingUtility errorUtil = ErrorHandlingUtility.getInstance();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // TODO Step 1: Extract the API code from the URL
        List<Map<String, Object>>  api_preconfig = extractApiCodeFromUrl(req.getRequestURI());
        String apiCode = "";

        if (errorUtil.checkStatus()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(errorUtil.getResponseJson());
            return;
        }
        System.out.println(errorUtil.checkStatus());
        apiCode = api_preconfig.get(0).get("api_code").toString().trim();
        List<Map<String, Object>>  apiFetchConfig = UtilityFunctions.fetchApiConfig(apiCode);

        if (errorUtil.checkStatus()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(errorUtil.getResponseJson());
            return;
        }


        // TODO Step 3: Fetch and validate request parameters
        //if(apiFetchConfig.get(0).get("post_method").toString().trim().toLowerCase().equalsIgnoreCase("post")) {
            Map<String, String> requestParams = UtilityFunctions.getRequestParameters((javax.servlet.http.HttpServletRequest) req);
            if (!UtilityFunctions.validateRequestParams(apiFetchConfig, requestParams)) {
                errorUtil.setErrorByCode("ERR10003");
            }
        //}

        if (errorUtil.checkStatus()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(errorUtil.getResponseJson());
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
        String requestPath = UtilityFunctions.getAfterV1(url);
        List<Map<String, Object>> results = UtilityFunctions.getApiCode(requestPath);
        // Process all results
        if (!results.isEmpty()) {
            apiPreconfigList.addAll(results);
        }else{
            errorUtil.setErrorByCode("ERR10001");
        }
        return apiPreconfigList;
    }

}


