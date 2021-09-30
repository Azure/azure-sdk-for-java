// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;

/** Initializes a new instance of the AzureSchemaRegistry type. */
public final class AzureSchemaRegistry {
    /** The Schema Registry service endpoint, for example my-namespace.servicebus.windows.net. */
    private final String endpoint;

    /**
     * Gets The Schema Registry service endpoint, for example my-namespace.servicebus.windows.net.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.endpoint;
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

    /** The Schemas object to access its operations. */
    private final Schemas schemas;

    /**
     * Gets the Schemas object to access its operations.
     *
     * @return the Schemas object.
     */
    public Schemas getSchemas() {
        return this.schemas;
    }

    /** Initializes an instance of AzureSchemaRegistry client. */
    AzureSchemaRegistry(String endpoint) {
        this(
                new HttpPipelineBuilder()
                        .policies(new UserAgentPolicy(), new RetryPolicy(), new CookiePolicy())
                        .build(),
                JacksonAdapter.createDefaultSerializerAdapter(),
                endpoint, "2020-09-01-preview");
    }

    /**
     * Initializes an instance of AzureSchemaRegistry client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     */
    AzureSchemaRegistry(HttpPipeline httpPipeline, String endpoint) {
        this(httpPipeline, JacksonAdapter.createDefaultSerializerAdapter(), endpoint, "2020-09-01-preview");
    }

    /**
     * Initializes an instance of AzureSchemaRegistry client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param serializerAdapter The serializer to serialize an object into a string.
     */
    AzureSchemaRegistry(HttpPipeline httpPipeline, SerializerAdapter serializerAdapter, String endpoint,
        String apiVersion) {
        this.httpPipeline = httpPipeline;
        this.serializerAdapter = serializerAdapter;
        this.endpoint = endpoint;
        this.apiVersion = apiVersion;
        this.schemas = new Schemas(this);
    }
}
