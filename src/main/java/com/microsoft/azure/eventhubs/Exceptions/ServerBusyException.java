package com.microsoft.azure.eventhubs.Exceptions;

import com.microsoft.azure.eventhubs.common.RetryPolicy;

public class ServerBusyException extends EventHubException 
{
	private static final long serialVersionUID = -1106827749824999989L;

	// return what kind of ServerBusyException this is - Throttle or not
	// PLEASE DONOT START DISCUSS: DO WE WANT A SUBCLASS or ERRORCODE
	public int getErrorCode(){
		return 2;
	}
	
	@Override 
	public boolean getIsTransient(){
		  return true;
	  }
}
