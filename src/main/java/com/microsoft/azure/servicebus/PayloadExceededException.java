package com.microsoft.azure.servicebus;

public class PayloadExceededException extends ServiceBusException {

	private static final long serialVersionUID = -7474944260042213247L;

	@Override
	public boolean getIsTransient() {
		// TODO Auto-generated method stub
		return false;
	}

}
