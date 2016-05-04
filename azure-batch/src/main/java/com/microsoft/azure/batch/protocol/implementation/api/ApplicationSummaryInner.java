/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains information about an application in an Azure Batch account.
 */
public class ApplicationSummaryInner {
    /**
     * Gets or sets a string that uniquely identifies the application within
     * the account.
     */
    @JsonProperty(required = true)
    private String id;

    /**
     * Gets or sets the display name for the application.
     */
    @JsonProperty(required = true)
    private String displayName;

    /**
     * The versions property.
     */
    @JsonProperty(required = true)
    private List<String> versions;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the ApplicationSummaryInner object itself.
     */
    public ApplicationSummaryInner setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set the displayName value.
     *
     * @param displayName the displayName value to set
     * @return the ApplicationSummaryInner object itself.
     */
    public ApplicationSummaryInner setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the versions value.
     *
     * @return the versions value
     */
    public List<String> versions() {
        return this.versions;
    }

    /**
     * Set the versions value.
     *
     * @param versions the versions value to set
     * @return the ApplicationSummaryInner object itself.
     */
    public ApplicationSummaryInner setVersions(List<String> versions) {
        this.versions = versions;
        return this;
    }

}
