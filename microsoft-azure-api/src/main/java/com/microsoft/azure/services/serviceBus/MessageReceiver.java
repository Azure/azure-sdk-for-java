package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.services.serviceBus.contract.BrokeredMessage;
import com.microsoft.azure.services.serviceBus.contract.ReceiveMode;

public interface MessageReceiver {
	BrokeredMessage receive(int timeout, ReceiveMode receiveMode);
	void abandon(BrokeredMessage message);
	void complete(BrokeredMessage message);
}
