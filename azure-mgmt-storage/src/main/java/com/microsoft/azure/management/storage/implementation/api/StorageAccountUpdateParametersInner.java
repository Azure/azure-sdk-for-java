/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation.api;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

/**
 * The parameters to update on the account.
 */
@JsonFlatten
public class StorageAccountUpdateParametersInner {
    /**
     * Resource tags.
     */
    private Map<String, String> tags;

    /**
     * Gets or sets the account type. Note that StandardZRS and PremiumLRS
     * accounts cannot be changed to other account types, and other account
     * types cannot be changed to StandardZRS or PremiumLRS. Possible values
     * include: 'Standard_LRS', 'Standard_ZRS', 'Standard_GRS',
     * 'Standard_RAGRS', 'Premium_LRS'.
     */
    @JsonProperty(value = "properties.accountType")
    private AccountType accountType;

    /**
     * User domain assigned to the storage account. Name is the CNAME source.
     * Only one custom domain is supported per storage account at this time.
     * To clear the existing custom domain, use an empty string for the
     * custom domain name property.
     */
    @JsonProperty(value = "properties.customDomain")
    private CustomDomain customDomain;

    /**
     * Get the tags value.
     *
     * @return the tags value
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags value to set
     * @return the StorageAccountUpdateParametersInner object itself.
     */
    public StorageAccountUpdateParametersInner withTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the accountType value.
     *
     * @return the accountType value
     */
    public AccountType accountType() {
        return this.accountType;
    }

    /**
     * Set the accountType value.
     *
     * @param accountType the accountType value to set
     * @return the StorageAccountUpdateParametersInner object itself.
     */
    public StorageAccountUpdateParametersInner withAccountType(AccountType accountType) {
        this.accountType = accountType;
        return this;
    }

    /**
     * Get the customDomain value.
     *
     * @return the customDomain value
     */
    public CustomDomain customDomain() {
        return this.customDomain;
    }

    /**
     * Set the customDomain value.
     *
     * @param customDomain the customDomain value to set
     * @return the StorageAccountUpdateParametersInner object itself.
     */
    public StorageAccountUpdateParametersInner withCustomDomain(CustomDomain customDomain) {
        this.customDomain = customDomain;
        return this;
    }

}
