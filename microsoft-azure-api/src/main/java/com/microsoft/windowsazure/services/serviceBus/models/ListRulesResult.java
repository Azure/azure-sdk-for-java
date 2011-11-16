package com.microsoft.windowsazure.services.serviceBus.models;

import java.util.List;

/**
 * Represents the result of a <code>listRules</code> operation.
 */
public class ListRulesResult {

    private List<Rule> items;

	/**
	 * Returns the items in the result list.
	 * 
	 * @return A <code>List</code> of {@link Rule} objects that represent the
	 *         items in the result list.
	 */
    public List<Rule> getItems() {
        return items;
    }

	/**
	 * Specfies the items in the result list.
	 * 
	 * @param value
	 *            A <code>List</code> object that contains the {@link Rule}
	 *            objects assigned as the value of the result.
	 */
    public void setItems(List<Rule> items) {
        this.items = items;
    }

}
