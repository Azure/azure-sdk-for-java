// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.implementation.AmqpConstants;

/**
 * Constants which is used for management calls to support operations for example renewlock, schedule, defer etc.
 */
class ManagementConstants {
    // Operation name key.
    static final String MANAGEMENT_OPERATION_KEY = "operation";

    // Names of operations that can be done through management node.
    static final String PEEK_OPERATION_VALUE = AmqpConstants.VENDOR + ":peek-message";
    static final String UPDATE_DISPOSITION_OPERATION = AmqpConstants.VENDOR + ":update-disposition";

    static final String SERVER_TIMEOUT = AmqpConstants.VENDOR + ":server-timeout";

    static final String REQUEST_RESPONSE_FROM_SEQUENCE_NUMBER = "from-sequence-number";
    static final String MESSAGE_COUNT_KEY = "message-count";
    static final String REQUEST_RESPONSE_SESSION_ID = "session-id";

    // Used in updating disposition of message.
    static final String LOCK_TOKENS_KEY = "lock-tokens";
    static final String DISPOSITION_STATUS_KEY = "disposition-status";
    static final String DEADLETTER_REASON_KEY = "deadletter-reason";
    static final String DEADLETTER_DESCRIPTION_KEY = "deadletter-description";
    static final String PROPERTIES_TO_MODIFY_KEY = "properties-to-modify";
    static final String ASSOCIATED_LINK_NAME_KEY = "associated-link-name";

    static final String LOCKED_UNTIL_NAME = "x-opt-locked-until";
    static final String PARTITION_KEY_NAME = "x-opt-partition-key";
    static final String VIA_PARTITION_KEY_NAME = "x-opt-via-partition-key";
    static final String DEAD_LETTER_SOURCE_NAME = "x-opt-deadletter-source";

    static final String REQUEST_RESPONSE_MESSAGE_ID = "message-id";
    static final String REQUEST_RESPONSE_PARTITION_KEY = "partition-key";
    static final String REQUEST_RESPONSE_VIA_PARTITION_KEY = "via-partition-key";
    static final String REQUEST_RESPONSE_SCHEDULE_MESSAGE_OPERATION = AmqpConstants.VENDOR + ":schedule-message";
    static final String REQUEST_RESPONSE_ASSOCIATED_LINK_NAME = "associated-link-name";
    static final String LOCK_TOKENS = "lock-tokens";
    static final String RENEW_LOCK_OPERATION = AmqpConstants.VENDOR + ":renew-lock";
    static final int REQUEST_RESPONSE_OK_STATUS_CODE = 200;
    static final String REQUEST_RESPONSE_EXPIRATIONS = "expirations";
}
