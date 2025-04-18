// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.streamanalytics.implementation;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.resourcemanager.streamanalytics.fluent.models.FunctionInner;
import com.azure.resourcemanager.streamanalytics.models.Function;
import com.azure.resourcemanager.streamanalytics.models.FunctionProperties;
import com.azure.resourcemanager.streamanalytics.models.FunctionRetrieveDefaultDefinitionParameters;
import com.azure.resourcemanager.streamanalytics.models.ResourceTestStatus;

public final class FunctionImpl implements Function, Function.Definition, Function.Update {
    private FunctionInner innerObject;

    private final com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager serviceManager;

    public String id() {
        return this.innerModel().id();
    }

    public FunctionProperties properties() {
        return this.innerModel().properties();
    }

    public String name() {
        return this.innerModel().name();
    }

    public String type() {
        return this.innerModel().type();
    }

    public String resourceGroupName() {
        return resourceGroupName;
    }

    public FunctionInner innerModel() {
        return this.innerObject;
    }

    private com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager manager() {
        return this.serviceManager;
    }

    private String resourceGroupName;

    private String jobName;

    private String functionName;

    private String createIfMatch;

    private String createIfNoneMatch;

    private String updateIfMatch;

    public FunctionImpl withExistingStreamingjob(String resourceGroupName, String jobName) {
        this.resourceGroupName = resourceGroupName;
        this.jobName = jobName;
        return this;
    }

    public Function create() {
        this.innerObject = serviceManager.serviceClient()
            .getFunctions()
            .createOrReplaceWithResponse(resourceGroupName, jobName, functionName, this.innerModel(), createIfMatch,
                createIfNoneMatch, Context.NONE)
            .getValue();
        return this;
    }

    public Function create(Context context) {
        this.innerObject = serviceManager.serviceClient()
            .getFunctions()
            .createOrReplaceWithResponse(resourceGroupName, jobName, functionName, this.innerModel(), createIfMatch,
                createIfNoneMatch, context)
            .getValue();
        return this;
    }

    FunctionImpl(String name, com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager serviceManager) {
        this.innerObject = new FunctionInner();
        this.serviceManager = serviceManager;
        this.functionName = name;
        this.createIfMatch = null;
        this.createIfNoneMatch = null;
    }

    public FunctionImpl update() {
        this.updateIfMatch = null;
        return this;
    }

    public Function apply() {
        this.innerObject = serviceManager.serviceClient()
            .getFunctions()
            .updateWithResponse(resourceGroupName, jobName, functionName, this.innerModel(), updateIfMatch,
                Context.NONE)
            .getValue();
        return this;
    }

    public Function apply(Context context) {
        this.innerObject = serviceManager.serviceClient()
            .getFunctions()
            .updateWithResponse(resourceGroupName, jobName, functionName, this.innerModel(), updateIfMatch, context)
            .getValue();
        return this;
    }

    FunctionImpl(FunctionInner innerObject,
        com.azure.resourcemanager.streamanalytics.StreamAnalyticsManager serviceManager) {
        this.innerObject = innerObject;
        this.serviceManager = serviceManager;
        this.resourceGroupName = ResourceManagerUtils.getValueFromIdByName(innerObject.id(), "resourcegroups");
        this.jobName = ResourceManagerUtils.getValueFromIdByName(innerObject.id(), "streamingjobs");
        this.functionName = ResourceManagerUtils.getValueFromIdByName(innerObject.id(), "functions");
    }

    public Function refresh() {
        this.innerObject = serviceManager.serviceClient()
            .getFunctions()
            .getWithResponse(resourceGroupName, jobName, functionName, Context.NONE)
            .getValue();
        return this;
    }

    public Function refresh(Context context) {
        this.innerObject = serviceManager.serviceClient()
            .getFunctions()
            .getWithResponse(resourceGroupName, jobName, functionName, context)
            .getValue();
        return this;
    }

    public ResourceTestStatus test() {
        return serviceManager.functions().test(resourceGroupName, jobName, functionName);
    }

    public ResourceTestStatus test(FunctionInner function, Context context) {
        return serviceManager.functions().test(resourceGroupName, jobName, functionName, function, context);
    }

    public Response<Function> retrieveDefaultDefinitionWithResponse(
        FunctionRetrieveDefaultDefinitionParameters functionRetrieveDefaultDefinitionParameters, Context context) {
        return serviceManager.functions()
            .retrieveDefaultDefinitionWithResponse(resourceGroupName, jobName, functionName,
                functionRetrieveDefaultDefinitionParameters, context);
    }

    public Function retrieveDefaultDefinition() {
        return serviceManager.functions().retrieveDefaultDefinition(resourceGroupName, jobName, functionName);
    }

    public FunctionImpl withProperties(FunctionProperties properties) {
        this.innerModel().withProperties(properties);
        return this;
    }

    public FunctionImpl withName(String name) {
        this.innerModel().withName(name);
        return this;
    }

    public FunctionImpl withIfMatch(String ifMatch) {
        if (isInCreateMode()) {
            this.createIfMatch = ifMatch;
            return this;
        } else {
            this.updateIfMatch = ifMatch;
            return this;
        }
    }

    public FunctionImpl withIfNoneMatch(String ifNoneMatch) {
        this.createIfNoneMatch = ifNoneMatch;
        return this;
    }

    private boolean isInCreateMode() {
        return this.innerModel().id() == null;
    }
}
