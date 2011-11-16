package com.microsoft.windowsazure.services.serviceBus.models;

/**
 * Represents the result of a <code>getQueue</code> operation.
 */
public class GetQueueResult {

	private Queue value;

	/**
	 * Creates an instance of the <code>GetQueueResult</code> class.
	 * 
	 * @param value
	 *            A {@link Queue} object assigned as the value of the result.
	 */
	public GetQueueResult(Queue value) {
		this.setValue(value);
	}

	/**
	 * Specfies the value of the result.
	 * 
	 * @param value
	 *            A {@link Queue} object assigned as the value of the result.
	 */
	public void setValue(Queue value) {
		this.value = value;
	}

	/**
	 * Returns the value of the result.
	 * 
	 * @return A {@link Queue} object that represents the value of the result.
	 */
	public Queue getValue() {
		return value;
	}

}
