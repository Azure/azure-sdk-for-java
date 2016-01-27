package com.microsoft.azure.servicebus;

public class AuthorizationFailedException extends ServiceBusException {
	private static final long serialVersionUID = -1106827749824999989L;
	
	public AuthorizationFailedException(String description) {
		super(description);
	}
	
	@Override 
	public boolean getIsTransient(){
		  return false;
	  }
}
