package com.microsoft.windowsazure.services.serviceBus.models;

import java.util.List;

/**
 * Represents the result of a <code>listSubscriptions</code> operation.
 */
public class ListSubscriptionsResult {

    private List<Subscription> items;

	/**
	 * Returns the items in the result list.
	 * 
	 * @return A <code>List</code> of {@link Subscription} objects that represent the
	 *         items in the result list.
	 */
    public List<Subscription> getItems() {
        return items;
    }

	/**
	 * Specfies the items in the result list.
	 * 
	 * @param value
	 *            A <code>List</code> object that contains the {@link Subscription}
	 *            objects assigned as the value of the result.
	 */
    public void setItems(List<Subscription> items) {
        this.items = items;
    }
}
