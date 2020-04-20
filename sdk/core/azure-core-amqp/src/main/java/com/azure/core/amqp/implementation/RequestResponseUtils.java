// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpResponseCode;
import org.apache.qpid.proton.message.Message;

import java.util.Map;

/**
 * This consists of various utilities needed to manage Request/Response channel.
 */
class RequestResponseUtils {
    static final int UNDEFINED_STATUS_CODE = -1;
    static final String UNDEFINED_STATUS_DESCRIPTION = "";

    private static final String STATUS_CODE = "statusCode";
    private static final String STATUS_DESCRIPTION = "statusDescription";

    private static final String LEGACY_STATUS_CODE = "status-code";
    private static final String LEGACY_STATUS_DESCRIPTION = "status-description";

    /**
     * Gets the {@link AmqpResponseCode} from RequestResponse messages.
     *
     * @param message Response from management channel.
     *
     * @return The status code or {@link #UNDEFINED_STATUS_CODE} if there is no status code.
     */
    static int getStatusCode(Message message) {
        final Map<String, Object> properties = message.getApplicationProperties().getValue();

        if (properties == null) {
            return UNDEFINED_STATUS_CODE;
        } else if (properties.containsKey(STATUS_CODE)) {
            return (int) properties.get(STATUS_CODE);
        } else if (properties.containsKey(LEGACY_STATUS_CODE)) {
            return (int) properties.get(LEGACY_STATUS_CODE);
        } else {
            return UNDEFINED_STATUS_CODE;
        }
    }

    /**
     * Gets the status description from a RequestResponse message.
     *
     * @param message Message to extract status description from.
     *
     * @return The status message, or an empty string if it does not contain one.
     */
    static String getStatusDescription(Message message) {
        final Map<String, Object> properties = message.getApplicationProperties().getValue();

        if (properties == null) {
            return UNDEFINED_STATUS_DESCRIPTION;
        } else if (properties.containsKey(STATUS_DESCRIPTION)) {
            return String.valueOf(properties.get(STATUS_DESCRIPTION));
        } else if (properties.containsKey(LEGACY_STATUS_DESCRIPTION)) {
            return String.valueOf(properties.get(LEGACY_STATUS_DESCRIPTION));
        } else {
            return UNDEFINED_STATUS_DESCRIPTION;
        }
    }
}
