/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendLinkHandler extends BaseLinkHandler
{
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(SendLinkHandler.class);
    
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
				TRACE_LOGGER.debug("onLinkRemoteOpen: linkName:{}, remoteTarge:{}", sender.getName(), link.getRemoteTarget());

				synchronized (this.firstFlow)
				{
					this.isFirstFlow = false;
					this.msgSender.onOpenComplete(null);
				}
			}
			else
			{				
				TRACE_LOGGER.debug("onLinkRemoteOpen: linkName:{}, remoteTarget:{}, remoteSource:{}, action:{}", sender.getName(), null, null, "waitingForError");
			}
		}
	}

	@Override
	public void onDelivery(Event event)
	{		
		Delivery delivery = event.getDelivery();
		
		while (delivery != null)
		{
			Sender sender = (Sender) delivery.getLink();			
			
			TRACE_LOGGER.debug("onDelivery: linkName:{}, unsettled:{}, credit:{}, deliveryState:{}, delivery.isBuffered:{}, delivery.tag:{}",
			        sender.getName(), sender.getUnsettled(), sender.getRemoteCredit(), delivery.getRemoteState(), delivery.isBuffered(), delivery.getTag());
			
			msgSender.onSendComplete(delivery);
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

		Sender sender = event.getSender();
		this.msgSender.onFlow(sender.getRemoteCredit());

		TRACE_LOGGER.debug("onLinkFlow: linkName:{}, unsettled:{}, credit:{}", sender.getName(), sender.getUnsettled(), sender.getCredit());
	}
}
