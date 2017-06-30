package com.microsoft.azure.servicebus.rules;

/**
 * Represents a special kind of filter that matches every message.
 * @since 1.0
 *
 */
public class TrueFilter extends SqlFilter
{
	private static final String TRUE_FILTER_EXPRESSION = "1=1";
	
	/**
	 * A true filter object that is pre-created. Clients can use this object instead of recreating a new instance every time.
	 */
	public static final TrueFilter DEFAULT = new TrueFilter();
	
	/**
	 * Creates a true filter.
	 */
	public TrueFilter()
	{
		super(TRUE_FILTER_EXPRESSION);
	}
}
