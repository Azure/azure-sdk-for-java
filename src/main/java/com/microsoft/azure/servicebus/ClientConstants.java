package com.microsoft.azure.servicebus;

import org.apache.qpid.proton.amqp.*;

public final class ClientConstants {

	private ClientConstants() { }

	public final static Symbol ServerBusyError = Symbol.getSymbol(AmqpConstants.Vendor + ":server-busy");
}
