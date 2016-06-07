/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Configuration of Azure web site.
 */
@JsonFlatten
public class SiteLogsConfigInner extends Resource {
    /**
     * Application logs configuration.
     */
    @JsonProperty(value = "properties.applicationLogs")
    private ApplicationLogsConfig applicationLogs;

    /**
     * Http logs configuration.
     */
    @JsonProperty(value = "properties.httpLogs")
    private HttpLogsConfig httpLogs;

    /**
     * Failed requests tracing configuration.
     */
    @JsonProperty(value = "properties.failedRequestsTracing")
    private EnabledConfig failedRequestsTracing;

    /**
     * Detailed error messages configuration.
     */
    @JsonProperty(value = "properties.detailedErrorMessages")
    private EnabledConfig detailedErrorMessages;

    /**
     * Get the applicationLogs value.
     *
     * @return the applicationLogs value
     */
    public ApplicationLogsConfig applicationLogs() {
        return this.applicationLogs;
    }

    /**
     * Set the applicationLogs value.
     *
     * @param applicationLogs the applicationLogs value to set
     * @return the SiteLogsConfigInner object itself.
     */
    public SiteLogsConfigInner withApplicationLogs(ApplicationLogsConfig applicationLogs) {
        this.applicationLogs = applicationLogs;
        return this;
    }

    /**
     * Get the httpLogs value.
     *
     * @return the httpLogs value
     */
    public HttpLogsConfig httpLogs() {
        return this.httpLogs;
    }

    /**
     * Set the httpLogs value.
     *
     * @param httpLogs the httpLogs value to set
     * @return the SiteLogsConfigInner object itself.
     */
    public SiteLogsConfigInner withHttpLogs(HttpLogsConfig httpLogs) {
        this.httpLogs = httpLogs;
        return this;
    }

    /**
     * Get the failedRequestsTracing value.
     *
     * @return the failedRequestsTracing value
     */
    public EnabledConfig failedRequestsTracing() {
        return this.failedRequestsTracing;
    }

    /**
     * Set the failedRequestsTracing value.
     *
     * @param failedRequestsTracing the failedRequestsTracing value to set
     * @return the SiteLogsConfigInner object itself.
     */
    public SiteLogsConfigInner withFailedRequestsTracing(EnabledConfig failedRequestsTracing) {
        this.failedRequestsTracing = failedRequestsTracing;
        return this;
    }

    /**
     * Get the detailedErrorMessages value.
     *
     * @return the detailedErrorMessages value
     */
    public EnabledConfig detailedErrorMessages() {
        return this.detailedErrorMessages;
    }

    /**
     * Set the detailedErrorMessages value.
     *
     * @param detailedErrorMessages the detailedErrorMessages value to set
     * @return the SiteLogsConfigInner object itself.
     */
    public SiteLogsConfigInner withDetailedErrorMessages(EnabledConfig detailedErrorMessages) {
        this.detailedErrorMessages = detailedErrorMessages;
        return this;
    }

}
