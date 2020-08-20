// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

/**
 * Client Options for setting common properties for example applicationId, contentType. Most of these properties are
 * applied on request being send to Azure Service but some could be used for other purpose also for example
 * applicationId is also used for telemetry.
 */
public class ClientOptions {
    private String applicationId;
    private String contentType;

    /**
     * applicationId
     * @return applicationId
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * sets id
     * @param applicationId id
     * @return updated options
     */
    public ClientOptions setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    /**
     * applicationId
     * @return applicationId
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * sets id
     * @param contentType id
     * @return updated options
     */
    public ClientOptions setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * sets headers
     * @param headers headers
     * @return updated options
     */
    public ClientOptions headers(Headers headers) {
        return this;
    }

    /**
     * sets headers
     * @param name name of the header
     * @param value value for this header
     * @return updated options
     */
    public ClientOptions header(String name, String value) {
        return this;
    }
}
