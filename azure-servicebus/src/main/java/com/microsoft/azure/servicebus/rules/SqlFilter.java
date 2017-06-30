package com.microsoft.azure.servicebus.rules;

/**
 * Represents a SQL language-based filter expression that is evaluated against a message.
 * @since 1.0
 *
 */
public class SqlFilter extends Filter {

	private String sqlExpression;
	
	/**
	 * Creates an instance of <code>SqlFilter</code> with the given match expression.
	 * @param sqlExpression SQL language-based filter expression
	 */
	public SqlFilter(String sqlExpression)
	{
		this.sqlExpression = sqlExpression;
	}
	
	/**
	 * Gets the match expression of this filter.
	 * @return SQL language-based expression of this filter
	 */
	public String getSqlExpression()
	{
		return this.sqlExpression;
	}
}
