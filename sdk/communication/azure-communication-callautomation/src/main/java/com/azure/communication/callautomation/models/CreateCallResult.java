// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.CallConnection;
import com.azure.communication.callautomation.CallConnectionAsync;

/**
 * The result of answering a call
 */
public class CreateCallResult extends CallResult {

    /**
     * Constructor
     *
     * @param callConnectionProperties The callConnectionProperties
     * @param callConnection The callConnection
     * @param callConnectionAsync The callConnectionAsync
     */
    public CreateCallResult(CallConnectionProperties callConnectionProperties, CallConnection callConnection, CallConnectionAsync callConnectionAsync) {
        super(callConnectionProperties, callConnection, callConnectionAsync);
    }
}
