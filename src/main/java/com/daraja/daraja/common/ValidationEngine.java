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

package com.daraja.daraja.common;

import com.daraja.daraja.common.CommonValidation;
import com.daraja.daraja.common.DataTypeValidation;
import com.daraja.daraja.service.DatabaseService;
import com.daraja.daraja.utility.ErrorHandlingUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidationEngine {
    private static DatabaseService dbservice;
    private static ErrorHandlingUtility errorUtil = ErrorHandlingUtility.getInstance();

    private ValidationEngine() {
    }

    public static DatabaseService getDatabaseService() {
        if (dbservice == null) {
            dbservice = new DatabaseService();
        }
        return dbservice;
    }

    public void validate(Map<String, Object> requestParams) throws Exception {
        for (Map.Entry<String, Object> entry : requestParams.entrySet()) {
            String paramName = entry.getKey();
            Object paramValue = entry.getValue();

            List<Map<String, Object>> rules = fetchValidationRules(paramName);
            for (Map<String, Object> rule : rules) {
                String validationType = (String) rule.get("validation_type");
                String validationRule = (String) rule.get("validation_rule");

                if ("business".equals(validationType)) {
                    executeBusinessValidation(validationRule, paramValue);
                } else if ("data_type".equals(validationType)) {
                    executeDataTypeValidation(validationRule, paramValue, rule);
                }
            }
        }
    }

    private List<Map<String, Object>> fetchValidationRules(String parameterName) throws Exception {
        String _query = "SELECT * FROM ValidationRules WHERE parameter_name = ?";
        List<Map<String, Object>> results = getDatabaseService().executeQuery(_query, Collections.singletonList(parameterName));
        return results;
    }

    private void executeBusinessValidation(String rule, Object value) {
        switch (rule) {
            //TODO These need to be implementated for business defined in table
//            case "check_balance":
//                if (!CommonValidation.checkBalance((double) value)) {
//                    throw new RuntimeException("Balance check failed.");
//                }
//                break;
//            case "check_date":
//                if (!CommonValidation.checkDate((String) value)) {
//                    throw new RuntimeException("Date check failed.");
//                }
//                break;
//            case "update_balance":
//                // Assume some business logic for updating balance
//                break;
//            default:
//                break;
        }
    }

    private void executeDataTypeValidation(String rule, Object value, Map<String, Object> ruleDetails) {
       // switch (rule) {

        //TODO Datatype validation to be implementated based on table defination
//            case "is_not_negative":
//                if (!DataTypeValidation.isNotNegative((double) value)) {
//                    throw new RuntimeException("Value cannot be negative.");
//                }
//                break;
//            case "check_length":
//                int minLength = Integer.parseInt((String) ruleDetails.get("min_length"));
//                int maxLength = Integer.parseInt((String) ruleDetails.get("max_length"));
//                if (!DataTypeValidation.checkLength((String) value, minLength, maxLength)) {
//                    throw new RuntimeException("Length check failed.");
//                }
//                break;
//            default:
//                break;
//        }
    }
}
