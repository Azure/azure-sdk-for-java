package com.microsoft.azure.eventhubs.Exceptions;

import com.microsoft.azure.eventhubs.common.RetryPolicy;

public class InternalServerErrorException extends EventHubException {
	private static final long serialVersionUID = -1106827749824999989L;

	@Override public boolean getIsTransient(){
		// 	basically tell the client to try after sometime
		return true;
	  }
}
