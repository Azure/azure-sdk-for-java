package com.microsoft.windowsazure.services.serviceBus.models;

public class GetRuleResult {

    private Rule value;

    public GetRuleResult(Rule value) {
        this.setValue(value);
    }

    public void setValue(Rule value) {
        this.value = value;
    }

    public Rule getValue() {
        return value;
    }

}
