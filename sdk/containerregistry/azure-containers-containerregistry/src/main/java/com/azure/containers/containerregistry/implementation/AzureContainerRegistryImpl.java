// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.containers.containerregistry.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;

/** Initializes a new instance of the AzureContainerRegistry type. */
public final class AzureContainerRegistryImpl {
    /** Registry login URL. */
    private final String url;

    /**
     * Gets Registry login URL.
     *
     * @return the url value.
     */
    public String getUrl() {
        return this.url;
    }

    /** Api Version. */
    private final String apiVersion;

    /**
     * Gets Api Version.
     *
     * @return the apiVersion value.
     */
    public String getApiVersion() {
        return this.apiVersion;
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

    /** The serializer to serialize an object into a string. */
    private final SerializerAdapter serializerAdapter;

    /**
     * Gets The serializer to serialize an object into a string.
     *
     * @return the serializerAdapter value.
     */
    public SerializerAdapter getSerializerAdapter() {
        return this.serializerAdapter;
    }

    /** The ContainerRegistriesImpl object to access its operations. */
    private final ContainerRegistriesImpl containerRegistries;

    /**
     * Gets the ContainerRegistriesImpl object to access its operations.
     *
     * @return the ContainerRegistriesImpl object.
     */
    public ContainerRegistriesImpl getContainerRegistries() {
        return this.containerRegistries;
    }

    /** The ContainerRegistryBlobsImpl object to access its operations. */
    private final ContainerRegistryBlobsImpl containerRegistryBlobs;

    /**
     * Gets the ContainerRegistryBlobsImpl object to access its operations.
     *
     * @return the ContainerRegistryBlobsImpl object.
     */
    public ContainerRegistryBlobsImpl getContainerRegistryBlobs() {
        return this.containerRegistryBlobs;
    }

    /** The AuthenticationsImpl object to access its operations. */
    private final AuthenticationsImpl authentications;

    /**
     * Gets the AuthenticationsImpl object to access its operations.
     *
     * @return the AuthenticationsImpl object.
     */
    public AuthenticationsImpl getAuthentications() {
        return this.authentications;
    }

    /**
     * Initializes an instance of AzureContainerRegistry client.
     *
     * @param url Registry login URL.
     * @param apiVersion Api Version.
     */
    public AzureContainerRegistryImpl(String url, String apiVersion) {
        this(new HttpPipelineBuilder().policies(new UserAgentPolicy(), new RetryPolicy(), new CookiePolicy()).build(),
            JacksonAdapter.createDefaultSerializerAdapter(), url, apiVersion);
    }

    /**
     * Initializes an instance of AzureContainerRegistry client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param url Registry login URL.
     * @param apiVersion Api Version.
     */
    public AzureContainerRegistryImpl(HttpPipeline httpPipeline, String url, String apiVersion) {
        this(httpPipeline, JacksonAdapter.createDefaultSerializerAdapter(), url, apiVersion);
    }

    /**
     * Initializes an instance of AzureContainerRegistry client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param serializerAdapter The serializer to serialize an object into a string.
     * @param url Registry login URL.
     * @param apiVersion Api Version.
     */
    public AzureContainerRegistryImpl(HttpPipeline httpPipeline, SerializerAdapter serializerAdapter, String url,
        String apiVersion) {
        this.httpPipeline = httpPipeline;
        this.serializerAdapter = serializerAdapter;
        this.url = url;
        this.apiVersion = apiVersion;
        this.containerRegistries = new ContainerRegistriesImpl(this);
        this.containerRegistryBlobs = new ContainerRegistryBlobsImpl(this);
        this.authentications = new AuthenticationsImpl(this);
    }
}
