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
 * The ContractualRulesAttribution model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("ContractualRules/Attribution")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "ContractualRules/LicenseAttribution", value = ContractualRulesLicenseAttribution.class),
    @JsonSubTypes.Type(name = "ContractualRules/LinkAttribution", value = ContractualRulesLinkAttribution.class),
    @JsonSubTypes.Type(name = "ContractualRules/MediaAttribution", value = ContractualRulesMediaAttribution.class),
    @JsonSubTypes.Type(name = "ContractualRules/TextAttribution", value = ContractualRulesTextAttribution.class)
})
public class ContractualRulesAttribution extends ContractualRulesContractualRule {
    /**
     * A Boolean value that determines whether the contents of the rule must be
     * placed in close proximity to the field that the rule applies to. If
     * true, the contents must be placed in close proximity. If false, or this
     * field does not exist, the contents may be placed at the caller's
     * discretion.
     */
    @JsonProperty(value = "mustBeCloseToContent", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean mustBeCloseToContent;

    /**
     * Get the mustBeCloseToContent value.
     *
     * @return the mustBeCloseToContent value
     */
    public Boolean mustBeCloseToContent() {
        return this.mustBeCloseToContent;
    }

}
