// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
//

package com.azure.iot.deviceupdate;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;

/** Initializes a new instance of the DeviceUpdateClient type. */
public final class DeviceUpdateClient {
    /** Account endpoint. */
    private final String accountEndpoint;

    /**
     * Gets Account endpoint.
     *
     * @return the accountEndpoint value.
     */
    public String getAccountEndpoint() {
        return this.accountEndpoint;
    }

    /** Account instance identifier. */
    private final String instanceId;

    /**
     * Gets Account instance identifier.
     *
     * @return the instanceId value.
     */
    public String getInstanceId() {
        return this.instanceId;
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

    /** The Updates object to access its operations. */
    private final Updates updates;

    /**
     * Gets the Updates object to access its operations.
     *
     * @return the Updates object.
     */
    public Updates getUpdates() {
        return this.updates;
    }

    /** The Devices object to access its operations. */
    private final Devices devices;

    /**
     * Gets the Devices object to access its operations.
     *
     * @return the Devices object.
     */
    public Devices getDevices() {
        return this.devices;
    }

    /** The Deployments object to access its operations. */
    private final Deployments deployments;

    /**
     * Gets the Deployments object to access its operations.
     *
     * @return the Deployments object.
     */
    public Deployments getDeployments() {
        return this.deployments;
    }

    /**
     * Initializes an instance of DeviceUpdateClient client.
     *
     * @param accountEndpoint Account endpoint.
     * @param instanceId Account instance identifier.
     */
    DeviceUpdateClient(String accountEndpoint, String instanceId) {
        this(
                new HttpPipelineBuilder()
                        .policies(new UserAgentPolicy(), new RetryPolicy(), new CookiePolicy())
                        .build(),
                JacksonAdapter.createDefaultSerializerAdapter(),
                accountEndpoint,
                instanceId);
    }

    /**
     * Initializes an instance of DeviceUpdateClient client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param accountEndpoint Account endpoint.
     * @param instanceId Account instance identifier.
     */
    DeviceUpdateClient(HttpPipeline httpPipeline, String accountEndpoint, String instanceId) {
        this(httpPipeline, JacksonAdapter.createDefaultSerializerAdapter(), accountEndpoint, instanceId);
    }

    /**
     * Initializes an instance of DeviceUpdateClient client.
     *
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param serializerAdapter The serializer to serialize an object into a string.
     * @param accountEndpoint Account endpoint.
     * @param instanceId Account instance identifier.
     */
    DeviceUpdateClient(
            HttpPipeline httpPipeline, SerializerAdapter serializerAdapter, String accountEndpoint, String instanceId) {
        this.httpPipeline = httpPipeline;
        this.serializerAdapter = serializerAdapter;
        this.accountEndpoint = accountEndpoint;
        this.instanceId = instanceId;
        this.updates = new Updates(this);
        this.devices = new Devices(this);
        this.deployments = new Deployments(this);
    }
}
