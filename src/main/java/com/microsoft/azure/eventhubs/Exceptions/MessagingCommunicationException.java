package com.microsoft.azure.eventhubs.Exceptions;

import com.microsoft.azure.eventhubs.common.RetryPolicy;

public class MessagingCommunicationException extends EventHubException{

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
