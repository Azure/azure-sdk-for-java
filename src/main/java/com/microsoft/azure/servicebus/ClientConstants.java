package com.microsoft.azure.servicebus;

import java.time.*;
import org.apache.qpid.proton.amqp.*;

public final class ClientConstants {

	private ClientConstants() { }

	public final static int AmqpsPort = 5671;
	
	public final static Symbol ServerBusyError = Symbol.getSymbol(AmqpConstants.Vendor + ":server-busy");
	public static final Symbol ArgumentError = Symbol.getSymbol(AmqpConstants.Vendor + ":argument-error");
	
	public static final Duration TimerTolerance = Duration.ofSeconds(5);
	
	public final static Duration DefaultRetryMinBackoff = Duration.ofSeconds(0);
	public final static Duration DefaultRetryMaxBackoff = Duration.ofSeconds(30);
	
	public final static int DefaultMaxRetryCount = 10;
	
	public final static String ServiceBusClientTrace = "servicebus.trace";
}
