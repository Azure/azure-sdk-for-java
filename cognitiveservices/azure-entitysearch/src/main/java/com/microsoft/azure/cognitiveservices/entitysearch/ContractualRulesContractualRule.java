/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.entitysearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonSubTypes;

/**
 * The ContractualRulesContractualRule model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("ContractualRules/ContractualRule")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "ContractualRules/Attribution", value = ContractualRulesAttribution.class)
})
public class ContractualRulesContractualRule {
    /**
     * The name of the field that the rule applies to.
     */
    @JsonProperty(value = "targetPropertyName", access = JsonProperty.Access.WRITE_ONLY)
    private String targetPropertyName;

    /**
     * Get the targetPropertyName value.
     *
     * @return the targetPropertyName value
     */
    public String targetPropertyName() {
        return this.targetPropertyName;
    }

}
