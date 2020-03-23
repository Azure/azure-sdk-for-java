// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.apache.qpid.proton.message.Message;

/**
 * This consists of various utilities needed to manage Request/Response  channel.
 */
public class RequestResponseUtils {
    static final int REQUEST_RESPONSE_UNDEFINED_STATUS_CODE = -1;

    private static final String REQUEST_RESPONSE_STATUS_CODE = "statusCode";
    private static final String REQUEST_RESPONSE_LEGACY_STATUS_CODE = "status-code";

    /**
     * There are different status code string returned by Service bus in response.
     * @param responseMessage which was received from service bus.
     * @return status code.
     */
    public static int getResponseStatusCode(Message responseMessage) {
        int statusCode = REQUEST_RESPONSE_UNDEFINED_STATUS_CODE;

        Object codeObject = responseMessage.getApplicationProperties().getValue()
            .get(REQUEST_RESPONSE_STATUS_CODE);

        if (codeObject == null) {
            codeObject = responseMessage.getApplicationProperties().getValue()
                .get(REQUEST_RESPONSE_LEGACY_STATUS_CODE);
        }

        if (codeObject != null) {
            statusCode = (int) codeObject;
        }

        return statusCode;
    }
}
