/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.time.Duration;
import org.apache.qpid.proton.amqp.Symbol;

import com.microsoft.azure.servicebus.amqp.AmqpConstants;

public final class ClientConstants
{
	private ClientConstants() { }

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

	public final static Duration DEFAULT_RERTRY_MIN_BACKOFF = Duration.ofSeconds(0);
	public final static Duration DEFAULT_RERTRY_MAX_BACKOFF = Duration.ofSeconds(30);

	public final static int DEFAULT_MAX_RETRY_COUNT = 10;

	public final static String SERVICEBUS_CLIENT_TRACE = "servicebus.trace";

	public final static boolean DEFAULT_IS_TRANSIENT = true;

	public final static int REACTOR_IO_POLL_TIMEOUT = 20;
	public final static int SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS = 4;

	public final static String NO_RETRY = "NoRetry";
	public final static String DEFAULT_RETRY = "Default";
	
	public final static String PRODUCT_NAME = "MSJavaClient";
	public final static String CURRENT_JAVACLIENT_VERSION = "0.10.0-SNAPSHOT";

	public static final String PLATFORM_INFO = getPlatformInfo();
        
        public static final String MANAGEMENT_ADDRESS = "$management";
        public static final String MANAGEMENT_EVENTHUB_ENTITY_TYPE = AmqpConstants.VENDOR + ":eventhub";
        public static final String MANAGEMENT_PARTITION_ENTITY_TYPE = AmqpConstants.VENDOR + ":partition";
        public static final String MANAGEMENT_STATUS_CODE_KEY = "status-code";
        public static final String MANAGEMENT_STATUS_DESCRIPTION_KEY = "status-description";
        public static final String MANAGEMENT_OPERATION_KEY = "operation";
        public static final String READ_OPERATION_VALUE = "READ";
        public static final String MANAGEMENT_ENTITY_TYPE_KEY = "type";
        public static final String MANAGEMENT_ENTITY_NAME_KEY = "name";
        public static final String MANAGEMENT_PARTITION_NAME_KEY = "partition";
        public static final String MANAGEMENT_SECURITY_TOKEN_KEY = "security_token";
        public static final String MANAGEMENT_RESPONSE_ERROR_CONDITION = "error-condition";
        public static final String MANAGEMENT_RESULT_PARTITION_IDS = "partition_ids";
        public static final String MANAGEMENT_RESULT_PARTITION_COUNT = "partition_count";
        public static final String MANAGEMENT_RESULT_BEGIN_SEQUENCE_NUMBER = "begin_sequence_number";
        public static final String MANAGEMENT_RESULT_LAST_ENQUEUED_SEQUENCE_NUMBER = "last_enqueued_sequence_number";
        public static final String MANAGEMENT_RESULT_LAST_ENQUEUED_OFFSET = "last_enqueued_offset";
        public static final String MANAGEMENT_RESULT_LAST_ENQUEUED_TIME_UTC = "last_enqueued_time_utc";

	private static String getPlatformInfo()
	{
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
