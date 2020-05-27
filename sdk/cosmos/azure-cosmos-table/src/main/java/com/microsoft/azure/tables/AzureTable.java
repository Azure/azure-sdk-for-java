package com.microsoft.azure.tables;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;

/** Initializes a new instance of the AzureTable type. */
public final class AzureTable {
    /** The URL of the service account or table that is the targe of the desired operation. */
    private String url;

    /**
     * Gets The URL of the service account or table that is the targe of the desired operation.
     *
     * @return the url value.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Sets The URL of the service account or table that is the targe of the desired operation.
     *
     * @param url the url value.
     * @return the service client itself.
     */
    public AzureTable setUrl(String url) {
        this.url = url;
        return this;
    }

    /** Specifies the version of the operation to use for this request. */
    private String version;

    /**
     * Gets Specifies the version of the operation to use for this request.
     *
     * @return the version value.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets Specifies the version of the operation to use for this request.
     *
     * @param version the version value.
     * @return the service client itself.
     */
    public AzureTable setVersion(String version) {
        this.version = version;
        return this;
    }

    /** The HTTP pipeline to send requests through. */
    private final HttpPipeline httpPipeline;

    /**
     * Gets The HTTP pipeline to send requests through.
     *
     * @return the httpPipeline value.
     */
    public HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /** The Tables object to access its operations. */
    private final Tables tables;

    /**
     * Gets the Tables object to access its operations.
     *
     * @return the Tables object.
     */
    public Tables getTables() {
        return this.tables;
    }

    /** The Services object to access its operations. */
    private final Services services;

    /**
     * Gets the Services object to access its operations.
     *
     * @return the Services object.
     */
    public Services getServices() {
        return this.services;
    }

    /** Initializes an instance of AzureTable client. */
    public AzureTable() {
        this(new HttpPipelineBuilder().policies(new UserAgentPolicy(), new RetryPolicy(), new CookiePolicy()).build());
    }

    /**
     * Initializes an instance of AzureTable client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     */
    public AzureTable(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.tables = new Tables(this);
        this.services = new Services(this);
    }
}
