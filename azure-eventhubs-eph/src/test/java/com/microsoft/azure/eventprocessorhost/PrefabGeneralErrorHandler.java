/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.ArrayList;
import java.util.function.Consumer;

public class PrefabGeneralErrorHandler implements Consumer<ExceptionReceivedEventArgs>
{
	private ArrayList<String> errors = new ArrayList<String>();

	ArrayList<String> getErrors()
	{
		return this.errors;
	}
	
	int getErrorCount()
	{
		return this.errors.size();
	}
	
	@Override
	public void accept(ExceptionReceivedEventArgs e)
	{
		this.errors.add("GENERAL: " + e.getHostname() + " " + e.getAction() + " " + e.getException().toString() + " " + e.getException().getMessage());
	}
}
