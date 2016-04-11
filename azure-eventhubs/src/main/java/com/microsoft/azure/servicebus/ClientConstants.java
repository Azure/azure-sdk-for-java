/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.time.*;
import org.apache.qpid.proton.amqp.*;

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
}
