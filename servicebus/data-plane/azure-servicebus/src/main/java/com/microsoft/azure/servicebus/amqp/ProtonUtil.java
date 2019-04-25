/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import java.io.IOException;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.ReactorOptions;

public final class ProtonUtil
{
	private ProtonUtil()
	{
	}

	public static Reactor reactor(ReactorHandler reactorHandler, final int maxFrameSize) throws IOException
	{
		final ReactorOptions reactorOptions = new ReactorOptions();
		reactorOptions.setMaxFrameSize(maxFrameSize);

		Reactor reactor = Proton.reactor(reactorOptions, reactorHandler);
		reactor.setGlobalHandler(new CustomIOHandler());
		reactor.getGlobalHandler().add(new LoggingHandler());
		return reactor;
	}
}
