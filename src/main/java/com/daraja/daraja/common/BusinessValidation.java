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

import java.util.Objects;

public class BusinessValidation {
    private String validationCode;
    private String validationDescription;

    public BusinessValidation(String validationCode, String validationDescription) {
        this.validationCode = validationCode;
        this.validationDescription = validationDescription;
    }

    /**
     * Executes the business validation based on the given rule and parameter value.
     *
     * @param validationRule The business validation rule to apply.
     * @param paramValue The value to validate against the rule.
     * @throws ValidationException If validation fails or an error occurs.
     */
    public void executeBusinessValidation(BusinessValidation validationRule, Object paramValue) throws ValidationException {
        if (validationRule.validationCode == null || validationRule.validationCode.isEmpty()) {
            throw new IllegalArgumentException("Validation rule cannot be null or empty.");
        }

        // Here you could parse the validation rule and apply the necessary business logic.
        // For demonstration, we will assume the validationRule is a simple condition.

        boolean isValid = applyBusinessRule(validationRule, paramValue);

        if (!isValid) {
            throw new ValidationException("Business validation failed for rule: " + validationRule + " - " + validationDescription);
        }
    }

    /**
     * Applies the business rule to the given parameter value.
     *
     * @param validationRule The business validation rule to apply.
     * @param paramValue The value to validate.
     * @return true if valid, false otherwise.
     */
    private boolean applyBusinessRule(BusinessValidation validationRule, Object paramValue) {
        // Implement your business logic here.
        // For example, if the validationRule is something like "value > 100"
        // You would parse that and compare it against paramValue.

        // For the sake of this example, let's say the rule is simply a numeric check:
        try {
            // Assuming the rule is a simple threshold check, e.g., "value > 100"
            String[] parts = validationRule.validationCode.split(" ");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid validation rule format.");
            }

            String operator = parts[1];
            Double threshold = Double.valueOf(parts[2]);

            if (paramValue instanceof Number) {
                Double value = ((Number) paramValue).doubleValue();

                switch (operator) {
                    case ">":
                        return value > threshold;
                    case "<":
                        return value < threshold;
                    case "=":
                        return Objects.equals(value, threshold);
                    // Add more operators as needed
                    default:
                        throw new IllegalArgumentException("Unsupported operator: " + operator);
                }
            } else {
                throw new IllegalArgumentException("Parameter value must be a number for this validation rule.");
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Error processing validation rule: " + validationRule, e);
        }
    }
}
