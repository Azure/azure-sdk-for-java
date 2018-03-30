/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;

/**
 * Represents a set of access conditions to be used for operations against the Azure Cosmos DB database service.
 */
public final class AccessCondition {

    private AccessConditionType type = AccessConditionType.IfMatch;
    private String condition;

    /**
     * Gets the condition type.
     *
     * @return the condition type.
     */
    public AccessConditionType getType() {
        return this.type;
    }

    /**
     * Sets the condition type.
     *
     * @param type the condition type to use.
     */
    public void setType(AccessConditionType type) {
        this.type = type;
    }

    /**
     * Gets the value of the condition - for AccessConditionType IfMatchs and IfNotMatch, this is the ETag that has to
     * be compared to.
     *
     * @return the condition.
     */
    public String getCondition() {
        return this.condition;
    }

    /**
     * Sets the value of the condition - for AccessConditionType IfMatchs and IfNotMatch, this is the ETag that has to
     * be compared to.
     *
     * @param condition the condition to use.
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }
}
