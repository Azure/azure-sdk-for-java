/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ServiceBus <-> ProtonReactor interaction 
// handles all recvLink - reactor events
public final class ReceiveLinkHandler extends BaseLinkHandler
{
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ReceiveLinkHandler.class);
    
	private final IAmqpReceiver amqpReceiver;
	private final Object firstResponse;
	private boolean isFirstResponse;

	public ReceiveLinkHandler(final IAmqpReceiver receiver)
	{
		super(receiver);

		this.amqpReceiver = receiver;
		this.firstResponse = new Object();
		this.isFirstResponse = true;
	}

	@Override
	public void onLinkLocalOpen(Event evt)
	{
		Link link = evt.getLink();
		if (link instanceof Receiver)
		{
			Receiver receiver = (Receiver) link;			
			TRACE_LOGGER.debug("onLinkLocalOpen: linkName:{}, localSource:{}", receiver.getName(), receiver.getSource());
		}
	}

	@Override
	public void onLinkRemoteOpen(Event event)
	{
		Link link = event.getLink();
		if (link != null && link instanceof Receiver)
		{
			Receiver receiver = (Receiver) link;
			if (link.getRemoteSource() != null)
			{				
				TRACE_LOGGER.debug("onLinkRemoteOpen: linkName:{}, remoteSource:{}", receiver.getName(), receiver.getRemoteSource());

				synchronized (this.firstResponse)
				{
					this.isFirstResponse = false;
					this.amqpReceiver.onOpenComplete(null);
				}
			}
			else
			{				
				TRACE_LOGGER.debug("onLinkRemoteOpen: linkName:{}, remoteTarget:{}, remoteTarget:{}, action:{}", receiver.getName(), null, null, "waitingForError");
			}
		}
	}

	@Override
	public void onDelivery(Event event)
	{
		synchronized (this.firstResponse)
		{
			if (this.isFirstResponse)
			{
				this.isFirstResponse = false;
				this.amqpReceiver.onOpenComplete(null);
			}
		}

		Delivery delivery = event.getDelivery();
		Receiver receiveLink = (Receiver) delivery.getLink();

		TRACE_LOGGER.debug("onDelivery: linkName:{}, updatedLinkCredit:{}, remoteCredit:{}, remoteCondition:{}, delivery.isPartial:{}", 
                receiveLink.getName(), receiveLink.getCredit(), receiveLink.getRemoteCredit(), receiveLink.getRemoteCondition(), delivery.isPartial());
		
		//TODO: What happens when a delivery has no message, but only disposition from the remote link? Like when ServiceBus service sends just a disposition to the receiver?"
		
		// If a message spans across deliveries (for ex: 200k message will be 4 frames (deliveries) 64k 64k 64k 8k), 
		// all until "last-1" deliveries will be partial
		// reactor will raise onDelivery event for all of these - we only need the last one
		if (!delivery.isPartial())
		{	
			this.amqpReceiver.onReceiveComplete(delivery);
		}
	}
}
