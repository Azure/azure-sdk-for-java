/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package com.microsoft.azure.servicebus.amqp;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.amqp.transport.*;
import org.apache.qpid.proton.engine.*;

import com.microsoft.azure.servicebus.ClientConstants;

public class BaseLinkHandler extends BaseHandler
{
	protected static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
	
	private final IAmqpLink underlyingEntity;
	
	public BaseLinkHandler(final IAmqpLink amqpLink)
	{
		this.underlyingEntity = amqpLink;
	}
	
	@Override
	public void onLinkLocalClose(Event event)
	{
		Link link = event.getLink();
		if (link != null)
		{
			if(TRACE_LOGGER.isLoggable(Level.FINE))
            {
            	TRACE_LOGGER.log(Level.FINE,
            			String.format("linkName[%s]", link.getName()));
            }
		}
	}
		
	public void processOnClose(Link link, ErrorCondition condition)
	{
		if (condition != null)
		{
			if(TRACE_LOGGER.isLoggable(Level.FINE))
	        {
				TRACE_LOGGER.log(Level.FINE, "linkName[" + link.getName() +
						(condition != null ? "], ErrorCondition[" + condition.getCondition() + ", " + condition.getDescription() + "]" : "], condition[null]"));
	        }
        }
		
		link.close();
		this.underlyingEntity.onClose(condition);
	}
	
	public void processOnClose(Link link, Exception exception)
	{
		link.close();
		this.underlyingEntity.onError(exception);
	}
}
