package com.microsoft.windowsazure.services.serviceBus.models;

import java.util.List;

/**
 * Represents the result of a <code>listRules</code> operation.
 */
public class ListRulesResult {

    private List<RuleInfo> items;

    /**
     * Returns the items in the result list.
     * 
     * @return A <code>List</code> of {@link RuleInfo} objects that represent the
     *         items in the result list.
     */
    public List<RuleInfo> getItems() {
        return items;
    }

    /**
     * Specfies the items in the result list.
     * 
     * @param value
     *            A <code>List</code> object that contains the {@link RuleInfo} objects assigned as the value of the
     *            result.
     */
    public void setItems(List<RuleInfo> items) {
        this.items = items;
    }

}
