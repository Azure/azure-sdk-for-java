/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Single sign on request information for domain management.
 */
public class DomainControlCenterSsoRequestInner {
    /**
     * Url where the single sign on request is to be made.
     */
    private String url;

    /**
     * Post parameter key.
     */
    private String postParameterKey;

    /**
     * Post parameter value. Client should use
     * 'application/x-www-form-urlencoded' encoding for this value.
     */
    private String postParameterValue;

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
     * @return the DomainControlCenterSsoRequestInner object itself.
     */
    public DomainControlCenterSsoRequestInner withUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Get the postParameterKey value.
     *
     * @return the postParameterKey value
     */
    public String postParameterKey() {
        return this.postParameterKey;
    }

    /**
     * Set the postParameterKey value.
     *
     * @param postParameterKey the postParameterKey value to set
     * @return the DomainControlCenterSsoRequestInner object itself.
     */
    public DomainControlCenterSsoRequestInner withPostParameterKey(String postParameterKey) {
        this.postParameterKey = postParameterKey;
        return this;
    }

    /**
     * Get the postParameterValue value.
     *
     * @return the postParameterValue value
     */
    public String postParameterValue() {
        return this.postParameterValue;
    }

    /**
     * Set the postParameterValue value.
     *
     * @param postParameterValue the postParameterValue value to set
     * @return the DomainControlCenterSsoRequestInner object itself.
     */
    public DomainControlCenterSsoRequestInner withPostParameterValue(String postParameterValue) {
        this.postParameterValue = postParameterValue;
        return this;
    }

}
