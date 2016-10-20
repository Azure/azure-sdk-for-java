/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.util.Locale;

public class SenderContext extends ErrorContext
{
	final String sendPath;
	final String referenceId;
	final Integer currentLinkCredit;

	SenderContext(
			final String namespaceName, 
			final String sendPath, 
			final String referenceId,
			final Integer currentLinkCredit)
	{
		super(namespaceName);

		this.sendPath = sendPath;
		this.referenceId = referenceId;
		this.currentLinkCredit = currentLinkCredit;
	}

	@Override
	public String toString()
	{
		final String superString = super.toString();
		StringBuilder toString = new StringBuilder();

		if (!StringUtil.isNullOrEmpty(superString))
		{
			toString.append(superString);
			toString.append(", ");
		}

		if (this.sendPath != null)
		{
			toString.append(String.format(Locale.US, "PATH: %s", this.sendPath));
			toString.append(", ");
		}

		if (this.referenceId != null)
		{
			toString.append(String.format(Locale.US, "REFERENCE_ID: %s", this.referenceId));
			toString.append(", ");
		}

		if (this.currentLinkCredit != null)
		{
			toString.append(String.format(Locale.US, "LINK_CREDIT: %s", this.currentLinkCredit));
			toString.append(", ");
		}

		if (toString.length() > 2)
		{
			toString.setLength(toString.length() - 2);
		}

		return toString.toString();
	}
}
