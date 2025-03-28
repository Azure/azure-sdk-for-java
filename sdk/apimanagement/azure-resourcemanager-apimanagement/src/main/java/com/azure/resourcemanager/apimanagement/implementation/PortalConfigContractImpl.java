// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.apimanagement.implementation;

import com.azure.core.util.Context;
import com.azure.resourcemanager.apimanagement.fluent.models.PortalConfigContractInner;
import com.azure.resourcemanager.apimanagement.models.PortalConfigContract;
import com.azure.resourcemanager.apimanagement.models.PortalConfigCorsProperties;
import com.azure.resourcemanager.apimanagement.models.PortalConfigCspProperties;
import com.azure.resourcemanager.apimanagement.models.PortalConfigDelegationProperties;
import com.azure.resourcemanager.apimanagement.models.PortalConfigPropertiesSignin;
import com.azure.resourcemanager.apimanagement.models.PortalConfigPropertiesSignup;

public final class PortalConfigContractImpl
    implements PortalConfigContract, PortalConfigContract.Definition, PortalConfigContract.Update {
    private PortalConfigContractInner innerObject;

    private final com.azure.resourcemanager.apimanagement.ApiManagementManager serviceManager;

    public String id() {
        return this.innerModel().id();
    }

    public String name() {
        return this.innerModel().name();
    }

    public String type() {
        return this.innerModel().type();
    }

    public Boolean enableBasicAuth() {
        return this.innerModel().enableBasicAuth();
    }

    public PortalConfigPropertiesSignin signin() {
        return this.innerModel().signin();
    }

    public PortalConfigPropertiesSignup signup() {
        return this.innerModel().signup();
    }

    public PortalConfigDelegationProperties delegation() {
        return this.innerModel().delegation();
    }

    public PortalConfigCorsProperties cors() {
        return this.innerModel().cors();
    }

    public PortalConfigCspProperties csp() {
        return this.innerModel().csp();
    }

    public String resourceGroupName() {
        return resourceGroupName;
    }

    public PortalConfigContractInner innerModel() {
        return this.innerObject;
    }

    private com.azure.resourcemanager.apimanagement.ApiManagementManager manager() {
        return this.serviceManager;
    }

    private String resourceGroupName;

    private String serviceName;

    private String portalConfigId;

    private String createIfMatch;

    private String updateIfMatch;

    public PortalConfigContractImpl withExistingService(String resourceGroupName, String serviceName) {
        this.resourceGroupName = resourceGroupName;
        this.serviceName = serviceName;
        return this;
    }

    public PortalConfigContract create() {
        this.innerObject = serviceManager.serviceClient()
            .getPortalConfigs()
            .createOrUpdateWithResponse(resourceGroupName, serviceName, portalConfigId, createIfMatch,
                this.innerModel(), Context.NONE)
            .getValue();
        return this;
    }

    public PortalConfigContract create(Context context) {
        this.innerObject = serviceManager.serviceClient()
            .getPortalConfigs()
            .createOrUpdateWithResponse(resourceGroupName, serviceName, portalConfigId, createIfMatch,
                this.innerModel(), context)
            .getValue();
        return this;
    }

    PortalConfigContractImpl(String name, com.azure.resourcemanager.apimanagement.ApiManagementManager serviceManager) {
        this.innerObject = new PortalConfigContractInner();
        this.serviceManager = serviceManager;
        this.portalConfigId = name;
        this.createIfMatch = null;
    }

    public PortalConfigContractImpl update() {
        this.updateIfMatch = null;
        return this;
    }

    public PortalConfigContract apply() {
        this.innerObject = serviceManager.serviceClient()
            .getPortalConfigs()
            .updateWithResponse(resourceGroupName, serviceName, portalConfigId, updateIfMatch, this.innerModel(),
                Context.NONE)
            .getValue();
        return this;
    }

    public PortalConfigContract apply(Context context) {
        this.innerObject = serviceManager.serviceClient()
            .getPortalConfigs()
            .updateWithResponse(resourceGroupName, serviceName, portalConfigId, updateIfMatch, this.innerModel(),
                context)
            .getValue();
        return this;
    }

    PortalConfigContractImpl(PortalConfigContractInner innerObject,
        com.azure.resourcemanager.apimanagement.ApiManagementManager serviceManager) {
        this.innerObject = innerObject;
        this.serviceManager = serviceManager;
        this.resourceGroupName = ResourceManagerUtils.getValueFromIdByName(innerObject.id(), "resourceGroups");
        this.serviceName = ResourceManagerUtils.getValueFromIdByName(innerObject.id(), "service");
        this.portalConfigId = ResourceManagerUtils.getValueFromIdByName(innerObject.id(), "portalconfigs");
    }

    public PortalConfigContract refresh() {
        this.innerObject = serviceManager.serviceClient()
            .getPortalConfigs()
            .getWithResponse(resourceGroupName, serviceName, portalConfigId, Context.NONE)
            .getValue();
        return this;
    }

    public PortalConfigContract refresh(Context context) {
        this.innerObject = serviceManager.serviceClient()
            .getPortalConfigs()
            .getWithResponse(resourceGroupName, serviceName, portalConfigId, context)
            .getValue();
        return this;
    }

    public PortalConfigContractImpl withEnableBasicAuth(Boolean enableBasicAuth) {
        this.innerModel().withEnableBasicAuth(enableBasicAuth);
        return this;
    }

    public PortalConfigContractImpl withSignin(PortalConfigPropertiesSignin signin) {
        this.innerModel().withSignin(signin);
        return this;
    }

    public PortalConfigContractImpl withSignup(PortalConfigPropertiesSignup signup) {
        this.innerModel().withSignup(signup);
        return this;
    }

    public PortalConfigContractImpl withDelegation(PortalConfigDelegationProperties delegation) {
        this.innerModel().withDelegation(delegation);
        return this;
    }

    public PortalConfigContractImpl withCors(PortalConfigCorsProperties cors) {
        this.innerModel().withCors(cors);
        return this;
    }

    public PortalConfigContractImpl withCsp(PortalConfigCspProperties csp) {
        this.innerModel().withCsp(csp);
        return this;
    }

    public PortalConfigContractImpl withIfMatch(String ifMatch) {
        if (isInCreateMode()) {
            this.createIfMatch = ifMatch;
            return this;
        } else {
            this.updateIfMatch = ifMatch;
            return this;
        }
    }

    private boolean isInCreateMode() {
        return this.innerModel().id() == null;
    }
}
