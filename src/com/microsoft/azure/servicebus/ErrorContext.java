/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.util.Locale;

public abstract class ErrorContext
{
	private final String namespaceName;

	ErrorContext(final String namespaceName)
	{
		this.namespaceName = namespaceName;
	}

	protected String getNamespaceName()
	{
		return this.namespaceName;
	}

	@Override
	public String toString()
	{
		return StringUtil.isNullOrEmpty(this.namespaceName) ? null : String.format(Locale.US, "NS: %s", this.namespaceName);
	}
}
