package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.ExceptionRule;
import com.azure.core.annotation.Fluent;

import java.util.Map;

@Fluent
public class UpdateExceptionPolicyOptions {

    private String id;

    private String name;

    private Map<String, ExceptionRule> exceptionRules;

    public UpdateExceptionPolicyOptions(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public UpdateExceptionPolicyOptions setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public UpdateExceptionPolicyOptions setExceptionRules(Map<String, ExceptionRule> exceptionRules) {
        this.exceptionRules = exceptionRules;
        return this;
    }

    public Map<String, ExceptionRule> getExceptionRules() {
        return this.exceptionRules;
    }
}
