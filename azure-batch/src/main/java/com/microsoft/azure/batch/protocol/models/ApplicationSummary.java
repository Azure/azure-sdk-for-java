/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains information about an application in an Azure Batch account.
 */
public class ApplicationSummary {
    /**
     * A string that uniquely identifies the application within the account.
     */
    @JsonProperty(required = true)
    private String id;

    /**
     * The display name for the application.
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
     * @return the ApplicationSummary object itself.
     */
    public ApplicationSummary withId(String id) {
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
     * @return the ApplicationSummary object itself.
     */
    public ApplicationSummary withDisplayName(String displayName) {
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
     * @return the ApplicationSummary object itself.
     */
    public ApplicationSummary withVersions(List<String> versions) {
        this.versions = versions;
        return this;
    }

}
