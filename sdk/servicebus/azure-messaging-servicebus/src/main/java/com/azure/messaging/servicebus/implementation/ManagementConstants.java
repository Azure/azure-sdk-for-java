// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.implementation.AmqpConstants;

/**
 * Constants which is used for management calls to support operations for example renewlock, schedule, defer etc.
 */
public class ManagementConstants {
    public static final String REQUEST_RESPONSE_FROM_SEQUENCE_NUMBER = "from-sequence-number";
    public static final String REQUEST_RESPONSE_MESSAGE_COUNT = "message-count";
    public static final String REQUEST_RESPONSE_SESSION_ID = "session-id";
    public static final String LOCKEDUNTILNAME = "x-opt-locked-until";
    public static final String PARTITIONKEYNAME = "x-opt-partition-key";
    public static final String VIAPARTITIONKEYNAME = "x-opt-via-partition-key";
    public static final String DEADLETTERSOURCENAME = "x-opt-deadletter-source";
    public static final String REQUEST_RESPONSE_MESSAGES = "messages";
    public static final String REQUEST_RESPONSE_MESSAGE = "message";
    public static final String REQUEST_RESPONSE_MESSAGE_ID = "message-id";
    public static final String REQUEST_RESPONSE_PARTITION_KEY = "partition-key";
    public static final String REQUEST_RESPONSE_VIA_PARTITION_KEY = "via-partition-key";
    public static final String REQUEST_RESPONSE_SCHEDULE_MESSAGE_OPERATION = AmqpConstants.VENDOR + ":schedule-message";
    public static final String REQUEST_RESPONSE_OPERATION_NAME = "operation";
    public static final String REQUEST_RESPONSE_TIMEOUT = AmqpConstants.VENDOR + ":server-timeout";
    public static final String REQUEST_RESPONSE_ASSOCIATED_LINK_NAME = "associated-link-name";


    // Numeric values here
    public static final int REQUEST_RESPONSE_OK_STATUS_CODE = 200;
}
