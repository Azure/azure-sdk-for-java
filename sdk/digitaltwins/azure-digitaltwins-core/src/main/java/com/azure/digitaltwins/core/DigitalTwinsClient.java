// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.digitaltwins.core.models.DigitalTwinsGetByIdResponse;

/**
 * This class provides a client for interacting synchronously with an Azure Digital Twins instance.
 *
 * <p>
 * This client is instantiated through {@link DigitalTwinsClientBuilder}.
 * </p>
 *
 * <p>
 * This client allows for management of digital twins, their components, and their relationships. It also allows for managing
 * the digital twin models and event routes tied to your Azure Digital Twins instance.
 * </p>
 */
@ServiceClient(builder = DigitalTwinsClientBuilder.class)
public class DigitalTwinsClient {
    private final DigitalTwinsAsyncClient digitalTwinsAsyncClient;

    protected DigitalTwinsClient(DigitalTwinsAsyncClient digitalTwinsAsyncClient) {
        this.digitalTwinsAsyncClient = digitalTwinsAsyncClient;
    }

    /**
     * Gets the {@link HttpPipeline} that this client is configured to use for all service requests. This pipeline can
     * be customized while building this client through {@link DigitalTwinsClientBuilder#httpPipeline(HttpPipeline)}.
     *
     * @return The {@link HttpPipeline} that this client uses for all service requests.
     */
    public HttpPipeline getHttpPipeline() {
        return digitalTwinsAsyncClient.getHttpPipeline();
    }

    /**
     * Gets the Azure Digital Twins service API version that this client is configured to use for all service requests.
     * Unless configured while building this client through {@link DigitalTwinsClientBuilder#serviceVersion(DigitalTwinsServiceVersion)},
     * this value will be equal to the latest service API version supported by this client.
     *
     * @return The Azure Digital Twins service API version.
     */
    public DigitalTwinsServiceVersion getServiceVersion() {
        return this.digitalTwinsAsyncClient.getServiceVersion();
    }

    // this annotation lets users know this method makes a call to the service and whether it returns a single resource or a collection of resources
    @ServiceMethod(returns = ReturnType.SINGLE)
    // TODO This is just a temporary implementation for test purposes. This should be spruced up/replaced once this API is actually designed
    public DigitalTwinsGetByIdResponse getDigitalTwin(String digitalTwinId) {
        // Blocking calls to the async client is the established pattern in track 2 Java SDKs
        return this.digitalTwinsAsyncClient.getDigitalTwin(digitalTwinId).block();
    }
}
