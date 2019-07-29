// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

/**
 * Represents a set of access conditions to be used for operations against the Azure Cosmos DB database service.
 */
public final class AccessCondition {

    private AccessConditionType type = AccessConditionType.IF_MATCH;
    private String condition;

    /**
     * Gets the condition type.
     *
     * @return the condition type.
     */
    public AccessConditionType type() {
        return this.type;
    }

    /**
     * Sets the condition type.
     *
     * @param type the condition type to use.
     * @return the Access Condition
     */
    public AccessCondition type(AccessConditionType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets the value of the condition - for AccessConditionType IfMatchs and IfNotMatch, this is the ETag that has to
     * be compared to.
     *
     * @return the condition.
     */
    public String condition() {
        return this.condition;
    }

    /**
     * Sets the value of the condition - for AccessConditionType IfMatchs and IfNotMatch, this is the ETag that has to
     * be compared to.
     *
     * @param condition the condition to use.
     * @return the Access Condition
     */
    public AccessCondition condition(String condition) {
        this.condition = condition;
        return this;
    }
}
