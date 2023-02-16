// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.implementation.AmqpConstants;

/**
 * Constants which is used for management calls to support operations for example renewlock, schedule, defer etc.
 */
public class ManagementConstants {
    // Properties set on management request and response messages.
    public static final String ASSOCIATED_LINK_NAME_KEY = "associated-link-name";
    public static final String EXPIRATION = "expiration";
    public static final String EXPIRATIONS = "expirations";
    public static final String FROM_SEQUENCE_NUMBER = "from-sequence-number";
    public static final String LOCK_TOKEN_KEY = "lock-token";
    public static final String LOCK_TOKENS_KEY = "lock-tokens";
    public static final String MESSAGE_COUNT_KEY = "message-count";
    public static final String MESSAGE = "message";
    public static final String MESSAGES = "messages";
    public static final String MESSAGE_ID = "message-id";
    public static final String PARTITION_KEY = "partition-key";
    public static final String RECEIVER_SETTLE_MODE = "receiver-settle-mode";
    public static final String SEQUENCE_NUMBERS = "sequence-numbers";
    public static final String SESSION_ID = "session-id";
    public static final String SESSION_STATE = "session-state";
    public static final String VIA_PARTITION_KEY = "via-partition-key";
    public static final String RULE_NAME = "rule-name";
    public static final String RULE_DESCRIPTION = "rule-description";
    public static final String SQL_RULE_FILTER = "sql-filter";
    public static final String EXPRESSION = "expression";
    public static final String CORRELATION_ID = "correlation-id";
    public static final String TO = "to";
    public static final String REPLY_TO = "reply-to";
    public static final String LABEL = "label";
    public static final String REPLY_TO_SESSION_ID = "reply-to-session-id";
    public static final String CONTENT_TYPE = "content-type";
    public static final String CORRELATION_FILTER_PROPERTIES = "properties";
    public static final String CORRELATION_FILTER = "correlation-filter";
    public static final String SQL_RULE_ACTION = "sql-rule-action";
    public static final String TOP = "top";
    public static final String SKIP = "skip";
    public static final String STATUS_CODE = "statusCode";
    public static final String LEGACY_STATUS_CODE = "status-code";
    public static final String RULES = "rules";

    // Operation name key.
    static final String MANAGEMENT_OPERATION_KEY = "operation";

    // Names of operations that can be done through management node.
    static final String OPERATION_CANCEL_SCHEDULED_MESSAGE = AmqpConstants.VENDOR + ":cancel-scheduled-message";
    static final String OPERATION_GET_SESSION_STATE = AmqpConstants.VENDOR + ":get-session-state";
    static final String OPERATION_PEEK = AmqpConstants.VENDOR + ":peek-message";
    static final String OPERATION_RENEW_LOCK = AmqpConstants.VENDOR + ":renew-lock";
    static final String OPERATION_RENEW_SESSION_LOCK = AmqpConstants.VENDOR + ":renew-session-lock";
    static final String OPERATION_RECEIVE_BY_SEQUENCE_NUMBER = AmqpConstants.VENDOR + ":receive-by-sequence-number";
    static final String OPERATION_SCHEDULE_MESSAGE = AmqpConstants.VENDOR + ":schedule-message";
    static final String OPERATION_SET_SESSION_STATE = AmqpConstants.VENDOR + ":set-session-state";
    static final String OPERATION_UPDATE_DISPOSITION = AmqpConstants.VENDOR + ":update-disposition";
    static final String OPERATION_ADD_RULE = AmqpConstants.VENDOR + ":add-rule";
    static final String OPERATION_REMOVE_RULE = AmqpConstants.VENDOR + ":remove-rule";
    static final String OPERATION_GET_RULES = AmqpConstants.VENDOR + ":enumerate-rules";

    static final String SERVER_TIMEOUT = AmqpConstants.VENDOR + ":server-timeout";

    // Used in updating disposition of message.
    static final String DEADLETTER_DESCRIPTION_KEY = "deadletter-description";
    static final String DEADLETTER_REASON_KEY = "deadletter-reason";
    static final String DISPOSITION_STATUS_KEY = "disposition-status";
    static final String PROPERTIES_TO_MODIFY_KEY = "properties-to-modify";

    static final int MAX_MESSAGING_AMQP_HEADER_SIZE_BYTES = 512;
}
