/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
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

	public final static Duration TIMER_TOLERANCE = Duration.ofSeconds(1);
	
	public final static Duration DEFAULT_RERTRY_MIN_BACKOFF = Duration.ofSeconds(0);
	public final static Duration DEFAULT_RERTRY_MAX_BACKOFF = Duration.ofSeconds(30);
	
	public final static int DEFAULT_MAX_RETRY_COUNT = 10;
	
	public final static String SERVICEBUS_CLIENT_TRACE = "servicebus.trace";
	
	public final static boolean DEFAULT_IS_TRANSIENT = true;
}
