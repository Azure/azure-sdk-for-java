package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.ExceptionRule;
import com.azure.core.annotation.Fluent;

import java.util.Map;

@Fluent
public class CreateExceptionPolicyOptions {

    private String id;

    private String name;

    private Map<String, ExceptionRule> exceptionRules;

    public CreateExceptionPolicyOptions(String id, Map<String, ExceptionRule> exceptionRules) {
        this.id = id;
        this.exceptionRules = exceptionRules;
    }

    public String getId() {
        return this.id;
    }

    public CreateExceptionPolicyOptions setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, ExceptionRule> getExceptionRules() {
        return this.exceptionRules;
    }
}
