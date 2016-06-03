/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DataLakeAnalytics DataLakeAnalyticsAccount information.
 */
public class DataLakeAnalyticsCatalogSecretCreateOrUpdateParametersInner {
    /**
     * Gets or sets the password for the secret to pass in.
     */
    @JsonProperty(required = true)
    private String password;

    /**
     * Gets or sets the URI identifier for the secret in the format
     * &lt;hostname&gt;:&lt;port&gt;.
     */
    private String uri;

    /**
     * Get the password value.
     *
     * @return the password value
     */
    public String password() {
        return this.password;
    }

    /**
     * Set the password value.
     *
     * @param password the password value to set
     * @return the DataLakeAnalyticsCatalogSecretCreateOrUpdateParametersInner object itself.
     */
    public DataLakeAnalyticsCatalogSecretCreateOrUpdateParametersInner withPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get the uri value.
     *
     * @return the uri value
     */
    public String uri() {
        return this.uri;
    }

    /**
     * Set the uri value.
     *
     * @param uri the uri value to set
     * @return the DataLakeAnalyticsCatalogSecretCreateOrUpdateParametersInner object itself.
     */
    public DataLakeAnalyticsCatalogSecretCreateOrUpdateParametersInner withUri(String uri) {
        this.uri = uri;
        return this;
    }

}
