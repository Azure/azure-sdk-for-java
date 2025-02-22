// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.dynatrace.implementation;

import com.azure.core.management.SystemData;
import com.azure.core.util.Context;
import com.azure.resourcemanager.dynatrace.fluent.models.TagRuleInner;
import com.azure.resourcemanager.dynatrace.models.LogRules;
import com.azure.resourcemanager.dynatrace.models.MetricRules;
import com.azure.resourcemanager.dynatrace.models.ProvisioningState;
import com.azure.resourcemanager.dynatrace.models.TagRule;

public final class TagRuleImpl implements TagRule, TagRule.Definition, TagRule.Update {
    private TagRuleInner innerObject;

    private final com.azure.resourcemanager.dynatrace.DynatraceManager serviceManager;

    public String id() {
        return this.innerModel().id();
    }

    public String name() {
        return this.innerModel().name();
    }

    public String type() {
        return this.innerModel().type();
    }

    public SystemData systemData() {
        return this.innerModel().systemData();
    }

    public LogRules logRules() {
        return this.innerModel().logRules();
    }

    public MetricRules metricRules() {
        return this.innerModel().metricRules();
    }

    public ProvisioningState provisioningState() {
        return this.innerModel().provisioningState();
    }

    public String resourceGroupName() {
        return resourceGroupName;
    }

    public TagRuleInner innerModel() {
        return this.innerObject;
    }

    private com.azure.resourcemanager.dynatrace.DynatraceManager manager() {
        return this.serviceManager;
    }

    private String resourceGroupName;

    private String monitorName;

    private String ruleSetName;

    public TagRuleImpl withExistingMonitor(String resourceGroupName, String monitorName) {
        this.resourceGroupName = resourceGroupName;
        this.monitorName = monitorName;
        return this;
    }

    public TagRule create() {
        this.innerObject = serviceManager.serviceClient()
            .getTagRules()
            .createOrUpdate(resourceGroupName, monitorName, ruleSetName, this.innerModel(), Context.NONE);
        return this;
    }

    public TagRule create(Context context) {
        this.innerObject = serviceManager.serviceClient()
            .getTagRules()
            .createOrUpdate(resourceGroupName, monitorName, ruleSetName, this.innerModel(), context);
        return this;
    }

    TagRuleImpl(String name, com.azure.resourcemanager.dynatrace.DynatraceManager serviceManager) {
        this.innerObject = new TagRuleInner();
        this.serviceManager = serviceManager;
        this.ruleSetName = name;
    }

    public TagRuleImpl update() {
        return this;
    }

    public TagRule apply() {
        this.innerObject = serviceManager.serviceClient()
            .getTagRules()
            .createOrUpdate(resourceGroupName, monitorName, ruleSetName, this.innerModel(), Context.NONE);
        return this;
    }

    public TagRule apply(Context context) {
        this.innerObject = serviceManager.serviceClient()
            .getTagRules()
            .createOrUpdate(resourceGroupName, monitorName, ruleSetName, this.innerModel(), context);
        return this;
    }

    TagRuleImpl(TagRuleInner innerObject, com.azure.resourcemanager.dynatrace.DynatraceManager serviceManager) {
        this.innerObject = innerObject;
        this.serviceManager = serviceManager;
        this.resourceGroupName = ResourceManagerUtils.getValueFromIdByName(innerObject.id(), "resourceGroups");
        this.monitorName = ResourceManagerUtils.getValueFromIdByName(innerObject.id(), "monitors");
        this.ruleSetName = ResourceManagerUtils.getValueFromIdByName(innerObject.id(), "tagRules");
    }

    public TagRule refresh() {
        this.innerObject = serviceManager.serviceClient()
            .getTagRules()
            .getWithResponse(resourceGroupName, monitorName, ruleSetName, Context.NONE)
            .getValue();
        return this;
    }

    public TagRule refresh(Context context) {
        this.innerObject = serviceManager.serviceClient()
            .getTagRules()
            .getWithResponse(resourceGroupName, monitorName, ruleSetName, context)
            .getValue();
        return this;
    }

    public TagRuleImpl withLogRules(LogRules logRules) {
        this.innerModel().withLogRules(logRules);
        return this;
    }

    public TagRuleImpl withMetricRules(MetricRules metricRules) {
        this.innerModel().withMetricRules(metricRules);
        return this;
    }
}
