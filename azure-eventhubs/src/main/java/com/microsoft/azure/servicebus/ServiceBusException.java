/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.util.Locale;

public class ServiceBusException extends Exception
{
	  private static final long serialVersionUID = -3654294093967132325L;
	  
	  private boolean isTransient;
	  private ErrorContext errorContext;
	  
	  ServiceBusException(final boolean isTransient)
	  {
	    super();
	    this.isTransient = isTransient;
	  }
	
	  ServiceBusException(final boolean isTransient, final String message)
	  {
	    super(message);
	    this.isTransient = isTransient;
	  }
	
	  public ServiceBusException(final boolean isTransient, final Throwable cause)
	  {
	    super(cause);
	    this.isTransient = isTransient;
	  }
	
	  ServiceBusException(final boolean isTransient, final String message, final Throwable cause)
	  {
	    super(message, cause);
	    this.isTransient = isTransient;
	  }
	  
	  @Override
	  public String getMessage()
	  {
		  final String baseMessage = super.getMessage();
		  return this.errorContext == null || StringUtil.isNullOrEmpty(this.errorContext.toString())
				  	? baseMessage
					: (!StringUtil.isNullOrEmpty(baseMessage) 
						? String.format(Locale.US, "%s, %s[%s]", baseMessage, "errorContext", this.errorContext.toString())
						: String.format(Locale.US, "%s[%s]", "errorContext", this.errorContext.toString()));
	  }
	  
	  /**
	   * A boolean indicating if the exception is a transient error or not.
	   * @return returns true when user can retry the operation that generated the exception without additional intervention.
	   */
	  public boolean getIsTransient()
	  {
		  return this.isTransient;
	  }
	  
	  public ErrorContext getContext()
	  {
		  return this.errorContext;
	  }
	  
	  void setContext(ErrorContext errorContext)
	  {
		  this.errorContext = errorContext;
	  }
}
