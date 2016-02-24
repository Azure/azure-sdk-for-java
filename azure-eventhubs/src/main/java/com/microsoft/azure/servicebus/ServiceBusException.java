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

public abstract class ServiceBusException extends Exception
{
	  private static final long serialVersionUID = -3654294093967132325L;
	  
	  public ServiceBusException()
	  {
	    super();
	  }
	
	  public ServiceBusException(String message)
	  {
	    super(message);
	  }
	
	  public ServiceBusException(Throwable cause)
	  {
	    super(cause);
	  }
	
	  public ServiceBusException(String message, Throwable cause)
	  {
	    super(message, cause);
	  }
	  
	  public abstract boolean getIsTransient();
	  
	  /**
	   *  internal-only; Sub-classing is allowed for {@link ServiceBusException} 
	   *  - but fx's other than ServiceBus sdk's cannot directly create instance & throw it
	   */
	  static ServiceBusException create(final boolean isTransient, final String message)
	  {
		  return (ServiceBusException) new InternalServiceBusException(isTransient, message);
	  }
	  
	  static ServiceBusException create(final boolean isTransient, Throwable cause)
	  {
		  return (ServiceBusException) new InternalServiceBusException(isTransient, cause);
	  }
	  
	  static ServiceBusException create(final boolean isTransient, final String message, final Throwable cause)
	  {
		  return (ServiceBusException) new InternalServiceBusException(isTransient, message, cause);
	  }
	  
	  private static final class InternalServiceBusException extends ServiceBusException
	  {
		boolean isTransient;
		  
		private InternalServiceBusException(final boolean isTransient, final String message)
		{
		  super(message);
		  this.isTransient = isTransient;
		}
		  
		private InternalServiceBusException(final boolean isTransient, final Throwable exception)
		{
			super(exception);
			this.isTransient = isTransient;
		}
		
		private InternalServiceBusException(final boolean isTransient, final String message, final Throwable cause)
		{
			super(message, cause);
			this.isTransient = isTransient;
		}
		  
		@Override
		public boolean getIsTransient()
		{
			return this.isTransient;
		}		  
	  }
}
