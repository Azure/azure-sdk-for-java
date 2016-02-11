package com.microsoft.azure.servicebus;

public final class TrackingUtil
{
	public static final String TRACKING_ID_TOKEN_SEPARATOR = "_"; 
	
	private TrackingUtil()
	{
	}
	
	/**
	 * parses ServiceBus role identifiers from trackingId  
	 * @return null if no roleIdentifier found
	 */
	public static String parseRoleIdentifier(final String trackingId)
	{
		if (StringUtil.isNullOrWhiteSpace(trackingId) || !trackingId.contains(TRACKING_ID_TOKEN_SEPARATOR))
		{
			return null;
		}
		
		return trackingId.substring(trackingId.indexOf(TRACKING_ID_TOKEN_SEPARATOR));
	}
}
