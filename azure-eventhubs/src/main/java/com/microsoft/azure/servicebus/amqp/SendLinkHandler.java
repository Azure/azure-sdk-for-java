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

import java.util.Locale;
import java.util.logging.*;

import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.*;

import com.microsoft.azure.servicebus.*;

public class SendLinkHandler extends BaseLinkHandler
{
	private final IAmqpSender msgSender;
	private final Object firstFlow;
	private boolean isFirstFlow;
	
	public SendLinkHandler(final IAmqpSender sender)
	{
		super(sender);
		
		this.msgSender = sender;
		this.firstFlow = new Object();
		this.isFirstFlow = true;
	}
	
	@Override
	public void onLinkRemoteOpen(Event event)
	{
		Link link = event.getLink();
        if (link != null && link instanceof Sender)
        {
        	Sender sender = (Sender) link;
        	if (link.getRemoteTarget() != null)
        	{
        		if(TRACE_LOGGER.isLoggable(Level.FINE))
                {
                	TRACE_LOGGER.log(Level.FINE, String.format(Locale.US, "linkName[%s], remoteTarget[%s]", sender.getName(), link.getRemoteTarget()));
                }
        		
        		synchronized (this.firstFlow)
        		{
					this.isFirstFlow = false;
	        		this.msgSender.onOpenComplete(null);
        		}
        	}
        	else
        	{
        		if(TRACE_LOGGER.isLoggable(Level.FINE))
                {
                	TRACE_LOGGER.log(Level.FINE,
                			String.format(Locale.US, "linkName[%s], remoteTarget[null], remoteSource[null], action[waitingForError]", sender.getName()));
                }
        	}
        }
	}

	@Override
    public void onDelivery(Event event)
	{		
		Delivery delivery = event.getDelivery();
		Sender sender = (Sender) delivery.getLink();
		if(TRACE_LOGGER.isLoggable(Level.FINEST))
        {
            TRACE_LOGGER.log(Level.FINEST, 
            		"linkName[" + sender.getName() + 
            		"], unsettled[" + sender.getUnsettled() + "], credit[" + sender.getCredit()+ "], deliveryState[" + delivery.getRemoteState() + 
            		"], delivery.isBuffered[" + delivery.isBuffered() +"], delivery.id[" + delivery.getTag() + "]");
        }
		
		while (delivery != null)
		{
			msgSender.onSendComplete(delivery.getTag(), delivery.getRemoteState());
			delivery.settle();
			delivery = sender.current();
		}
	}

	@Override
	public void onLinkFlow(Event event)
	{
		if (this.isFirstFlow)
		{
			synchronized (this.firstFlow)
			{
				if (this.isFirstFlow)
				{
					this.msgSender.onOpenComplete(null);
					this.isFirstFlow = false;
				}
			}
		}
		
		if(TRACE_LOGGER.isLoggable(Level.FINE))
        {
			Sender sender = (Sender) event.getLink();
			TRACE_LOGGER.log(Level.FINE, "linkName[" + sender.getName() + "], unsettled[" + sender.getUnsettled() + "], credit[" + sender.getCredit()+ "]");
        }
	}
	
	@Override
    public void onLinkRemoteClose(Event event)
	{
		Link link = event.getLink();
        if (link instanceof Sender)
        {
        	ErrorCondition condition = link.getRemoteCondition();
    		this.processOnClose(link, condition);
        }
	}
	
	@Override
	public void onLinkRemoteDetach(Event event)
	{
		this.onLinkRemoteClose(event);
	}
}
