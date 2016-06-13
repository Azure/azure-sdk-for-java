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
 * The parameters to provide for the account.
 */
@JsonFlatten
public class StorageAccountUpdateParametersInner {
    /**
     * Gets or sets the sku type. Note that sku cannot be updated to
     * StandardZRS or ProvisionedLRS, nor can accounts of that sku type be
     * updated to any other value.
     */
    private Sku sku;

    /**
     * Gets or sets a list of key value pairs that describe the resource.
     * These tags can be used in viewing and grouping this resource (across
     * resource groups). A maximum of 15 tags can be provided for a resource.
     * Each tag must have a key no greater than 128 characters and value no
     * greater than 256 characters.
     */
    private Map<String, String> tags;

    /**
     * User domain assigned to the storage account. Name is the CNAME source.
     * Only one custom domain is supported per storage account at this time.
     * To clear the existing custom domain, use an empty string for the
     * custom domain name property.
     */
    @JsonProperty(value = "properties.customDomain")
    private CustomDomain customDomain;

    /**
     * Provides the encryption settings on the account. The default setting is
     * unencrypted.
     */
    @JsonProperty(value = "properties.encryption")
    private Encryption encryption;

    /**
     * The access tier used for billing. Access tier cannot be changed more
     * than once every 7 days (168 hours). Access tier cannot be set for
     * StandardLRS, StandardGRS, StandardRAGRS, or PremiumLRS account types.
     * Possible values include: 'Hot', 'Cool'.
     */
    @JsonProperty(value = "properties.accessTier")
    private AccessTier accessTier;

    /**
     * Get the sku value.
     *
     * @return the sku value
     */
    public Sku sku() {
        return this.sku;
    }

    /**
     * Set the sku value.
     *
     * @param sku the sku value to set
     * @return the StorageAccountUpdateParametersInner object itself.
     */
    public StorageAccountUpdateParametersInner withSku(Sku sku) {
        this.sku = sku;
        return this;
    }

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

    /**
     * Get the encryption value.
     *
     * @return the encryption value
     */
    public Encryption encryption() {
        return this.encryption;
    }

    /**
     * Set the encryption value.
     *
     * @param encryption the encryption value to set
     * @return the StorageAccountUpdateParametersInner object itself.
     */
    public StorageAccountUpdateParametersInner withEncryption(Encryption encryption) {
        this.encryption = encryption;
        return this;
    }

    /**
     * Get the accessTier value.
     *
     * @return the accessTier value
     */
    public AccessTier accessTier() {
        return this.accessTier;
    }

    /**
     * Set the accessTier value.
     *
     * @param accessTier the accessTier value to set
     * @return the StorageAccountUpdateParametersInner object itself.
     */
    public StorageAccountUpdateParametersInner withAccessTier(AccessTier accessTier) {
        this.accessTier = accessTier;
        return this;
    }

}
