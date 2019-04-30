/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.primitives;

import java.util.Locale;

public class ReceiverErrorContext extends ErrorContext
{
	final static boolean EPOCH_RECEIVER_TYPE = true;
	final static boolean NON_EPOCH_RECEIVER_TYPE = !ReceiverErrorContext.EPOCH_RECEIVER_TYPE;

	final String receivePath;
	final String referenceId;
	final Integer prefetchCount;
	final Integer currentLinkCredit;
	final Integer prefetchQueueLength;

	ReceiverErrorContext(
			final String namespaceName, 
			final String receivePath, 
			final String referenceId,
			final Integer prefetchCount,
			final Integer currentLinkCredit,
			final Integer prefetchQueueLength)
	{
		super(namespaceName);
		this.receivePath = receivePath;
		this.referenceId = referenceId;
		this.prefetchCount = prefetchCount;
		this.currentLinkCredit = currentLinkCredit;
		this.prefetchQueueLength = prefetchQueueLength;
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

		if (this.receivePath != null)
		{
			toString.append(String.format(Locale.US, "PATH: %s", this.receivePath));
			toString.append(", ");
		}

		if (this.referenceId != null)
		{
			toString.append(String.format(Locale.US, "REFERENCE_ID: %s", this.referenceId));
			toString.append(", ");
		}		

		if (this.prefetchCount != null)
		{
			toString.append(String.format(Locale.US, "PREFETCH_COUNT: %s", this.prefetchCount));
			toString.append(", ");
		}

		if (this.currentLinkCredit != null)
		{
			toString.append(String.format(Locale.US, "LINK_CREDIT: %s", this.currentLinkCredit));
			toString.append(", ");
		}

		if (this.prefetchQueueLength != null)
		{
			toString.append(String.format(Locale.US, "PREFETCH_Q_LEN: %s", this.prefetchQueueLength));
			toString.append(", ");
		}		

		if (toString.length() > 2)
		{
			toString.setLength(toString.length() - 2);
		}

		return toString.toString();
	}
}
