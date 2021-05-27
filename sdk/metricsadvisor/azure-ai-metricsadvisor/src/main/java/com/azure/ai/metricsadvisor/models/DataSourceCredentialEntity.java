// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

/**
 * The base data source credential type.
 */
public abstract class DataSourceCredentialEntity {
    /**
     * Gets the credential id.
     *
     * @return The credential id.
     */
    public abstract String getId();
    /**
     * Gets the credential name.
     *
     * @return The credential name.
     */
    public abstract String getName();
    /**
     * Gets the credential description.
     *
     * @return The credential description.
     */
    public abstract String getDescription();
}
