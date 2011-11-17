package com.microsoft.windowsazure.services.serviceBus.models;

/**
 * Represents the result of a <code>createSubscription</code> operation.
 */
public class CreateSubscriptionResult {

    private Subscription value;

	/**
	 * Creates an instance of the <code>CreateSubscriptionResult</code> class.
	 * 
	 * @param value
	 *            A {@link Subscription} object assigned as the value of the result.
	 */    
    public CreateSubscriptionResult(Subscription value) {
        this.setValue(value);
    }

	/**
	 * Specfies the value of the result.
	 * 
	 * @return A {@link Subscription} object assigned as the value of the result.
	 */
    public void setValue(Subscription value) {
        this.value = value;
    }

	/**
	 * Returns the value of the result.
	 * 
	 * @return A {@link Subscription} object that represents the value of the result.
	 */
    public Subscription getValue() {
        return value;
    }

}
