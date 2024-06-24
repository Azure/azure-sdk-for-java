// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;
import com.azure.core.annotation.Fluent;

/** The options for creating a call. */

@Fluent
public final class ConnectCallOptions {
     /**
     * Either a {@link GroupCallLocator} or {@link ServerCallLocator} or {@link RoomCallLocator} for locating the call.
     */
    private final CallLocator callLocator;

    /**
     * The call back URl.
     */
    private final String callbackUrl;

     /**
     * The value to identify context of the operation.
     */
    private String operationContext;

    /*
     * AI options for the call.
     */
    private CallIntelligenceOptions callIntelligenceOptions;

    /**
     * Creates a new instance of ConnectCallOptions
     * @param callLocator - The CallLocator
     * @param callbackUrl - The CallbackUrl
     */
    public ConnectCallOptions(CallLocator callLocator, String callbackUrl) {
        this.callLocator = callLocator;
        this.callbackUrl = callbackUrl;
    }

    /**
     * Get the call back url.
     *
     * @return the call back url.
     */
    public String getCallbackUrl() {
        return this.callbackUrl;
    }

    /**
     * Get the call locator.
     *
     * @return the call locator.
     */
    public CallLocator getCallLocator() {
        return this.callLocator;
    }

    /**
     * Get the operation context.
     *
     * @return operation context.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Get the CallIntelligenceOptions property: AI options for the call such as cognitiveServicesEndpoint
     *
     * @return the callIntelligenceOptions value.
     */
    public CallIntelligenceOptions getCallIntelligenceOptions() {
        return this.callIntelligenceOptions;
    }

    /**
     * Set the CallIntelligenceOptions property: AI options for the call such as cognitiveServicesEndpoint
     *
     * @param callIntelligenceOptions the cognitiveServicesEndpoint value to set.
     * @return the ConnectCallOptions object itself.
     */
    public ConnectCallOptions setCallIntelligenceOptions(CallIntelligenceOptions callIntelligenceOptions) {
        this.callIntelligenceOptions = callIntelligenceOptions;
        return this;
    }

     /**
     * Sets the operation context.
     *
     * @param operationContext Operation Context
     * @return The ConnectCallOptions object.
     */
    public ConnectCallOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
