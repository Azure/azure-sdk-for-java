package com.microsoft.azure.servicebus.rules;

public class FalseFilter extends SqlFilter
{
	private static final String FALSE_FILTER_EXPRESSION = "1=0";
	public static final FalseFilter DEFAULT = new FalseFilter();
	public FalseFilter() {
		super(FALSE_FILTER_EXPRESSION);
	}
}
