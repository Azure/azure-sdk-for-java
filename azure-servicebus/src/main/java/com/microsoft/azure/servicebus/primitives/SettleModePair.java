package com.microsoft.azure.servicebus.primitives;

import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;

public class SettleModePair {
	private final SenderSettleMode senderSettleMode;
	private final ReceiverSettleMode receiverSettleMode;
	
	public SettleModePair(SenderSettleMode senderSettleMode, ReceiverSettleMode receiverSettleMode)
	{
		this.senderSettleMode = senderSettleMode;
		this.receiverSettleMode = receiverSettleMode;
	}

	public SenderSettleMode getSenderSettleMode() {
		return senderSettleMode;
	}	

	public ReceiverSettleMode getReceiverSettleMode() {
		return receiverSettleMode;
	}
	
	@Override
	public String toString()
	{
	    return String.format("sender settle mode: %s, receiver settle mode: %s", this.senderSettleMode, this.receiverSettleMode);
	}
}
