package com.microsoft.azure.servicebus.rules;

public class TrueFilter extends SqlFilter
{
	private static final String TRUE_FILTER_EXPRESSION = "1=1";
	public static final TrueFilter DEFAULT = new TrueFilter();
	public TrueFilter()
	{
		super(TRUE_FILTER_EXPRESSION);
	}
}
