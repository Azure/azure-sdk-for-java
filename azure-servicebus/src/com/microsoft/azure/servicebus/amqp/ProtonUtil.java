/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import java.io.IOException;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.reactor.Reactor;

public final class ProtonUtil
{
	private ProtonUtil()
	{
	}

	public static Reactor reactor(ReactorHandler reactorHandler) throws IOException
	{
		Reactor reactor = Proton.reactor(reactorHandler);
		reactor.setGlobalHandler(new CustomIOHandler());
		return reactor;
	}
}
