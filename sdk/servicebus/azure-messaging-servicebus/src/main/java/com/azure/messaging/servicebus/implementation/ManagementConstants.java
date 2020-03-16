// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.implementation.AmqpConstants;

/**
 * Constants which is used for management calls to support operations for example renewlock, schedule, defer etc.
 */
class ManagementConstants {
    static final String REQUEST_RESPONSE_FROM_SEQUENCE_NUMBER = "from-sequence-number";
    static final String REQUEST_RESPONSE_MESSAGE_COUNT = "message-count";
    static final String REQUEST_RESPONSE_SESSION_ID = "session-id";
    static final String LOCKEDUNTILNAME = "x-opt-locked-until";
    static final String PARTITIONKEYNAME = "x-opt-partition-key";
    static final String VIAPARTITIONKEYNAME = "x-opt-via-partition-key";
    static final String DEADLETTERSOURCENAME = "x-opt-deadletter-source";
    static final String REQUEST_RESPONSE_MESSAGE_ID = "message-id";
    static final String REQUEST_RESPONSE_PARTITION_KEY = "partition-key";
    static final String REQUEST_RESPONSE_VIA_PARTITION_KEY = "via-partition-key";
    static final String REQUEST_RESPONSE_SCHEDULE_MESSAGE_OPERATION = AmqpConstants.VENDOR + ":schedule-message";
    static final String REQUEST_RESPONSE_OPERATION_NAME = "operation";
    static final String REQUEST_RESPONSE_TIMEOUT = AmqpConstants.VENDOR + ":server-timeout";
    static final String REQUEST_RESPONSE_ASSOCIATED_LINK_NAME = "associated-link-name";

}
