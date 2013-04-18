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
 * Represents a table query filter expression consisting of a filter, a binary comparison operator, and a filter. Use
 * the static factory methods in the {@link Filter} class to create <code>BinaryFilter</code> instances, rather
 * than constructing them directly.
 */
public class BinaryFilter extends Filter {
    private final String operator;
    private final Filter left;
    private final Filter right;

    /**
     * Creates a <code>BinaryFilter</code> expression from a {@link Filter}, a binary comparison operator, and a
     * {@link Filter}.
     * <p>
     * Use the static factory methods in the {@link Filter} class to create <code>BinaryFilter</code> instances, rather
     * than constructing them directly.
     * 
     * @param left
     *            The {@link Filter} to use on the left hand side of the expression.
     * @param operator
     *            A {@link String} containing the comparison operator to use in the expression.
     * @param right
     *            The {@link Filter} to use on the right hand side of the expression.
     */
    public BinaryFilter(Filter left, String operator, Filter right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    /**
     * Gets the comparison operator to use in the <code>BinaryFilter</code> expression.
     * 
     * @return
     *         A {@link String} containing the comparison operator to use in the expression.
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Gets the {@link Filter} to use on the left hand side of the <code>BinaryFilter</code> expression.
     * 
     * @return
     *         A {@link Filter} to use on the left hand side of the expression.
     */
    public Filter getLeft() {
        return left;
    }

    /**
     * Gets the {@link Filter} to use on the right hand side of the <code>BinaryFilter</code> expression.
     * 
     * @return
     *         A {@link Filter} to use on the right hand side of the expression.
     */
    public Filter getRight() {
        return right;
    }

}
