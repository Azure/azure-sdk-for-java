package com.microsoft.windowsazure.services.serviceBus.models;

import java.util.List;

/**
 * Represents the result of a <code>listSubscriptions</code> operation.
 */
public class ListSubscriptionsResult {

    private List<SubscriptionInfo> items;

	/**
	 * Returns the items in the result list.
	 * 
	 * @return A <code>List</code> of {@link SubscriptionInfo} objects that represent the
	 *         items in the result list.
	 */
    public List<SubscriptionInfo> getItems() {
        return items;
    }

	/**
	 * Specfies the items in the result list.
	 * 
	 * @param value
	 *            A <code>List</code> object that contains the {@link SubscriptionInfo}
	 *            objects assigned as the value of the result.
	 */
    public void setItems(List<SubscriptionInfo> items) {
        this.items = items;
    }
}
