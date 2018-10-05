/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.primitives;

import java.io.IOException;
import java.time.*;
import java.util.Properties;
import java.util.UUID;

import org.apache.qpid.proton.amqp.*;

import com.microsoft.azure.servicebus.amqp.AmqpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientConstants
{
	final static String END_POINT_FORMAT = "amqps://%s.servicebus.windows.net";

    private ClientConstants() { }

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ClientConstants.class);

	public static final String FATAL_MARKER = "FATAL";
	public final static String PRODUCT_NAME = "MSJavaClient";
    public final static String CURRENT_JAVACLIENT_VERSION =  getClientVersion();
    public static final String PLATFORM_INFO = getPlatformInfo();
    
    public static final int DEFAULT_OPERATION_TIMEOUT_IN_SECONDS = 30;
    
	public static final int LOCKTOKENSIZE = 16;
	public static final String ENQUEUEDTIMEUTCNAME = "x-opt-enqueued-time";
	public static final String SCHEDULEDENQUEUETIMENAME = "x-opt-scheduled-enqueue-time";
	public static final String SEQUENCENUBMERNAME = "x-opt-sequence-number";
	//public static final String LOCKTOKENNAME = "x-opt-lock-token";
	public static final String LOCKEDUNTILNAME = "x-opt-locked-until";
	public static final String PARTITIONKEYNAME = "x-opt-partition-key";
	public static final String VIAPARTITIONKEYNAME = "x-opt-via-partition-key";
	public static final String DEADLETTERSOURCENAME = "x-opt-deadletter-source";
	public static final UUID ZEROLOCKTOKEN = new UUID(0l, 0l);	

	public final static int AMQPS_PORT = 5671;
	public final static int HTTPS_PORT = 443;
	public final static int MAX_PARTITION_KEY_LENGTH = 128;

	public final static Symbol SERVER_BUSY_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":server-busy");
	public final static Symbol ARGUMENT_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":argument-error");
	public final static Symbol ARGUMENT_OUT_OF_RANGE_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":argument-out-of-range");
	public final static Symbol ENTITY_DISABLED_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":entity-disabled");
	public final static Symbol PARTITION_NOT_OWNED_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":partition-not-owned");
	public final static Symbol STORE_LOCK_LOST_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":store-lock-lost");
	public final static Symbol PUBLISHER_REVOKED_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":publisher-revoked");
	public final static Symbol TIMEOUT_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":timeout");
	public final static Symbol LINK_TIMEOUT_PROPERTY = Symbol.getSymbol(AmqpConstants.VENDOR + ":timeout");
    public final static Symbol LINK_TRANSFER_DESTINATION_PROPERTY = Symbol.getSymbol(AmqpConstants.VENDOR + ":transfer-destination-address");
	public final static Symbol LINK_PEEKMODE_PROPERTY = Symbol.getSymbol(AmqpConstants.VENDOR + ":peek-mode");
	public final static Symbol TRACKING_ID_PROPERTY = Symbol.getSymbol(AmqpConstants.VENDOR + ":tracking-id");
	public static final Symbol DEADLETTERNAME = Symbol.valueOf(AmqpConstants.VENDOR + ":dead-letter");
    public static final Symbol MESSAGE_LOCK_LOST_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":message-lock-lost");
    public static final Symbol SESSION_LOCK_LOST_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":session-lock-lost");
    public static final Symbol SESSIONS_CANNOT_BE_LOCKED_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":session-cannot-be-locked");
    public static final Symbol MESSAGE_NOT_FOUND_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":message-not-found");
    public static final Symbol SESSION_NOT_FOUND_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":session-not-found");
    public static final Symbol ENTITY_ALREADY_EXISTS_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":entity-already-exists");
    public static final Symbol SESSION_FILTER = Symbol.getSymbol(AmqpConstants.VENDOR + ":session-filter");
    public static final Symbol LOCKED_UNTIL_UTC = Symbol.getSymbol(AmqpConstants.VENDOR + ":locked-until-utc");
	public final static Symbol ENTITY_TYPE_PROPERTY = Symbol.getSymbol(AmqpConstants.VENDOR + ":entity-type");
	
	public static final String DEADLETTER_REASON_HEADER = "DeadLetterReason";
    public static final String DEADLETTER_ERROR_DESCRIPTION_HEADER = "DeadLetterErrorDescription";

	public static final int MAX_MESSAGE_LENGTH_BYTES = 1024 * 1024;
	public static final int MAX_FRAME_SIZE_BYTES = 64 * 1024;
	public static final int MAX_MESSAGING_AMQP_HEADER_SIZE_BYTES = 512;

	public final static Duration TIMER_TOLERANCE = Duration.ofSeconds(1);

	public final static Duration DEFAULT_RERTRY_MIN_BACKOFF = Duration.ofSeconds(0);
	public final static Duration DEFAULT_RERTRY_MAX_BACKOFF = Duration.ofSeconds(30);

	public final static int DEFAULT_MAX_RETRY_COUNT = 10;	

	public final static boolean DEFAULT_IS_TRANSIENT = true;

	public final static int REACTOR_IO_POLL_TIMEOUT = 20;
	public final static int SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS = 4;

	public final static String NO_RETRY = "NoRetry";
	public final static String DEFAULT_RETRY = "Default";
	
	public static final String REQUEST_RESPONSE_OPERATION_NAME = "operation";
	public static final String REQUEST_RESPONSE_TIMEOUT = AmqpConstants.VENDOR + ":server-timeout";
	public static final String REQUEST_RESPONSE_RENEWLOCK_OPERATION = AmqpConstants.VENDOR + ":renew-lock";
	public static final String REQUEST_RESPONSE_RENEW_SESSIONLOCK_OPERATION = AmqpConstants.VENDOR + ":renew-session-lock";
	public static final String REQUEST_RESPONSE_RECEIVE_BY_SEQUENCE_NUMBER = AmqpConstants.VENDOR + ":receive-by-sequence-number";
	public static final String REQUEST_RESPONSE_SCHEDULE_MESSAGE_OPERATION = AmqpConstants.VENDOR + ":schedule-message";
    public static final String REQUEST_RESPONSE_CANCEL_CHEDULE_MESSAGE_OPERATION = AmqpConstants.VENDOR + ":cancel-scheduled-message";
    public static final String REQUEST_RESPONSE_PEEK_OPERATION = AmqpConstants.VENDOR + ":peek-message";
    public static final String REQUEST_RESPONSE_UPDATE_DISPOSTION_OPERATION = AmqpConstants.VENDOR + ":update-disposition";
    public static final String REQUEST_RESPONSE_GET_SESSION_STATE_OPERATION = AmqpConstants.VENDOR + ":get-session-state";
    public static final String REQUEST_RESPONSE_SET_SESSION_STATE_OPERATION = AmqpConstants.VENDOR + ":set-session-state";
    public static final String REQUEST_RESPONSE_GET_MESSAGE_SESSIONS_OPERATION = AmqpConstants.VENDOR + ":get-message-sessions";
    public static final String REQUEST_RESPONSE_ADD_RULE_OPERATION = AmqpConstants.VENDOR + ":add-rule";
    public static final String REQUEST_RESPONSE_REMOVE_RULE_OPERATION = AmqpConstants.VENDOR + ":remove-rule";
    public static final String REQUEST_RESPONSE_GET_RULES_OPERATION = AmqpConstants.VENDOR + ":enumerate-rules";
    public static final String REQUEST_RESPONSE_PUT_TOKEN_OPERATION = "put-token";
    public static final String REQUEST_RESPONSE_PUT_TOKEN_TYPE = "type";
    public static final String REQUEST_RESPONSE_PUT_TOKEN_AUDIENCE = "name";
    public static final String REQUEST_RESPONSE_PUT_TOKEN_EXPIRATION = "expiration";
	public static final String REQUEST_RESPONSE_LOCKTOKENS = "lock-tokens";
	public static final String REQUEST_RESPONSE_LOCKTOKEN = "lock-token";
	public static final String REQUEST_RESPONSE_EXPIRATION = "expiration";
	public static final String REQUEST_RESPONSE_EXPIRATIONS = "expirations";
	public static final String REQUEST_RESPONSE_SESSIONID = "session-id";
	public static final String REQUEST_RESPONSE_SESSION_STATE = "session-state";
	public static final String REQUEST_RESPONSE_SEQUENCE_NUMBERS = "sequence-numbers";
	public static final String REQUEST_RESPONSE_RECEIVER_SETTLE_MODE = "receiver-settle-mode";
	public static final String REQUEST_RESPONSE_MESSAGES = "messages";
	public static final String REQUEST_RESPONSE_MESSAGE = "message";
	public static final String REQUEST_RESPONSE_MESSAGE_ID = "message-id";
	public static final String REQUEST_RESPONSE_SESSION_ID = "session-id";
	public static final String REQUEST_RESPONSE_PARTITION_KEY = "partition-key";
    public static final String REQUEST_RESPONSE_VIA_PARTITION_KEY = "via-partition-key";
	public static final String REQUEST_RESPONSE_FROM_SEQUENCE_NUMER = "from-sequence-number";
	public static final String REQUEST_RESPONSE_MESSAGE_COUNT = "message-count";
	public static final String REQUEST_RESPONSE_STATUS_CODE = "statusCode";
    public static final String REQUEST_RESPONSE_STATUS_DESCRIPTION = "statusDescription";
    public static final String REQUEST_RESPONSE_ERROR_CONDITION = "errorCondition";
    public static final String REQUEST_RESPONSE_ASSOCIATED_LINK_NAME = "associated-link-name";
    
    // Legacy property names are used in CBS responses
    public static final String REQUEST_RESPONSE_LEGACY_STATUS_CODE = "status-code";
    public static final String REQUEST_RESPONSE_LEGACY_STATUS_DESCRIPTION = "status-description";
    public static final String REQUEST_RESPONSE_LEGACY_ERROR_CONDITION = "error-condition";
    public static final String REQUEST_RESPONSE_DISPOSITION_STATUS = "disposition-status";
    public static final String REQUEST_RESPONSE_DEADLETTER_REASON = "deadletter-reason";
    public static final String REQUEST_RESPONSE_DEADLETTER_DESCRIPTION = "deadletter-description";
    public static final String REQUEST_RESPONSE_PROPERTIES_TO_MODIFY = "properties-to-modify";
    public static final String REQUEST_RESPONSE_LAST_UPDATED_TIME = "last-updated-time";
    public static final String REQUEST_RESPONSE_LAST_SESSION_ID = "last-session-id";
    public static final String REQUEST_RESPONSE_SKIP = "skip";
    public static final String REQUEST_RESPONSE_TOP = "top";
    public static final String REQUEST_RESPONSE_SESSIONIDS = "sessions-ids";
    public static final String REQUEST_RESPONSE_RULES = "rules";
    public static final String REQUEST_RESPONSE_RULENAME = "rule-name";
    public static final String REQUEST_RESPONSE_RULEDESCRIPTION = "rule-description";
    public static final String REQUEST_RESPONSE_SQLFILTER = "sql-filter";
    public static final String REQUEST_RESPONSE_SQLRULEACTION = "sql-rule-action";
    public static final String REQUEST_RESPONSE_EXPRESSION = "expression";
    public static final String REQUEST_RESPONSE_CORRELATION_FILTER = "correlation-filter";
    public static final String REQUEST_RESPONSE_CORRELATION_ID = "correlation-id";
    public static final String REQUEST_RESPONSE_TO = "to";
    public static final String REQUEST_RESPONSE_REPLY_TO = "reply-to";
    public static final String REQUEST_RESPONSE_LABEL = "label";
    public static final String REQUEST_RESPONSE_REPLY_TO_SESSION_ID = "reply-to-session-id";
    public static final String REQUEST_RESPONSE_CONTENT_TYPE = "content-type";
    public static final String REQUEST_RESPONSE_CORRELATION_FILTER_PROPERTIES = "properties";
    
    public static final String DISPOSITION_STATUS_COMPLETED = "completed";
    public static final String DISPOSITION_STATUS_DEFERED = "defered";
    public static final String DISPOSITION_STATUS_SUSPENDED = "suspended";
    public static final String DISPOSITION_STATUS_ABANDONED = "abandoned";
//    public static final String DISPOSITION_STATUS_RENEWED = "renewed";
//    public static final String DISPOSITION_STATUS_UNLOCKED = "unlocked";
    
    public static final int REQUEST_RESPONSE_OK_STATUS_CODE = 200;
    public static final int REQUEST_RESPONSE_ACCEPTED_STATUS_CODE = 0xca;
    public static final int REQUEST_RESPONSE_NOCONTENT_STATUS_CODE = 0xcc;
    public static final int REQUEST_RESPONSE_NOTFOUND_STATUS_CODE = 0x194;
    public static final int REQUEST_RESPONSE_UNDEFINED_STATUS_CODE = -1;
    public static final int REQUEST_RESPONSE_SERVER_BUSY_STATUS_CODE = 0x1f7;

    public static final UnsignedLong RULE_DESCRIPTION_DESCRIPTOR = new UnsignedLong(0x0000013700000004L);
    public static final UnsignedLong EMPTY_RULE_ACTION_DESCRIPTOR = new UnsignedLong(0x0000013700000005L);
    public static final UnsignedLong SQL_RULE_ACTION_DESCRIPTOR = new UnsignedLong(0x0000013700000006L);
    public static final UnsignedLong SQL_FILTER_DESCRIPTOR = new UnsignedLong(0x000001370000006L);
    public static final UnsignedLong TRUE_FILTER_DESCRIPTOR = new UnsignedLong(0x000001370000007L);
    public static final UnsignedLong FALSE_FILTER_DESCRIPTOR = new UnsignedLong(0x000001370000008L);
    public static final UnsignedLong CORRELATION_FILTER_DESCRIPTOR = new UnsignedLong(0x000001370000009L);

    public static final String HTTPS_URI_FORMAT = "https://%s:%s";

    static final int DEFAULT_SAS_TOKEN_SEND_RETRY_INTERVAL_IN_SECONDS = 5;
    static final String SAS_TOKEN_AUDIENCE_FORMAT = "amqp://%s/%s";

    private static String getClientVersion() {
        String clientVersion;
        final Properties properties = new Properties();
        try {
            properties.load(ClientConstants.class.getResourceAsStream("/client.properties"));
            clientVersion = properties.getProperty("client.version");
        } catch (Exception e) {
            clientVersion = "NOTFOUND";
            TRACE_LOGGER.error("Exception while retrieving client version. Exception: ", e.toString());
        }

        return clientVersion;
    }

    private static String getPlatformInfo() {
        final Package javaRuntimeClassPkg = Runtime.class.getPackage();
        final StringBuilder patformInfo = new StringBuilder();
        patformInfo.append("jre:");
        patformInfo.append(javaRuntimeClassPkg.getImplementationVersion());
        patformInfo.append(";vendor:");
        patformInfo.append(javaRuntimeClassPkg.getImplementationVendor());
        patformInfo.append(";jvm:");
        patformInfo.append(System.getProperty("java.vm.version"));
        patformInfo.append(";arch:");
        patformInfo.append(System.getProperty("os.arch"));
        patformInfo.append(";os:");
        patformInfo.append(System.getProperty("os.name"));
        patformInfo.append(";os version:");
        patformInfo.append(System.getProperty("os.version"));

        return patformInfo.toString();
    }
	
}
