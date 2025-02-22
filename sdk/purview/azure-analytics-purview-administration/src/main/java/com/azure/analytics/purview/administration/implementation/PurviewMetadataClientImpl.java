// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.analytics.purview.administration.implementation;

import com.azure.analytics.purview.administration.PurviewMetadataServiceVersion;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;

/**
 * Initializes a new instance of the PurviewMetadataClient type.
 */
public final class PurviewMetadataClientImpl {
    /**
     * The endpoint of your Purview account. Example: https://{accountName}.purview.azure.com.
     */
    private final String endpoint;

    /**
     * Gets The endpoint of your Purview account. Example: https://{accountName}.purview.azure.com.
     * 
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Service version.
     */
    private final PurviewMetadataServiceVersion serviceVersion;

    /**
     * Gets Service version.
     * 
     * @return the serviceVersion value.
     */
    public PurviewMetadataServiceVersion getServiceVersion() {
        return this.serviceVersion;
    }

    /**
     * The HTTP pipeline to send requests through.
     */
    private final HttpPipeline httpPipeline;

    /**
     * Gets The HTTP pipeline to send requests through.
     * 
     * @return the httpPipeline value.
     */
    public HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /**
     * The serializer to serialize an object into a string.
     */
    private final SerializerAdapter serializerAdapter;

    /**
     * Gets The serializer to serialize an object into a string.
     * 
     * @return the serializerAdapter value.
     */
    public SerializerAdapter getSerializerAdapter() {
        return this.serializerAdapter;
    }

    /**
     * The MetadataRolesImpl object to access its operations.
     */
    private final MetadataRolesImpl metadataRoles;

    /**
     * Gets the MetadataRolesImpl object to access its operations.
     * 
     * @return the MetadataRolesImpl object.
     */
    public MetadataRolesImpl getMetadataRoles() {
        return this.metadataRoles;
    }

    /**
     * The MetadataPoliciesImpl object to access its operations.
     */
    private final MetadataPoliciesImpl metadataPolicies;

    /**
     * Gets the MetadataPoliciesImpl object to access its operations.
     * 
     * @return the MetadataPoliciesImpl object.
     */
    public MetadataPoliciesImpl getMetadataPolicies() {
        return this.metadataPolicies;
    }

    /**
     * Initializes an instance of PurviewMetadataClient client.
     * 
     * @param endpoint The endpoint of your Purview account. Example: https://{accountName}.purview.azure.com.
     * @param serviceVersion Service version.
     */
    public PurviewMetadataClientImpl(String endpoint, PurviewMetadataServiceVersion serviceVersion) {
        this(new HttpPipelineBuilder().policies(new UserAgentPolicy(), new RetryPolicy()).build(),
            JacksonAdapter.createDefaultSerializerAdapter(), endpoint, serviceVersion);
    }

    /**
     * Initializes an instance of PurviewMetadataClient client.
     * 
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param endpoint The endpoint of your Purview account. Example: https://{accountName}.purview.azure.com.
     * @param serviceVersion Service version.
     */
    public PurviewMetadataClientImpl(HttpPipeline httpPipeline, String endpoint,
        PurviewMetadataServiceVersion serviceVersion) {
        this(httpPipeline, JacksonAdapter.createDefaultSerializerAdapter(), endpoint, serviceVersion);
    }

    /**
     * Initializes an instance of PurviewMetadataClient client.
     * 
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param serializerAdapter The serializer to serialize an object into a string.
     * @param endpoint The endpoint of your Purview account. Example: https://{accountName}.purview.azure.com.
     * @param serviceVersion Service version.
     */
    public PurviewMetadataClientImpl(HttpPipeline httpPipeline, SerializerAdapter serializerAdapter, String endpoint,
        PurviewMetadataServiceVersion serviceVersion) {
        this.httpPipeline = httpPipeline;
        this.serializerAdapter = serializerAdapter;
        this.endpoint = endpoint;
        this.serviceVersion = serviceVersion;
        this.metadataRoles = new MetadataRolesImpl(this);
        this.metadataPolicies = new MetadataPoliciesImpl(this);
    }
}
