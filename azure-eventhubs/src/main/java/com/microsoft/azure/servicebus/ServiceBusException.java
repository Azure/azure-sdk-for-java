/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
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
	
	  ServiceBusException(final boolean isTransient, final Throwable cause)
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
