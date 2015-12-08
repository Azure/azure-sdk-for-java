package com.microsoft.azure.eventhubs.Exceptions;

import com.microsoft.azure.eventhubs.common.RetryPolicy;

public class PayloadExceededException extends EventHubException {

	private static final long serialVersionUID = -7474944260042213247L;

	@Override
	public boolean getIsTransient() {
		// TODO Auto-generated method stub
		return false;
	}

}
