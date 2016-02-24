package com.microsoft.azure.servicebus;

import java.util.Locale;

public class ReceiverContext extends ErrorContext
{
	final static boolean EPOCH_RECEIVER_TYPE = true;
	final static boolean NON_EPOCH_RECEIVER_TYPE = !ReceiverContext.EPOCH_RECEIVER_TYPE;
	
	final String receivePath;
	final String referenceId;
	final Long lastReceivedOffset;
	final Integer prefetchCount;
	final Integer currentLinkCredit;
	final Integer prefetchQueueLength;
	final Boolean receiverType;
	
	ReceiverContext(
			final String namespaceName, 
			final String receivePath, 
			final String referenceId,
			final Long lastReceivedOffset,
			final Integer prefetchCount,
			final Integer currentLinkCredit,
			final Integer prefetchQueueLength,
			final Boolean receiverType)
	{
		super(namespaceName);
		this.receivePath = receivePath;
		this.referenceId = referenceId;
		this.lastReceivedOffset = lastReceivedOffset;
		this.prefetchCount = prefetchCount;
		this.currentLinkCredit = currentLinkCredit;
		this.prefetchQueueLength = prefetchQueueLength;
		this.receiverType = receiverType;
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
		
		if (this.lastReceivedOffset != null)
		{
			toString.append(String.format(Locale.US, "LAST_OFFSET: %s", this.lastReceivedOffset));
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
		
		if (this.receiverType != null)
		{
			toString.append(String.format(Locale.US, "R_TYPE: %s", this.receiverType ? "EPOCH" : "NON_EPOCH"));
			toString.append(", ");
		}
		
		if (toString.length() > 2)
		{
			toString.setLength(toString.length() - 2);
		}
		
		return toString.toString();
	}
}
