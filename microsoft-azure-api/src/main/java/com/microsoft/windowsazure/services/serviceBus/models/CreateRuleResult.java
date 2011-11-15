package com.microsoft.windowsazure.services.serviceBus.models;

public class CreateRuleResult {

    private Rule value;

    public CreateRuleResult(Rule value) {
        this.setValue(value);
    }

    public void setValue(Rule value) {
        this.value = value;
    }

    public Rule getValue() {
        return value;
    }

}
