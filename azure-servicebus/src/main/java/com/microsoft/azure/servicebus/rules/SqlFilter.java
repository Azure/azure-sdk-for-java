package com.microsoft.azure.servicebus.rules;

import com.microsoft.azure.servicebus.Message;

public class SqlFilter extends Filter {

	private String sqlExpression;
	
	public SqlFilter(String sqlExpression)
	{
		this.sqlExpression = sqlExpression;
	}
	
	public String getSqlExpression()
	{
		return this.sqlExpression;
	}
}
