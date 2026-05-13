// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

/**
 * Feature flag variant definition that defines a specific variant of a feature flag.
 */
public final class FeatureFlagVariant {
    private final String name;
    private String value;
    private String contentType;
    private FeatureFlagStatusOverride statusOverride;

    /**
     * Creates an instance of FeatureFlagVariant.
     *
     * @param name the name of the variant.
     */
    public FeatureFlagVariant(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the variant.
     *
     * @return the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the value of the variant.
     *
     * @return the value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value of the variant.
     *
     * @param value the value.
     * @return the updated FeatureFlagVariant object.
     */
    public FeatureFlagVariant setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Gets the content type of the value.
     *
     * @return the content type.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Sets the content type of the value.
     *
     * @param contentType the content type.
     * @return the updated FeatureFlagVariant object.
     */
    public FeatureFlagVariant setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Gets the status override that determines if the variant should override the status of the flag.
     *
     * @return the status override.
     */
    public FeatureFlagStatusOverride getStatusOverride() {
        return this.statusOverride;
    }

    /**
     * Sets the status override that determines if the variant should override the status of the flag.
     *
     * @param statusOverride the status override.
     * @return the updated FeatureFlagVariant object.
     */
    public FeatureFlagVariant setStatusOverride(FeatureFlagStatusOverride statusOverride) {
        this.statusOverride = statusOverride;
        return this;
    }
}
