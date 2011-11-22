package com.microsoft.windowsazure.services.serviceBus.models;

/**
 * Represents the result of a <code>getQueue</code> operation.
 */
public class GetQueueResult {

	private QueueInfo value;

	/**
	 * Creates an instance of the <code>GetQueueResult</code> class.
	 * 
	 * @param value
	 *            A {@link QueueInfo} object assigned as the value of the result.
	 */
	public GetQueueResult(QueueInfo value) {
		this.setValue(value);
	}

	/**
	 * Specfies the value of the result.
	 * 
	 * @param value
	 *            A {@link QueueInfo} object assigned as the value of the result.
	 */
	public void setValue(QueueInfo value) {
		this.value = value;
	}

	/**
	 * Returns the value of the result.
	 * 
	 * @return A {@link QueueInfo} object that represents the value of the result.
	 */
	public QueueInfo getValue() {
		return value;
	}

}
