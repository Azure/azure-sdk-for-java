/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.time.Duration;

import org.apache.qpid.proton.amqp.Symbol;

import com.microsoft.azure.eventhubs.amqp.AmqpConstants;

public final class ClientConstants {
    private ClientConstants() {
    }

    public final static int AMQPS_PORT = 5671;
    public final static int MAX_PARTITION_KEY_LENGTH = 128;

    public final static Symbol SERVER_BUSY_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":server-busy");
    public final static Symbol ARGUMENT_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":argument-error");
    public final static Symbol ARGUMENT_OUT_OF_RANGE_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":argument-out-of-range");
    public final static Symbol ENTITY_DISABLED_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":entity-disabled");
    public final static Symbol PARTITION_NOT_OWNED_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":partition-not-owned");
    public final static Symbol STORE_LOCK_LOST_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":store-lock-lost");
    public final static Symbol PUBLISHER_REVOKED_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":publisher-revoked");
    public final static Symbol TIMEOUT_ERROR = Symbol.getSymbol(AmqpConstants.VENDOR + ":timeout");
    public final static Symbol TRACKING_ID_PROPERTY = Symbol.getSymbol(AmqpConstants.VENDOR + ":tracking-id");

    public static final int MAX_MESSAGE_LENGTH_BYTES = 256 * 1024;
    public static final int MAX_FRAME_SIZE_BYTES = 64 * 1024;
    public static final int MAX_EVENTHUB_AMQP_HEADER_SIZE_BYTES = 512;

    public final static Duration TIMER_TOLERANCE = Duration.ofSeconds(1);

    public final static Duration DEFAULT_RETRY_MIN_BACKOFF = Duration.ofSeconds(0);
    public final static Duration DEFAULT_RETRY_MAX_BACKOFF = Duration.ofSeconds(30);
    public final static Duration TOKEN_REFRESH_INTERVAL = Duration.ofMinutes(10); // renew every 10 mins, which expires 20 mins
    public final static Duration TOKEN_VALIDITY = Duration.ofMinutes(20);

    public final static int DEFAULT_MAX_RETRY_COUNT = 10;

    public final static boolean DEFAULT_IS_TRANSIENT = true;

    public final static int SESSION_OPEN_TIMEOUT_IN_MS = 15000;

    public final static int REACTOR_IO_POLL_TIMEOUT = 20;
    public final static int SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS = 4;

    public final static String NO_RETRY = "NoRetry";
    public final static String DEFAULT_RETRY = "Default";

    public final static String PRODUCT_NAME = "MSJavaClient";
    public final static String CURRENT_JAVACLIENT_VERSION = "1.0.0-SNAPSHOT";

    public static final String PLATFORM_INFO = getPlatformInfo();
    public static final String FRAMEWORK_INFO = getFrameworkInfo();

    public static final String CBS_ADDRESS = "$cbs";
    public static final String PUT_TOKEN_OPERATION = "operation";
    public static final String PUT_TOKEN_OPERATION_VALUE = "put-token";
    public static final String PUT_TOKEN_TYPE = "type";
    public static final String SAS_TOKEN_TYPE = "servicebus.windows.net:sastoken";
    public static final String PUT_TOKEN_AUDIENCE = "name";
    public static final String PUT_TOKEN_EXPIRY = "expiration";
    public static final String PUT_TOKEN_STATUS_CODE = "status-code";
    public static final String PUT_TOKEN_STATUS_DESCRIPTION = "status-description";

    public static final String MANAGEMENT_ADDRESS = "$management";
    public static final String MANAGEMENT_EVENTHUB_ENTITY_TYPE = AmqpConstants.VENDOR + ":eventhub";
    public static final String MANAGEMENT_PARTITION_ENTITY_TYPE = AmqpConstants.VENDOR + ":partition";
    public static final String MANAGEMENT_OPERATION_KEY = "operation";
    public static final String READ_OPERATION_VALUE = "READ";
    public static final String MANAGEMENT_ENTITY_TYPE_KEY = "type";
    public static final String MANAGEMENT_ENTITY_NAME_KEY = "name";
    public static final String MANAGEMENT_PARTITION_NAME_KEY = "partition";
    public static final String MANAGEMENT_SECURITY_TOKEN_KEY = "security_token";
    public static final String MANAGEMENT_RESULT_PARTITION_IDS = "partition_ids";
    public static final String MANAGEMENT_RESULT_PARTITION_COUNT = "partition_count";
    public static final String MANAGEMENT_RESULT_CREATED_AT = "created_at";
    public static final String MANAGEMENT_RESULT_BEGIN_SEQUENCE_NUMBER = "begin_sequence_number";
    public static final String MANAGEMENT_RESULT_LAST_ENQUEUED_SEQUENCE_NUMBER = "last_enqueued_sequence_number";
    public static final String MANAGEMENT_RESULT_LAST_ENQUEUED_OFFSET = "last_enqueued_offset";
    public static final String MANAGEMENT_RESULT_LAST_ENQUEUED_TIME_UTC = "last_enqueued_time_utc";
    public static final String MANAGEMENT_STATUS_CODE_KEY = "status-code";
    public static final String MANAGEMENT_STATUS_DESCRIPTION_KEY = "status-description";
    public static final String MANAGEMENT_RESPONSE_ERROR_CONDITION = "error-condition";

    public static final Symbol LAST_ENQUEUED_SEQUENCE_NUMBER = Symbol.valueOf(MANAGEMENT_RESULT_LAST_ENQUEUED_SEQUENCE_NUMBER);
    public static final Symbol LAST_ENQUEUED_OFFSET = Symbol.valueOf(MANAGEMENT_RESULT_LAST_ENQUEUED_OFFSET);
    public static final Symbol LAST_ENQUEUED_TIME_UTC = Symbol.valueOf(MANAGEMENT_RESULT_LAST_ENQUEUED_TIME_UTC);

    public static final String AMQP_PUT_TOKEN_FAILED_ERROR = "Put token failed. status-code: %s, status-description: %s";
    public static final String TOKEN_AUDIENCE_FORMAT = "amqp://%s/%s";

    public static final int MAX_RECEIVER_NAME_LENGTH = 64;

    private static String getPlatformInfo() {
        final StringBuilder platformInfo = new StringBuilder();
        platformInfo.append("arch:");
        platformInfo.append(System.getProperty("os.arch"));
        platformInfo.append(";os:");
        platformInfo.append(System.getProperty("os.name"));
        platformInfo.append(";os version:");
        platformInfo.append(System.getProperty("os.version"));

        return platformInfo.toString();
    }

    private static String getFrameworkInfo() {
        final Package javaRuntimeClassPkg = Runtime.class.getPackage();
        final StringBuilder frameworkInfo = new StringBuilder();
        frameworkInfo.append("jre:");
        frameworkInfo.append(javaRuntimeClassPkg.getImplementationVersion());
        frameworkInfo.append(";vendor:");
        frameworkInfo.append(javaRuntimeClassPkg.getImplementationVendor());
        frameworkInfo.append(";jvm");
        frameworkInfo.append(System.getProperty("java.vm.version"));

        return frameworkInfo.toString();
    }
}
