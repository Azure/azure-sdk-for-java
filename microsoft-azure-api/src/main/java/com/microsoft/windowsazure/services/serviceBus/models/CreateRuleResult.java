package com.microsoft.windowsazure.services.serviceBus.models;

/**
 * Represents the result of a <code>createRule</code> operation.
 */
public class CreateRuleResult {

	private Rule value;

	/**
	 * Creates an instance of the <code>CreateRuleResult</code> class.
	 * 
	 * @param value
	 *            A {@link Rule} object assigned as the value of the result.
	 */
	public CreateRuleResult(Rule value) {
		this.setValue(value);
	}

	/**
	 * Specfies the value of the result.
	 * 
	 * @param value
	 *            A {@link Rule} object assigned as the value of the result.
	 */
	public void setValue(Rule value) {
		this.value = value;
	}

	/**
	 * Returns the value of the result.
	 * 
	 * @return A {@link Rule} object that represents the value of the result.
	 */
	public Rule getValue() {
		return value;
	}

}
