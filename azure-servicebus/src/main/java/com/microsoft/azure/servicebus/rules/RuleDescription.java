package com.microsoft.azure.servicebus.rules;

public class RuleDescription {	 
     private Filter filter;
     private RuleAction action;
     private String name;
     
     public RuleDescription()
     {
    	 this.filter = TrueFilter.DEFAULT;
     }
     
     public RuleDescription(String name)
     {
    	 this.filter = TrueFilter.DEFAULT;
    	 this.name = name;
     }
     
     public RuleDescription(Filter filter)
     {
    	 this.filter = filter;
     }
     
     public RuleDescription(String name, Filter filter)
     {
    	 this.name = name;
    	 this.filter = filter;
     }

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	public RuleAction getAction() {
		return action;
	}

	public void setAction(RuleAction action) {
		this.action = action;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}    
}
