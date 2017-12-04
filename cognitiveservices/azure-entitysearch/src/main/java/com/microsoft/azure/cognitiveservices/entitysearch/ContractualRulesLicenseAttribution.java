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
 * Defines a contractual rule for license attribution.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("ContractualRules/LicenseAttribution")
public class ContractualRulesLicenseAttribution extends ContractualRulesAttribution {
    /**
     * The license under which the content may be used.
     */
    @JsonProperty(value = "license", access = JsonProperty.Access.WRITE_ONLY)
    private License license;

    /**
     * The license to display next to the targeted field.
     */
    @JsonProperty(value = "licenseNotice", access = JsonProperty.Access.WRITE_ONLY)
    private String licenseNotice;

    /**
     * Get the license value.
     *
     * @return the license value
     */
    public License license() {
        return this.license;
    }

    /**
     * Get the licenseNotice value.
     *
     * @return the licenseNotice value
     */
    public String licenseNotice() {
        return this.licenseNotice;
    }

}
