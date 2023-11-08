// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.CallConnection;
import com.azure.communication.callingserver.CallConnectionAsync;

/**
 * The abstract class used as parent of [action]CallResult
 */
public abstract class CallResult {
    /*
     * The callConnectionProperties
     */
    private final CallConnectionProperties callConnectionProperties;

    /*
     * The callConnection
     */
    private final CallConnection callConnection;

    /*
     * The callConnectionAsync
     */
    private final CallConnectionAsync callConnectionAsync;

    CallResult(CallConnectionProperties callConnectionProperties, CallConnection callConnection, CallConnectionAsync callConnectionAsync) {
        this.callConnectionProperties = callConnectionProperties;
        this.callConnection = callConnection;
        this.callConnectionAsync = callConnectionAsync;
    }

    /**
     * Get the callConnectionProperties.
     *
     * @return the callConnectionProperties.
     */
    public CallConnectionProperties getCallConnectionProperties() {
        return callConnectionProperties;
    }

    /**
     * Get the callConnection.
     *
     * @return the callConnection.
     */
    public CallConnection getCallConnection() {
        return callConnection;
    }

    /**
     * Get the callConnectionAsync.
     *
     * @return the callConnectionAsync.
     */
    public CallConnectionAsync getCallConnectionAsync() {
        return callConnectionAsync;
    }
}
