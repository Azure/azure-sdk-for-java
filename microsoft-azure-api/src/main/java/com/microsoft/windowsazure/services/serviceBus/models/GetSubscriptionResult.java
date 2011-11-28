package com.microsoft.windowsazure.services.serviceBus.models;

/**
 * Represents the result of a <code>getSubscription</code> operation.
 */
public class GetSubscriptionResult {

    private SubscriptionInfo value;

    /**
     * Creates an instance of the <code>GetSubscriptionResult</code> class.
     * 
     * @param value
     *            A {@link SubscriptionInfo} object assigned as the value of the
     *            result.
     */
    public GetSubscriptionResult(SubscriptionInfo value) {
        this.setValue(value);
    }

    /**
     * Specfies the value of the result.
     * 
     * @param value
     *            A {@link SubscriptionInfo} object assigned as the value of the
     *            result.
     */
    public void setValue(SubscriptionInfo value) {
        this.value = value;
    }

    /**
     * Returns the value of the result.
     * 
     * @return A {@link SubscriptionInfo} object that represents the value of the
     *         result.
     */
    public SubscriptionInfo getValue() {
        return value;
    }

}
