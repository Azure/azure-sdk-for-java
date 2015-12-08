package com.microsoft.azure.servicebus;

public class InternalServerErrorException extends ServiceBusException {
	private static final long serialVersionUID = -1106827749824999989L;

	@Override public boolean getIsTransient(){
		// 	basically tell the client to try after sometime
		return true;
	  }
}
