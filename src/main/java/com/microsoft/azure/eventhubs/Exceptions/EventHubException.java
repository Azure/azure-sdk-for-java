package com.microsoft.azure.eventhubs.Exceptions;

import com.microsoft.azure.eventhubs.common.RetryPolicy;

public abstract class EventHubException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3654294093967132325L;

	  public EventHubException() {
	    super();
	  }

	  public EventHubException(String message) {
	    super(message);
	  }
	
	  public EventHubException(Throwable cause) {
	    super(cause);
	  }
	
	  public EventHubException(String message, Throwable cause) {
	    super(message, cause);
	  }
	  
	  public abstract boolean getIsTransient();
}
