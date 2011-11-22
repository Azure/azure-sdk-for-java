package com.microsoft.windowsazure.services.serviceBus.models;

import java.util.List;

/**
 * Represents the result of a <code>listTopics</code> operation.
 */
public class ListTopicsResult {

    private List<TopicInfo> items;

	/**
	 * Returns the items in the result list.
	 * 
	 * @return A <code>List</code> of {@link TopicInfo} objects that represent the
	 *         items in the result list.
	 */
    public List<TopicInfo> getItems() {
        return items;
    }
    
	/**
	 * Specfies the items in the result list.
	 * 
	 * @param value
	 *            A <code>List</code> object that contains the {@link TopicInfo}
	 *            objects assigned as the value of the result.
	 */
    public void setItems(List<TopicInfo> items) {
        this.items = items;
    }
}
