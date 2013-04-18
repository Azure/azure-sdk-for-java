/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.table.models;

/**
 * Represents a table query filter expression consisting of a unary logical operator and a boolean filter expression.
 * Use the static factory methods in the {@link Filter} class to create <code>UnaryFilter</code> instances, rather
 * than constructing them directly.
 */
public class UnaryFilter extends Filter {
    private final String operator;
    private final Filter operand;

    /**
     * Creates a <code>UnaryFilter</code> expression from a unary logical operator and a boolean {@link Filter}.
     * <p>
     * Use the static factory methods in the {@link Filter} class to create <code>UnaryFilter</code> instances, rather
     * than constructing them directly.
     * 
     * @param operator
     *            A {@link String} containing the unary logical operator to use in the expression.
     * @param operand
     *            The boolean {@link Filter} to use as the operand of the expression.
     */
    public UnaryFilter(String operator, Filter operand) {
        this.operator = operator;
        this.operand = operand;
    }

    /**
     * Gets the unary logical operator to use in the <code>UnaryFilter</code> expression.
     * 
     * @return
     *         A {@link String} containing the unary logical operator to use in the expression.
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Gets the boolean {@link Filter} to use as the operand of the <code>UnaryFilter</code> expression.
     * 
     * @return
     *         A {@link Filter} to use as the operand of the expression.
     */
    public Filter getOperand() {
        return operand;
    }

}
