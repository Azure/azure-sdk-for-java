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
	public final static String HostName = "127.0.0.1";
	public final static int Port = 5671;
	
	private Reactor reactor;
	private Acceptor acceptor;
	
	private MockServer(BaseHandler handler) throws IOException, InterruptedException
	{
		this.reactor = Proton.reactor();
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				if(TestBase.TEST_LOGGER.isLoggable(Level.FINE))
			    {
					TestBase.TEST_LOGGER.log(Level.FINE, "starting reactor instance.");
			    }
				
				reactor.run();
			}
		}).start();
		
		this.acceptor = this.reactor.acceptor(MockServer.HostName, MockServer.Port, 
				handler == null ? new ServerTraceHandler() : handler);
	}

	public static MockServer Create(BaseHandler handler) throws IOException, InterruptedException
	{
		MockServer server = new MockServer(handler);
		return server;
	}

	@Override
	public void close() throws IOException
	{
		if (this.acceptor != null)
		{
			this.acceptor.close();
		}
	}
}
