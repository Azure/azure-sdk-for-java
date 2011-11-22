package com.microsoft.windowsazure.services.serviceBus.models;

import java.util.List;

/**
 * Represents the result of a <code>listTopics</code> operation.
 */
public class ListTopicsResult {

    private List<Topic> items;

	/**
	 * Returns the items in the result list.
	 * 
	 * @return A <code>List</code> of {@link Topic} objects that represent the
	 *         items in the result list.
	 */
    public List<Topic> getItems() {
        return items;
    }
    
	/**
	 * Specfies the items in the result list.
	 * 
	 * @param value
	 *            A <code>List</code> object that contains the {@link Topic}
	 *            objects assigned as the value of the result.
	 */
    public void setItems(List<Topic> items) {
        this.items = items;
    }
}
