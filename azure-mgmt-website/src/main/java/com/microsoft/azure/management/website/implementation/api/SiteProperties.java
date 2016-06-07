/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;

/**
 * The SiteProperties model.
 */
public class SiteProperties {
    /**
     * The metadata property.
     */
    private List<NameValuePair> metadata;

    /**
     * The properties property.
     */
    private List<NameValuePair> properties;

    /**
     * The appSettings property.
     */
    private List<NameValuePair> appSettings;

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public List<NameValuePair> metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the SiteProperties object itself.
     */
    public SiteProperties withMetadata(List<NameValuePair> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public List<NameValuePair> properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the SiteProperties object itself.
     */
    public SiteProperties withProperties(List<NameValuePair> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get the appSettings value.
     *
     * @return the appSettings value
     */
    public List<NameValuePair> appSettings() {
        return this.appSettings;
    }

    /**
     * Set the appSettings value.
     *
     * @param appSettings the appSettings value to set
     * @return the SiteProperties object itself.
     */
    public SiteProperties withAppSettings(List<NameValuePair> appSettings) {
        this.appSettings = appSettings;
        return this;
    }

}
