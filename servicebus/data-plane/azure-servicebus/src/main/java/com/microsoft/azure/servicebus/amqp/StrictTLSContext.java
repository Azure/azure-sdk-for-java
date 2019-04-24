package com.microsoft.azure.servicebus.amqp;

import java.security.Provider;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;

// Customer SSLContext that wraps around an SSLContext and removes SSLv2Hello protocol from every SSLEngine created.
public class StrictTLSContext extends SSLContext{
	
	protected StrictTLSContext(SSLContextSpi contextSpi, Provider provider, String protocol) {
		super(contextSpi, provider, protocol);
	}
}
