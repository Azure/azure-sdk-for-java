package com.microsoft.windowsazure.services.serviceBus.models;

/**
 * Represents the result of a <code>createRule</code> operation.
 */
public class CreateRuleResult {

	private RuleInfo value;

	/**
	 * Creates an instance of the <code>CreateRuleResult</code> class.
	 * 
	 * @param value
	 *            A {@link RuleInfo} object assigned as the value of the result.
	 */
	public CreateRuleResult(RuleInfo value) {
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
