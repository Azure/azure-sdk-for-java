// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.CallConnection;
import com.azure.communication.callautomation.CallConnectionAsync;
import com.azure.core.annotation.Immutable;

/**
 * The result of connect request.
 */
@Immutable
public final class ConnectResult extends CallResult {
    /**
     * Constructor
     *
     * @param callConnectionProperties The callConnectionProperties
     * @param callConnection The callConnection
     * @param callConnectionAsync The callConnectionAsync
     */
    public ConnectResult(CallConnectionProperties callConnectionProperties, CallConnection callConnection, CallConnectionAsync callConnectionAsync) {
        super(callConnectionProperties, callConnection, callConnectionAsync);
    }
}
