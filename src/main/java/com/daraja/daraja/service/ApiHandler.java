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
import java.util.Map;

public class ApiHandler extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Step 1: Extract the API code from the URL
        String apiCode = extractApiCodeFromUrl(req.getRequestURI());

        // TODO Step 2: Fetch API configuration based on api_code
        Map<String, Object> apiConfig = UtilityFunctions.fetchApiConfig(apiCode);
        if (apiConfig == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "API not found");
            return;
        }

        // TODO Step 3: Fetch and validate request parameters
        Map<String, String> requestParams = UtilityFunctions.getRequestParameters((javax.servlet.http.HttpServletRequest) req);
        if (!UtilityFunctions.validateRequestParams(apiConfig, requestParams)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request parameters");
            return;
        }

        // Step 4: Dynamically set request parameters using the 'set' methods from procctlmpg
        try {
            // Fetch the class that contains the set methods from procctlcfg
            String targetClassName = (String) apiConfig.get("className");

            // Use UtilityFunctions to dynamically set parameters before invoking BO
            UtilityFunctions.setRequestParametersDynamically(targetClassName, requestParams);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error setting request parameters");
            return;
        }

        // TODO Step 4: Dynamically invoke the class and method specified in procctlcfg
        String className = (String) apiConfig.get("className");
        String methodName = (String) apiConfig.get("methodName");

        // TODO Step 5: Invoke the method dynamically
        try {
            UtilityFunctions.invokeMethod(className, methodName, requestParams, (javax.servlet.http.HttpServletResponse) resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error while invoking API");
        }
    }

    private String extractApiCodeFromUrl(String url) {
        // TODO: Logic to extract the API code from the URL
        String api_code = "10001";
        return api_code;
    }
}
