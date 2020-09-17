// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.models;

/**
 * Defines an overriding value that overrides values passed in for RegistryFileTaskStep, RegistryFileTaskRunRequest,
 * RegistryEncodedTaskStep, and RegistryEncodedTaskRunRequest.
 */
public class OverridingValue {
    private final String value;
    private final boolean isSecret;

    /**
     * Constructor that defines an OverridingValue.
     *
     * @param value the value of the overriding value.
     * @param isSecret whether the overriding value will be secret.
     */
    public OverridingValue(String value, boolean isSecret) {
        this.value = value;
        this.isSecret = isSecret;
    }

    /** @return the value of the overriding value. */
    public String value() {
        return this.value;
    }

    /** @return whether the overriding value is secret or not. */
    public boolean isSecret() {
        return this.isSecret;
    }
}
