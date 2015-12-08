package com.microsoft.azure.servicebus;

public class OperationTimeoutException extends ServiceBusException {
	private static final long serialVersionUID = -1106827749824999989L;

	@Override
	public boolean getIsTransient() {
		// TODO Auto-generated method stub
		return true;
	}

}
