package com.microsoft.azure.servicebus;

public class ReceiverDisconnectedException extends ServiceBusException {

	public ReceiverDisconnectedException(final String description) {
		super(description);
	}

	@Override
	public boolean getIsTransient() {
		return false;
	}

}
