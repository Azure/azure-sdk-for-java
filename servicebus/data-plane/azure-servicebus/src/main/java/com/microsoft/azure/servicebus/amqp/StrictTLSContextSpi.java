package com.microsoft.azure.servicebus.amqp;

import java.security.KeyManagementException;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

// Wraps over a standard SSL context and disables the SSLv2Hello protocol.
public class StrictTLSContextSpi extends SSLContextSpi{

	private static final String SSLv2Hello = "SSLv2Hello";
	
	SSLContext innerContext;
	public StrictTLSContextSpi(SSLContext innerContext) {
		this.innerContext = innerContext;
	}
	
	@Override
	protected SSLEngine engineCreateSSLEngine() {
		SSLEngine engine = this.innerContext.createSSLEngine();
		this.removeSSLv2Hello(engine);
		return engine;
	}

	@Override
	protected SSLEngine engineCreateSSLEngine(String arg0, int arg1) {
		SSLEngine engine = this.innerContext.createSSLEngine(arg0, arg1);
		this.removeSSLv2Hello(engine);
		return engine;
	}

	@Override
	protected SSLSessionContext engineGetClientSessionContext() {
		return innerContext.getClientSessionContext();
	}

	@Override
	protected SSLSessionContext engineGetServerSessionContext() {
		return this.innerContext.getServerSessionContext();
	}

	@Override
	protected SSLServerSocketFactory engineGetServerSocketFactory() {
		return this.innerContext.getServerSocketFactory();
	}

	@Override
	protected SSLSocketFactory engineGetSocketFactory() {
		return this.innerContext.getSocketFactory();
	}

	@Override
	protected void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom sr) throws KeyManagementException {
		this.innerContext.init(km, tm, sr);		
	}
	
	private void removeSSLv2Hello(SSLEngine engine)
	{
		String[] enabledProtocols = engine.getEnabledProtocols();
		boolean sslv2HelloFound = false;
		for(String protocol : enabledProtocols)
		{
			if(protocol.equalsIgnoreCase(SSLv2Hello))
			{
				sslv2HelloFound = true;
				break;
			}
		}
		
		if(sslv2HelloFound)
		{
			ArrayList<String> modifiedProtocols = new ArrayList<String>();
			for(String protocol : enabledProtocols)
			{
				if(!protocol.equalsIgnoreCase(SSLv2Hello))
				{
					modifiedProtocols.add(protocol);
				}
			}
			
			engine.setEnabledProtocols(modifiedProtocols.toArray(new String[modifiedProtocols.size()]));
		}
		
		
	}
}
