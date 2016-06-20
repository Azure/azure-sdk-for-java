/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

// This class is never thrown into user code, so it can be package private.
class ExceptionWithAction extends Exception
{
	private static final long serialVersionUID = 7480590197418857145L;
	
	private final String action;

	// This class is only ever instantiated to wrap an already existing exception with an action string,
	// so this is the only constructor needed.
	ExceptionWithAction(Throwable e, String action)
	{
		super(e);
		this.action = action;
	}
	
	String getAction()
	{
		return this.action;
	}
}
