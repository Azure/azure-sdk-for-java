// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.implementation.AmqpConstants;

/**
 * Constants which is used for management calls to support operations for example renewlock, schedule, defer etc.
 */
class ManagementConstants {
    // Well-known keys for management plane service requests.
    static final String MANAGEMENT_ENTITY_TYPE_KEY = "type";
    static final String MANAGEMENT_OPERATION_KEY = "operation";
    static final String MANAGEMENT_SECURITY_TOKEN_KEY = "security_token";

    // Well-known values for the service request.
    static final String PEEK_OPERATION_VALUE = AmqpConstants.VENDOR + ":peek-message";
    static final String MANAGEMENT_SERVICEBUS_ENTITY_TYPE = AmqpConstants.VENDOR + ":servicebus";
    static final String MANAGEMENT_SERVER_TIMEOUT = AmqpConstants.VENDOR + ":server-timeout";

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

    // Well-known keys from the management service responses and requests.
    static final String MANAGEMENT_ENTITY_NAME_KEY = "name";
    static final String MANAGEMENT_PARTITION_NAME_KEY = "partition";
    static final String MANAGEMENT_RESULT_PARTITION_IDS = "partition_ids";
    static final String MANAGEMENT_RESULT_CREATED_AT = "created_at";
    static final String MANAGEMENT_RESULT_BEGIN_SEQUENCE_NUMBER = "begin_sequence_number";
    static final String MANAGEMENT_RESULT_LAST_ENQUEUED_SEQUENCE_NUMBER = "last_enqueued_sequence_number";
    static final String MANAGEMENT_RESULT_LAST_ENQUEUED_OFFSET = "last_enqueued_offset";
    static final String MANAGEMENT_RESULT_LAST_ENQUEUED_TIME_UTC = "last_enqueued_time_utc";
    static final String MANAGEMENT_RESULT_RUNTIME_INFO_RETRIEVAL_TIME_UTC = "runtime_info_retrieval_time_utc";
    static final String MANAGEMENT_RESULT_PARTITION_IS_EMPTY = "is_partition_empty";
}
