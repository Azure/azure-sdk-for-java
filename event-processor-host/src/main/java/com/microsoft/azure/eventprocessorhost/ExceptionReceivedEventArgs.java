/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

/**
 * Passed as an argument to the general exception handler that can be set via EventProcessorOptions. 
 *
 */
public final class ExceptionReceivedEventArgs
{
	private final String hostname;
	private final Exception exception;
	private final String action;
	
	ExceptionReceivedEventArgs(String hostname, Exception exception, String action)
	{
		this.hostname = hostname;
		this.exception = exception;
		this.action = action;
	}
	
	/**
	 * Allows distinguishing the error source if multiple hosts in a single process. 
	 * 
	 * @return The name of the host that experienced the exception.
	 */
	public String getHostname()
	{
		return this.hostname;
	}
	
	/**
	 * 
	 * @return	The exception that was thrown.
	 */
	public Exception getException()
	{
		return this.exception;
	}
	
	/**
	 * See EventProcessorHostActionString for a list of possible values.
	 * 
	 * @return  A short string that indicates what general activity threw the exception.
	 */
	public String getAction()
	{
		return this.action;
	}
}
