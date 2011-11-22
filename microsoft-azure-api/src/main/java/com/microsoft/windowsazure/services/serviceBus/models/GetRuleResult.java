package com.microsoft.windowsazure.services.serviceBus.models;

/**
 * Represents the result of a <code>getRule</code> operation.
 */
public class GetRuleResult {

	private RuleInfo value;

	/**
	 * Creates an instance of the <code>GetRuleResult</code> class.
	 * 
	 * @param value
	 *            A {@link RuleInfo} object assigned as the value of the result.
	 */
	public GetRuleResult(RuleInfo value) {
		this.setValue(value);
	}

	/**
	 * Specfies the value of the result.
	 * 
	 * @param value
	 *            A {@link RuleInfo} object assigned as the value of the result.
	 */
	public void setValue(RuleInfo value) {
		this.value = value;
	}

	/**
	 * Returns the value of the result.
	 * 
	 * @return A {@link RuleInfo} object that represents the value of the result.
	 */
	public RuleInfo getValue() {
		return value;
	}

}
