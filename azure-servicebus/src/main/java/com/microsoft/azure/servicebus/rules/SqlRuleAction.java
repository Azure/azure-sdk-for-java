package com.microsoft.azure.servicebus.rules;

public class SqlRuleAction extends RuleAction
{
	private String sqlExpression;
	
	public SqlRuleAction(String sqlExpression)
	{
		this.sqlExpression = sqlExpression;
	}
	
	public String getSqlExpression()
	{
		return this.sqlExpression;
	}
}
