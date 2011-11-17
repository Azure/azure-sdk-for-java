package com.microsoft.windowsazure.services.serviceBus.models;

/**
 * Represents the result of a <code>createTopic</code> operation.
 */
public class CreateTopicResult {

    private Topic value;

	/**
	 * Creates an instance of the <code>CreateTopicResult</code> class.
	 * 
	 * @param value
	 *            A {@link Topic} object assigned as the value of the result.
	 */    
    public CreateTopicResult(Topic value) {
        this.setValue(value);
    }

	/**
	 * Specfies the value of the result.
	 * 
	 * @return A {@link Topic} object assigned as the value of the result.
	 */    
    public void setValue(Topic value) {
        this.value = value;
    }

	/**
	 * Returns the value of the result.
	 * 
	 * @return A {@link Topic} object that represents the value of the result.
	 */
    public Topic getValue() {
        return value;
    }

}
