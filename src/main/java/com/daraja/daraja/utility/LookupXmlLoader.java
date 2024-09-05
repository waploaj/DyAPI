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

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class LookupXmlLoader {

    private static final String XML_FILE_PATH = "/lookup.xml";

    // TODO 1: Load and parse lookup.xml to get BO class and method based on api_code
    public static Map<String, String> getBoMapping(String apiCode) {
        Map<String, String> boMapping = new HashMap<>();
        try {
            InputStream xmlStream = LookupXmlLoader.class.getResourceAsStream(XML_FILE_PATH);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlStream);

            NodeList apiNodes = doc.getElementsByTagName("api");
            for (int i = 0; i < apiNodes.getLength(); i++) {
                Element apiElement = (Element) apiNodes.item(i);
                String code = apiElement.getAttribute("api_code");
                if (code.equals(apiCode)) {
                    Element boElement = (Element) apiElement.getElementsByTagName("bo").item(0);
                    if (boElement != null) {
                        String className = boElement.getAttribute("class");
                        String methodName = boElement.getAttribute("method");
                        boMapping.put("className", className);
                        boMapping.put("methodName", methodName);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return boMapping;
    }

    // TODO 2: Invoke the BO method dynamically
    public static void invokeBoMethod(String apiCode, Map<String, Object> params) throws Exception {
        Map<String, String> boMapping = getBoMapping(apiCode);
        if (boMapping.isEmpty()) {
            throw new Exception("BO mapping not found for api_code: " + apiCode);
        }

        String className = boMapping.get("className");
        String methodName = boMapping.get("methodName");

        Class<?> clazz = Class.forName(className);
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method method = clazz.getMethod(methodName, Map.class);
        method.invoke(instance, params);
    }
}
