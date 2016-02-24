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
package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.amqp.Symbol;

public final class AmqpErrorCode
{

	public static final Symbol NotFound = Symbol.getSymbol("amqp:not-found");
	public static final Symbol UnauthorizedAccess = Symbol.getSymbol("amqp:unauthorized-access");
	public static final Symbol ResourceLimitExceeded = Symbol.getSymbol("amqp:resource-limit-exceeded");
	public static final Symbol NotAllowed = Symbol.getSymbol("amqp:not-allowed");
	public static final Symbol InternalError = Symbol.getSymbol("amqp:internal-error");
	public static final Symbol IllegalState = Symbol.getSymbol("amqp:illegal-state");
	public static final Symbol NotImplemented = Symbol.getSymbol("amqp:not-implemented");
	
	// link errors
	public static final Symbol Stolen = Symbol.getSymbol("amqp:link:stolen");
	public static final Symbol PayloadSizeExceeded = Symbol.getSymbol("amqp:link:message-size-exceeded");
	public static final Symbol AmqpLinkDetachForced = Symbol.getSymbol("amqp:link:detach-forced");

	// connection errors
	public static final Symbol ConnectionForced = Symbol.getSymbol("amqp:connection:forced");
}
