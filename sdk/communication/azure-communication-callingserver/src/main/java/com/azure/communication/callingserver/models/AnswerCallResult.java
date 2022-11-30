// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.communication.callingserver.CallConnection;
import com.azure.communication.callingserver.CallConnectionAsync;

/**
 * The result of answering a call
 */
public class AnswerCallResult extends CallResult {

    /**
     * Constructor
     *
     * @param callConnectionProperties The callConnectionProperties
     * @param callConnection The callConnection
     * @param callConnectionAsync The callConnectionAsync
     */
    public AnswerCallResult(CallConnectionProperties callConnectionProperties, CallConnection callConnection, CallConnectionAsync callConnectionAsync) {
        super(callConnectionProperties, callConnection, callConnectionAsync);
    }
}
