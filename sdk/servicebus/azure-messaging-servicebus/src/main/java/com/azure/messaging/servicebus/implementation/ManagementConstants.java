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
    static final String PEEK_OPERATION = AmqpConstants.VENDOR + ":peek-message";
    static final String UPDATE_DISPOSITION_OPERATION = AmqpConstants.VENDOR + ":update-disposition";
    static final String RENEW_LOCK_OPERATION = AmqpConstants.VENDOR + ":renew-lock";
    static final String RECEIVE_BY_SEQUENCE_NUMBER_OPERATION = AmqpConstants.VENDOR
        + ":receive-by-sequence-number";
    static final String CANCEL_SCHEDULED_MESSAGE_OPERATION = AmqpConstants.VENDOR
        + ":cancel-scheduled-message";
    static final String SCHEDULE_MESSAGE_OPERATION = AmqpConstants.VENDOR + ":schedule-message";

    static final String SERVER_TIMEOUT = AmqpConstants.VENDOR + ":server-timeout";

    static final String FROM_SEQUENCE_NUMBER = "from-sequence-number";
    static final String MESSAGE_COUNT_KEY = "message-count";
    static final String REQUEST_RESPONSE_SESSION_ID = "session-id";

    // Used in updating disposition of message.
    static final String LOCK_TOKENS_KEY = "lock-tokens";
    static final String DISPOSITION_STATUS_KEY = "disposition-status";
    static final String DEADLETTER_REASON_KEY = "deadletter-reason";
    static final String DEADLETTER_DESCRIPTION_KEY = "deadletter-description";
    static final String PROPERTIES_TO_MODIFY_KEY = "properties-to-modify";
    static final String ASSOCIATED_LINK_NAME_KEY = "associated-link-name";
    static final String SEQUENCE_NUMBERS = "sequence-numbers";
    static final String RECEIVER_SETTLE_MODE = "receiver-settle-mode";
    static final String MESSAGES = "messages";
    static final String MESSAGE = "message";
    static final String MESSAGE_ID = "message-id";

    static final int MAX_MESSAGING_AMQP_HEADER_SIZE_BYTES = 512;
}
