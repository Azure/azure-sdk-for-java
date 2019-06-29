// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.blob.models;

import com.azure.storage.blob.models.CorsRule;
import com.azure.storage.blob.models.Logging;
import com.azure.storage.blob.models.Metrics;
import com.azure.storage.blob.models.RetentionPolicy;
import com.azure.storage.blob.models.StaticWebsite;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Storage Service Properties.
 */
@JacksonXmlRootElement(localName = "StorageServiceProperties")
public final class StorageServiceProperties {
    /*
     * The logging property.
     */
    @JsonProperty(value = "Logging")
    private com.azure.storage.blob.models.Logging logging;

    /*
     * The hourMetrics property.
     */
    @JsonProperty(value = "HourMetrics")
    private com.azure.storage.blob.models.Metrics hourMetrics;

    /*
     * The minuteMetrics property.
     */
    @JsonProperty(value = "MinuteMetrics")
    private com.azure.storage.blob.models.Metrics minuteMetrics;

    private static final class CorsWrapper {
        @JacksonXmlProperty(localName = "CorsRule")
        private final List<com.azure.storage.blob.models.CorsRule> items;

        @JsonCreator
        private CorsWrapper(@JacksonXmlProperty(localName = "CorsRule") List<com.azure.storage.blob.models.CorsRule> items) {
            this.items = items;
        }
    }

    /*
     * The set of CORS rules.
     */
    @JsonProperty(value = "Cors")
    private CorsWrapper cors;

    /*
     * The default version to use for requests to the Blob service if an
     * incoming request's version is not specified. Possible values include
     * version 2008-10-27 and all more recent versions
     */
    @JsonProperty(value = "DefaultServiceVersion")
    private String defaultServiceVersion;

    /*
     * The deleteRetentionPolicy property.
     */
    @JsonProperty(value = "DeleteRetentionPolicy")
    private com.azure.storage.blob.models.RetentionPolicy deleteRetentionPolicy;

    /*
     * The staticWebsite property.
     */
    @JsonProperty(value = "StaticWebsite")
    private com.azure.storage.blob.models.StaticWebsite staticWebsite;

    /**
     * Get the logging property: The logging property.
     *
     * @return the logging value.
     */
    public com.azure.storage.blob.models.Logging logging() {
        return this.logging;
    }

    /**
     * Set the logging property: The logging property.
     *
     * @param logging the logging value to set.
     * @return the StorageServiceProperties object itself.
     */
    public StorageServiceProperties logging(Logging logging) {
        this.logging = logging;
        return this;
    }

    /**
     * Get the hourMetrics property: The hourMetrics property.
     *
     * @return the hourMetrics value.
     */
    public com.azure.storage.blob.models.Metrics hourMetrics() {
        return this.hourMetrics;
    }

    /**
     * Set the hourMetrics property: The hourMetrics property.
     *
     * @param hourMetrics the hourMetrics value to set.
     * @return the StorageServiceProperties object itself.
     */
    public StorageServiceProperties hourMetrics(com.azure.storage.blob.models.Metrics hourMetrics) {
        this.hourMetrics = hourMetrics;
        return this;
    }

    /**
     * Get the minuteMetrics property: The minuteMetrics property.
     *
     * @return the minuteMetrics value.
     */
    public com.azure.storage.blob.models.Metrics minuteMetrics() {
        return this.minuteMetrics;
    }

    /**
     * Set the minuteMetrics property: The minuteMetrics property.
     *
     * @param minuteMetrics the minuteMetrics value to set.
     * @return the StorageServiceProperties object itself.
     */
    public StorageServiceProperties minuteMetrics(Metrics minuteMetrics) {
        this.minuteMetrics = minuteMetrics;
        return this;
    }

    /**
     * Get the cors property: The set of CORS rules.
     *
     * @return the cors value.
     */
    public List<com.azure.storage.blob.models.CorsRule> cors() {
        if (this.cors == null) {
            this.cors = new CorsWrapper(new ArrayList<com.azure.storage.blob.models.CorsRule>());
        }
        return this.cors.items;
    }

    /**
     * Set the cors property: The set of CORS rules.
     *
     * @param cors the cors value to set.
     * @return the StorageServiceProperties object itself.
     */
    public StorageServiceProperties cors(List<CorsRule> cors) {
        this.cors = new CorsWrapper(cors);
        return this;
    }

    /**
     * Get the defaultServiceVersion property: The default version to use for
     * requests to the Blob service if an incoming request's version is not
     * specified. Possible values include version 2008-10-27 and all more
     * recent versions.
     *
     * @return the defaultServiceVersion value.
     */
    public String defaultServiceVersion() {
        return this.defaultServiceVersion;
    }

    /**
     * Set the defaultServiceVersion property: The default version to use for
     * requests to the Blob service if an incoming request's version is not
     * specified. Possible values include version 2008-10-27 and all more
     * recent versions.
     *
     * @param defaultServiceVersion the defaultServiceVersion value to set.
     * @return the StorageServiceProperties object itself.
     */
    public StorageServiceProperties defaultServiceVersion(String defaultServiceVersion) {
        this.defaultServiceVersion = defaultServiceVersion;
        return this;
    }

    /**
     * Get the deleteRetentionPolicy property: The deleteRetentionPolicy
     * property.
     *
     * @return the deleteRetentionPolicy value.
     */
    public com.azure.storage.blob.models.RetentionPolicy deleteRetentionPolicy() {
        return this.deleteRetentionPolicy;
    }

    /**
     * Set the deleteRetentionPolicy property: The deleteRetentionPolicy
     * property.
     *
     * @param deleteRetentionPolicy the deleteRetentionPolicy value to set.
     * @return the StorageServiceProperties object itself.
     */
    public StorageServiceProperties deleteRetentionPolicy(RetentionPolicy deleteRetentionPolicy) {
        this.deleteRetentionPolicy = deleteRetentionPolicy;
        return this;
    }

    /**
     * Get the staticWebsite property: The staticWebsite property.
     *
     * @return the staticWebsite value.
     */
    public com.azure.storage.blob.models.StaticWebsite staticWebsite() {
        return this.staticWebsite;
    }

    /**
     * Set the staticWebsite property: The staticWebsite property.
     *
     * @param staticWebsite the staticWebsite value to set.
     * @return the StorageServiceProperties object itself.
     */
    public StorageServiceProperties staticWebsite(StaticWebsite staticWebsite) {
        this.staticWebsite = staticWebsite;
        return this;
    }
}
