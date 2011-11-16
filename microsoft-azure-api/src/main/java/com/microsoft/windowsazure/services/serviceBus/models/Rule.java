package com.microsoft.windowsazure.services.serviceBus.models;

import javax.ws.rs.core.MediaType;

import com.microsoft.windowsazure.services.serviceBus.implementation.Content;
import com.microsoft.windowsazure.services.serviceBus.implementation.Entry;
import com.microsoft.windowsazure.services.serviceBus.implementation.EntryModel;
import com.microsoft.windowsazure.services.serviceBus.implementation.Filter;
import com.microsoft.windowsazure.services.serviceBus.implementation.RuleAction;
import com.microsoft.windowsazure.services.serviceBus.implementation.RuleDescription;

/**
 * Represents a rule.
 */
public class Rule extends EntryModel<RuleDescription> {

	/**
	 * Creates an instance of the <code>Rule</code> class.
	 */
	public Rule() {
		super(new Entry(), new RuleDescription());
		getEntry().setContent(new Content());
		getEntry().getContent().setType(MediaType.APPLICATION_XML);
		getEntry().getContent().setRuleDescription(getModel());
	}

	/**
	 * Creates an instance of the <code>Rule</code> class using the specified
	 * entry.
	 * 
	 * @param entry
	 *  An {@link Entry} object.
	 * 
	 */
	public Rule(Entry entry) {
		super(entry, entry.getContent().getRuleDescription());
	}

	/**
	 * Creates an instance of the <code>Rule</code> class using the specified
	 * name.
	 * 
	 * @param name
	 *  A <code>String</code> object that represents the name of the rule.
	 * 
	 */	public Rule(String name) {
		this();
		setName(name);
	}

	/**
	 * Returns the name of the rule.
	 * 
	 *  @return A <code>String</code> object that represents the name of the rule.
	 */
	public String getName() {
		return getEntry().getTitle();
	}

	/**
	 * Sets the name of the rule.
	 * 
	 * @param value
	 *           A <code>String</code> object that represents the name of the rule.
	 * 
	 * @return A <code>Rule</code> object that represents the updated rule.
	 */
	public Rule setName(String value) {
		getEntry().setTitle(value);
		return this;
	}

	/**
	 * Returns the filter used for the rule.
	 * 
	 *  @return A <code>Filter</code> object that represents the filter of the rule.
	 */	
	public Filter getFilter() {
		return getModel().getFilter();
	}

	/**
	 * Specifies the filter used for the rule.
	 * 
	 * @param value
	 *             A <code>Filter</code> object that represents the filter of the rule.
	 * 
	 * @return A <code>Rule</code> object that represents the updated rule.
	 */
	public Rule setFilter(Filter value) {
		getModel().setFilter(value);
		return this;
	}
	
	/**
	 * Returns the rule action used for the rule.
	 * 
	 *  @return A <code>RuleAction</code> object that represents the rule action.
	 */	
	public RuleAction getAction() {
		return getModel().getAction();
	}

	/**
	 * Specifies the rule action for the rule.
	 * 
	 * @param value
	 *             A <code>RuleAction</code> object that represents the rule action.
	 * 
	 * @return A <code>Rule</code> object that represents the updated rule.
	 */
	public Rule setAction(RuleAction value) {
		getModel().setAction(value);
		return this;
	}

}
