/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines an expression and its answer.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = Computation.class)
@JsonTypeName("Computation")
public class Computation extends Answer {
    /**
     * The math or conversion expression. If the query contains a request to
     * convert units of measure (for example, meters to feet), this field
     * contains the from units and value contains the to units. If the query
     * contains a mathematical expression such as 2+2, this field contains the
     * expression and value contains the answer. Note that mathematical
     * expressions may be normalized. For example, if the query was
     * sqrt(4^2+8^2), the normalized expression may be sqrt((4^2)+(8^2)). If
     * the user's query is a math question and the textDecorations query
     * parameter is set to true, the expression string may include formatting
     * markers. For example, if the user's query is log(2), the normalized
     * expression includes the subscript markers. For more information, see Hit
     * Highlighting.
     */
    @JsonProperty(value = "expression", required = true)
    private String expression;

    /**
     * The expression's answer.
     */
    @JsonProperty(value = "value", required = true)
    private String value;

    /**
     * Get the expression value.
     *
     * @return the expression value
     */
    public String expression() {
        return this.expression;
    }

    /**
     * Set the expression value.
     *
     * @param expression the expression value to set
     * @return the Computation object itself.
     */
    public Computation withExpression(String expression) {
        this.expression = expression;
        return this;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the Computation object itself.
     */
    public Computation withValue(String value) {
        this.value = value;
        return this;
    }

}
