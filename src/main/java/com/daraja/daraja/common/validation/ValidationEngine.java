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
package com.daraja.daraja.common.validation;

import com.daraja.daraja.service.DatabaseService;
import com.daraja.daraja.utility.ErrorHandlingUtility;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ValidationEngine {
    private static DatabaseService dbservice;
    private static ErrorHandlingUtility errorUtil = ErrorHandlingUtility.getInstance();
    private static final ValidationEngine instance = new ValidationEngine();
    private  static BusinessValidation businessUtil = BusinessValidation.getInstance();

    public ValidationEngine() {
    }

    public static DatabaseService getDatabaseService() {
        if (dbservice == null) {
            dbservice = new DatabaseService();
        }
        return dbservice;
    }

    public static ValidationEngine getInstance() {
        return instance;
    }

    public void validate(String apiCode, Map<String, Object> requestParams) throws ValidationException {
        errorUtil.clearError();
        for (Map.Entry<String, Object> entry : requestParams.entrySet()) {
            String paramName = entry.getKey();
            Object paramValue = entry.getValue();

            // Fetch validation rules based on api_code and parameter_name
            boolean validationPassed = fetchValidationRules(apiCode, paramName, paramValue);
            if (!validationPassed) {
                errorUtil.setError(errorUtil.getErrorByCode("ERR10015") +" " +paramName);
                return;
                //throw new ValidationException("Validation failed for parameter: " + paramName);
            }
        }
    }

    private boolean fetchValidationRules(String apiCode, String parameterName, Object paramValue) throws ValidationException {
        // Fetch validation rules from the database based on api_code and parameter_name
        errorUtil.clearError();

        String query = "SELECT * FROM ValidationRules WHERE api_code = ? AND parameter_name = ?";
        List<Map<String, Object>> rules = getDatabaseService().executeQuery(query,
                Arrays.asList(apiCode, parameterName));

        // Iterate through the rules and execute validations
        for (Map<String, Object> rule : rules) {
            String validationRule = (String) rule.get("validation_rule");
            String businessValidationId = rule.get("id").toString();

            // Retrieve codes from the rule
            String businessValidationCode = (String) rule.get("business_validation_code");
            String dataValidationCode = (String) rule.get("data_validation_code");
            String commonValidationCode = (String) rule.get("common_validation_code");

            // Check and execute appropriate validation
            if (businessValidationCode != null && !businessValidationCode.isEmpty()) {
                // Fetch business validation description and execute the validation
                 fetchBusinessValidation(businessValidationId);
                if(errorUtil.checkStatus())
                    return false;

                businessUtil.executeBusinessValidation(paramValue);
            } else if (dataValidationCode != null && !dataValidationCode.isEmpty()) {
                executeDataTypeValidation(dataValidationCode, paramValue, rule);
            } else if (commonValidationCode != null && !commonValidationCode.isEmpty()) {
                executeCommonValidation(commonValidationCode, paramValue);
            } else {
                errorUtil.setError(errorUtil.getErrorByCode("ERR10015") +" " +parameterName);
                return false;
                //throw new ValidationException("No valid validation type found for parameter: " + parameterName);
            }
        }
        return true; // Return true if all validations pass
    }

    private void fetchBusinessValidation(String validationCode) throws ValidationException {
        errorUtil.clearError();
        String query = "SELECT * FROM BusinessValidation WHERE id = ?";
        List<Map<String, Object>> validations = getDatabaseService().executeQuery(query, Arrays.asList(Integer.parseInt(validationCode)));

        if (validations.isEmpty()) {
            errorUtil.setError(errorUtil.getErrorByCode("ERR10016") +"->"+validationCode);
            //throw new ValidationException("No business validation found for code: " + validationCode);
        }
        if(!errorUtil.checkStatus()) {
            Map<String, Object> validation = validations.get(0);
            if(!validation.get("api_code").toString().isEmpty())
                businessUtil.setApiCode(validation.get("api_code").toString());
            if(!validation.get("validation_code").toString().isEmpty())
                businessUtil.setValidationCode(validation.get("validation_code").toString());
            if(!validation.get("validation_description").toString().isEmpty())
                businessUtil.setValidationDescription(validation.get("validation_description").toString());
            if(!validation.get("request_param").toString().isEmpty())
                businessUtil.setRequestParam(validation.get("request_param").toString());
            if (!validation.get("validation_rule").toString().isEmpty()) {
                String validationRule = validation.get("validation_rule").toString();
                businessUtil.setValidationRule(validationRule);

                // Here, parse the validation rule string and create BusinessValidationRule instances
                // Assuming the format is: "db_lookup|user_software_usage|number_of_requests|user_id|=1000"
                String[] parts = validationRule.split("\\|");
                if (parts.length >= 5) {
                    // Constructing BusinessValidationRule from parts
                    String type = parts[0]; // For example, db_lookup
                    String tableName = parts[1];
                    String columnName = parts[2];
                    String userIdColumn = parts[3];
                    String criteria = parts[4];

                    // Example of creating the BusinessValidationRule
                    if (type.equals("db_lookup")) {
                        BusinessValidation.BusinessValidationRule rule = new BusinessValidation.BusinessValidationRule(
                                userIdColumn,
                                BusinessValidation.ValidationType.DB_LOOKUP,
                                "=",
                                criteria,
                                validationRule
                        );
                        businessUtil.getValidationRules().add(rule); // Assuming there's a getter for validationRules
                    }
                }
            }
        }
    }

    private void executeDataTypeValidation(String rule, Object value, Map<String, Object> ruleDetails) throws ValidationException {
        //TODO these one we require userstory and roadmap
        // Implement data type validation logic based on the rule
//        switch (rule) {
//            case "is_not_negative":
//                if (!DataTypeValidation.isNotNegative((double) value)) {
//                    throw new ValidationException("Value cannot be negative.");
//                }
//                break;
//            case "check_length":
//                int minLength = Integer.parseInt((String) ruleDetails.get("min_length"));
//                int maxLength = Integer.parseInt((String) ruleDetails.get("max_length"));
//                if (!DataTypeValidation.checkLength((String) value, minLength, maxLength)) {
//                    throw new ValidationException("Length check failed.");
//                }
//                break;
//            // Add more rules as needed
//            default:
//                throw new ValidationException("Unknown data type validation rule: " + rule);
//        }
    }

    private void executeCommonValidation(String rule, Object value) throws ValidationException {
        // Implement common validation logic based on the rule
        switch (rule) {
            // Example implementations
            // Add common validation rules as needed
            default:
                throw new ValidationException("Unknown common validation rule: " + rule);
        }
    }
}
