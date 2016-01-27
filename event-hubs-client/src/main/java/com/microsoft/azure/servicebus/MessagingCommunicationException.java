package com.microsoft.azure.servicebus;

public class MessagingCommunicationException extends ServiceBusException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8148692364386204335L;

	@Override
	public boolean getIsTransient() {
		// TODO Auto-generated method stub
		return true;
	}
}
