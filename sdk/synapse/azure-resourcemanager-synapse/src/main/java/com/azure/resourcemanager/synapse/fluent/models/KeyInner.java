// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.synapse.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.JsonFlatten;
import com.azure.core.management.ProxyResource;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/** A workspace key. */
@JsonFlatten
@Fluent
public class KeyInner extends ProxyResource {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(KeyInner.class);

    /*
     * Used to activate the workspace after a customer managed key is provided.
     */
    @JsonProperty(value = "properties.isActiveCMK")
    private Boolean isActiveCmk;

    /*
     * The Key Vault Url of the workspace key.
     */
    @JsonProperty(value = "properties.keyVaultUrl")
    private String keyVaultUrl;

    /**
     * Get the isActiveCmk property: Used to activate the workspace after a customer managed key is provided.
     *
     * @return the isActiveCmk value.
     */
    public Boolean isActiveCmk() {
        return this.isActiveCmk;
    }

    /**
     * Set the isActiveCmk property: Used to activate the workspace after a customer managed key is provided.
     *
     * @param isActiveCmk the isActiveCmk value to set.
     * @return the KeyInner object itself.
     */
    public KeyInner withIsActiveCmk(Boolean isActiveCmk) {
        this.isActiveCmk = isActiveCmk;
        return this;
    }

    /**
     * Get the keyVaultUrl property: The Key Vault Url of the workspace key.
     *
     * @return the keyVaultUrl value.
     */
    public String keyVaultUrl() {
        return this.keyVaultUrl;
    }

    /**
     * Set the keyVaultUrl property: The Key Vault Url of the workspace key.
     *
     * @param keyVaultUrl the keyVaultUrl value to set.
     * @return the KeyInner object itself.
     */
    public KeyInner withKeyVaultUrl(String keyVaultUrl) {
        this.keyVaultUrl = keyVaultUrl;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }
}
