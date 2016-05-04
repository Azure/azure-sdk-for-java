/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;


/**
 * Information about the most recent job to run under the job schedule.
 */
public class RecentJob {
    /**
     * Gets or sets the id of the job.
     */
    private String id;

    /**
     * Gets or sets the URL of the job.
     */
    private String url;

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
     * @return the RecentJob object itself.
     */
    public RecentJob setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the url value.
     *
     * @return the url value
     */
    public String url() {
        return this.url;
    }

    /**
     * Set the url value.
     *
     * @param url the url value to set
     * @return the RecentJob object itself.
     */
    public RecentJob setUrl(String url) {
        this.url = url;
        return this;
    }

}
