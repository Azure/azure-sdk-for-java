/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.reactor.Reactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.primitives.ClientConstants;

public class ReactorHandler extends BaseHandler
{
	private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ReactorHandler.class);

	@Override
	public void onReactorInit(Event e)
	{		
		TRACE_LOGGER.debug("reactor.onReactorInit");

		final Reactor reactor = e.getReactor();
		reactor.setTimeout(ClientConstants.REACTOR_IO_POLL_TIMEOUT);
	}

	@Override public void onReactorFinal(Event e)
	{		
		TRACE_LOGGER.debug("reactor.onReactorFinal");
	}
}
