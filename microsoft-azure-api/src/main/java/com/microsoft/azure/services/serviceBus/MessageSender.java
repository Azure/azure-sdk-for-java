package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.services.serviceBus.contract.BrokeredMessage;

public interface MessageSender {
	void send(BrokeredMessage message);
}
