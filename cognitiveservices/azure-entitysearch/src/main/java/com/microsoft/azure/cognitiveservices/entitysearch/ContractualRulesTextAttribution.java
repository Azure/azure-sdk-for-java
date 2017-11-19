/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.entitysearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a contractual rule for text attribution.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("ContractualRules/TextAttribution")
public class ContractualRulesTextAttribution extends ContractualRulesAttribution {
    /**
     * The attribution text. Text attribution applies to the entity as a whole
     * and should be displayed immediately following the entity presentation.
     * If there are multiple text or link attribution rules that do not specify
     * a target, you should concatenate them and display them using a "Data
     * from:" label.
     */
    @JsonProperty(value = "text", required = true)
    private String text;

    /**
     * Indicates whether this provider's attribution is optional.
     */
    @JsonProperty(value = "optionalForListDisplay", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean optionalForListDisplay;

    /**
     * Get the text value.
     *
     * @return the text value
     */
    public String text() {
        return this.text;
    }

    /**
     * Set the text value.
     *
     * @param text the text value to set
     * @return the ContractualRulesTextAttribution object itself.
     */
    public ContractualRulesTextAttribution withText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get the optionalForListDisplay value.
     *
     * @return the optionalForListDisplay value
     */
    public Boolean optionalForListDisplay() {
        return this.optionalForListDisplay;
    }

}
