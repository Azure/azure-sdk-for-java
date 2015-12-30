package com.microsoft.azure.servicebus;

import org.apache.qpid.proton.amqp.Symbol;

public final class AmqpErrorCode {

	public static final Symbol NotFound = Symbol.getSymbol("amqp:not-found");
	public static final Symbol UnauthorizedAccess = Symbol.getSymbol("amqp:unauthorized-access");
	public static final Symbol ResourceLimitExceeded = Symbol.getSymbol("amqp:resource-limit-exceeded");
	public static final Symbol NotAllowed = Symbol.getSymbol("amqp:not-allowed");
	public static final Symbol InternalError = Symbol.getSymbol("amqp:internal-error");
	
	// link errors
	public static final Symbol Stolen = Symbol.getSymbol("amqp:link:stolen");
	
}
