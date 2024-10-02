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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Objects;

public class BusinessValidation {

    public enum ValidationType {
        COMPARISON,  // e.g., value > 100
        REGEX,       // e.g., email must match pattern
        DB_LOOKUP,   // e.g., must exist in master table
        CROSS_FIELD  // e.g., fieldA depends on fieldB's value
    }


    private String validationCode;
    private String validationDescription;
    private String validationRule;
    private String requestParam;
    private String api_code;
    private List<BusinessValidationRule> validationRules;
    private static DatabaseService dbservice;
    private static ErrorHandlingUtility errorUtil = ErrorHandlingUtility.getInstance();
    private static final BusinessValidation instance = new BusinessValidation();


    public static BusinessValidation getInstance(){
        return instance;
    }

    public static class BusinessValidationRule {
        private String paramName;
        private ValidationType validationType;
        private String operator;   // For comparison or complex rules
        private String criteria;   // Value or pattern to check against
        private String validationRule;

        public BusinessValidationRule(String paramName, ValidationType validationType, String operator, String criteria,String validationRule) {
            this.paramName = paramName;
            this.validationType = validationType;
            this.operator = operator;
            this.criteria = criteria;
            this.validationRule = validationRule;
        }

        public String getParamName() {
            return paramName;
        }

        public ValidationType getValidationType() {
            return validationType;
        }

        public String getOperator() {
            return operator;
        }

        public String getCriteria() {
            return criteria;
        }

        public String getValidationRule() {
            return validationRule;
        }
    }



    public BusinessValidation(String validationCode, String validationDescription,String ValidationRule,
                              String requestParam, String api_code) {
        this.validationCode = validationCode;
        this.validationDescription = validationDescription;
        this.validationRule = ValidationRule;
        this.requestParam = requestParam;
        this.api_code = api_code;
    }

    public BusinessValidation() {
        this.validationRules = new ArrayList<>(); // Initialize the list of rules
    }

    public BusinessValidation(String validationCode, String validationDescription, List<BusinessValidationRule> validationRules, String requestParam, String api_code) {
        this.validationCode = validationCode;
        this.validationDescription = validationDescription;
        this.validationRules = validationRules; // Initialize with provided rules
        this.requestParam = requestParam;
        this.api_code = api_code;
    }


    public List<BusinessValidationRule> getValidationRules() {
        return validationRules;
    }

    public static DatabaseService getDatabaseService() {
        if (dbservice == null) {
            dbservice = new DatabaseService();
        }
        return dbservice;
    }

    public void setValidationCode(String validationCode) {
        this.validationCode = validationCode;
    }

    public void setValidationDescription(String validationDescription) {
        this.validationDescription = validationDescription;
    }

    public void setValidationRule(String validationRule) {
        this.validationRule = validationRule;
    }

    public void setRequestParam(String requestParam) {
        this.requestParam = requestParam;
    }

    public void setApiCode(String apiCode) {
        this.api_code = apiCode;
    }

    /**
     * Executes the business validation based on the given parameter value.
     *
     * @param paramValue The value to validate against the rules.
     * @throws ValidationException If validation fails or an error occurs.
     */
    public void executeBusinessValidation(Object paramValue) throws ValidationException {
        for (BusinessValidationRule rule : validationRules) {
            if (rule == null || rule.getCriteria() == null || rule.getCriteria().isEmpty()) {
                errorUtil.setErrorByCode("ERR10017");  // Set error if validation rule is empty or null.
                continue;
            }

            if (errorUtil.checkStatus()) {
                return;  // Exit if an error is already present.
            }

            boolean isValid = applyBusinessRule(rule, paramValue);

            if (!isValid) {
                errorUtil.setError(
                        errorUtil.getErrorByCode("ERR10018") + "-" + rule.getParamName() + " - " + validationDescription
                );
            }
        }
    }


    /**
     * Applies the business rule to the given parameter value.
     *
     * @param validationRule The business validation rule to apply.
     * @param paramValue     The value to validate.
     * @return true if valid, false otherwise.
     */
    private boolean applyBusinessRule(BusinessValidationRule validationRule, Object paramValue) {
        switch (validationRule.getValidationType()) {
            case COMPARISON:
                return handleComparisonRule(validationRule, paramValue);
            case REGEX:
                return handleRegexRule(validationRule, paramValue);
            case DB_LOOKUP:
                return handleDbLookupRule(validationRule, paramValue);
            case CROSS_FIELD:
                return handleCrossFieldRule(validationRule, paramValue); // Placeholder for complex logic
            default:
                throw new IllegalArgumentException("Unknown validation type: " + validationRule.getValidationType());
        }
    }

    // Handle comparison-based rules (>, <, =)
    private boolean handleComparisonRule(BusinessValidationRule validationRule, Object paramValue) {
        try {
            Double threshold = Double.valueOf(validationRule.getCriteria());
            if (paramValue instanceof Number) {
                Double value = ((Number) paramValue).doubleValue();
                switch (validationRule.getOperator()) {
                    case ">":
                        return value > threshold;
                    case "<":
                        return value < threshold;
                    case "=":
                    case "==":
                        return Objects.equals(value, threshold);
                    case ">=":
                        return value >= threshold;
                    case "<=":
                        return value <= threshold;
                    default:
                        throw new IllegalArgumentException("Unsupported operator: " + validationRule.getOperator());
                }
            } else {
                throw new IllegalArgumentException("Parameter value must be a number for comparison validation.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid comparison threshold: " + validationRule.getCriteria(), e);
        }
    }

    // Handle regex-based validation
    private boolean handleRegexRule(BusinessValidationRule validationRule, Object paramValue) {
        if (paramValue instanceof String) {
            String value = (String) paramValue;
            Pattern pattern = Pattern.compile(validationRule.getCriteria());
            Matcher matcher = pattern.matcher(value);
            return matcher.matches();
        } else {
            throw new IllegalArgumentException("Parameter value must be a string for regex validation.");
        }
    }

    // Handle database lookup-based validation
    private boolean handleDbLookupRule(BusinessValidationRule validationRule, Object paramValue) {
        // Assuming the validation rule format: "db_lookup|user_software_usage|number_of_requests>user_id"
        String[] parts = validationRule.getValidationRule().split("\\|");

        if (parts.length < 5 || !parts[0].equals("db_lookup")) {
            throw new IllegalArgumentException("Invalid validation rule format.");
        }

        String tableName = parts[1];
        String columnName = parts[2];
        String userIdColumn = parts[3];

        // Create whereConditions map
        Map<String, Object> whereConditions = new HashMap<>();
        whereConditions.put(userIdColumn, paramValue); // e.g., user_id = paramValue

        // Get valid values from the database
        List<Map<String, Object>> results = getDatabaseService().getValidValues(
                Arrays.asList(columnName), // List of columns to select
                tableName,                // Table name
                whereConditions           // Where conditions
        );

        // Check if any results were returned
        return !results.isEmpty();
    }

    // Placeholder for cross-field validation
    private boolean handleCrossFieldRule(BusinessValidationRule validationRule, Object paramValue) {
        // You can implement more complex cross-field logic here
        return true;
    }
}
