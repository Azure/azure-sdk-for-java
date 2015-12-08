package com.microsoft.azure.eventhubs.Exceptions;

import com.microsoft.azure.eventhubs.common.RetryPolicy;

public class AuthorizationFailedException extends EventHubException {
	private static final long serialVersionUID = -1106827749824999989L;
	
	@Override 
	public boolean getIsTransient(){
		  return false;
	  }
}
