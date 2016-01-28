package com.microsoft.azure.eventhubs.lib;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.*;
import org.apache.qpid.proton.driver.*;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.reactor.*;

/**
 * Mock Server (Singleton) designed to test AMQP related features in the javaClient
 */
public class MockServer implements Closeable
{
	private static final Logger TRACE_LOGGER = Logger.getLogger("servicebus.test.trace");

	public final static String HostName = "127.0.0.1";
	public final static int Port = 5671;
	
	public static Reactor reactor;
	
	private static MockServer server;
	private Acceptor acceptor;
	
	private MockServer(Reactor reactor, BaseHandler handler) throws IOException
	{
		MockServer.reactor = (reactor == null) ? Proton.reactor() : reactor;
		
		if (reactor == null)
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					if(TRACE_LOGGER.isLoggable(Level.FINE))
				    {
						TRACE_LOGGER.log(Level.FINE, "starting reactor instance.");
				    }
					
					MockServer.reactor.run();
				}
			}).start();
		}
		
		this.acceptor = MockServer.reactor.acceptor(MockServer.HostName, MockServer.Port, 
				handler == null ? new ServerTraceHandler() : handler);
		
	}

	public static MockServer Create(Reactor reactor, BaseHandler handler) throws IOException
	{
		if (MockServer.server == null)
		{
			MockServer.server = new MockServer(reactor, handler);
		}
		
		return MockServer.server;
	}

	@Override
	public void close() throws IOException
	{
		if (MockServer.reactor != null)
		{
			MockServer.reactor.free();
			MockServer.reactor = null;
		}
	}
}
