// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.serializer.SerializerAdapter;

/** Initializes a new instance of the ModelsRepositoryAPIImpl type. */
public final class ModelsRepositoryAPIImpl {
    /** server parameter. */
    private final String host;

    /**
     * Gets server parameter.
     *
     * @return the host value.
     */
    public String getHost() {
        return this.host;
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

    /** The ModelsRepositoryImpl object to access its operations. */
    private final ModelsRepositoryImpl modelsRepository;

    /**
     * Gets the ModelsRepositoryImpl object to access its operations.
     *
     * @return the ModelsRepositoryImpl object.
     */
    public ModelsRepositoryImpl getModelsRepository() {
        return this.modelsRepository;
    }

    /**
     * Initializes an instance of ModelsRepositoryAPI client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param serializerAdapter The serializer to serialize an object into a string.
     * @param host server parameter.
     */
    ModelsRepositoryAPIImpl(HttpPipeline httpPipeline, SerializerAdapter serializerAdapter, String host, String apiVersion) {
        this.httpPipeline = httpPipeline;
        this.serializerAdapter = serializerAdapter;
        this.host = host;
        this.apiVersion = apiVersion;
        this.modelsRepository = new ModelsRepositoryImpl(this);
    }
}
