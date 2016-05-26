/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentOperations;
import com.microsoft.azure.management.resources.Provider;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableImpl;
import com.microsoft.azure.management.resources.implementation.api.Dependency;
import com.microsoft.azure.management.resources.implementation.api.DeploymentExtendedInner;
import com.microsoft.azure.management.resources.implementation.api.DeploymentInner;
import com.microsoft.azure.management.resources.implementation.api.DeploymentMode;
import com.microsoft.azure.management.resources.implementation.api.DeploymentOperationsInner;
import com.microsoft.azure.management.resources.implementation.api.DeploymentProperties;
import com.microsoft.azure.management.resources.implementation.api.DeploymentPropertiesExtended;
import com.microsoft.azure.management.resources.implementation.api.DeploymentsInner;
import com.microsoft.azure.management.resources.implementation.api.ParametersLink;
import com.microsoft.azure.management.resources.implementation.api.ProviderInner;
import com.microsoft.azure.management.resources.implementation.api.TemplateLink;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of this class provides access to a deployment in Azure.
 */
final class DeploymentImpl extends
        CreatableImpl<Deployment, DeploymentExtendedInner>
        implements
        Deployment,
        Deployment.DefinitionBlank,
        Deployment.DefinitionWithTemplate,
        Deployment.DefinitionWithParameters,
        Deployment.DefinitionWithMode,
        Deployment.DefinitionCreatable {

    private final DeploymentsInner client;
    private final DeploymentOperationsInner deploymentOperationsClient;
    private final ResourceGroups resourceGroups;
    private String resourceGroupName;

    DeploymentImpl(DeploymentExtendedInner innerModel,
                          final DeploymentsInner client,
                          final DeploymentOperationsInner deploymentOperationsClient,
                          final ResourceGroups resourceGroups) {
        super(innerModel.name(), innerModel);
        this.client = client;
        this.deploymentOperationsClient = deploymentOperationsClient;
        this.resourceGroupName = ResourceUtils.groupFromResourceId(innerModel.id());
        this.resourceGroups = resourceGroups;
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String provisioningState() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().provisioningState();
    }

    @Override
    public String correlationid() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().correlationId();
    }

    @Override
    public DateTime timestamp() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().timestamp();
    }

    @Override
    public Object outputs() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().outputs();
    }

    @Override
    public List<Provider> providers() {
        if (this.inner().properties() == null) {
            return null;
        }
        List<Provider> providers = new ArrayList<>();
        for (ProviderInner inner : this.inner().properties().providers()) {
            providers.add(new ProviderImpl(inner));
        }
        return providers;
    }

    @Override
    public List<Dependency> dependencies() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().dependencies();
    }

    @Override
    public Object template() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().template();
    }

    @Override
    public TemplateLink templateLink() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().templateLink();
    }

    @Override
    public Object parameters() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().parameters();
    }

    @Override
    public ParametersLink parametersLink() {
        if (this.inner().properties() == null) {
            return null;
        }
        return this.inner().properties().parametersLink();
    }

    @Override
    public DeploymentMode mode() {
        if (this.inner().properties() == null) {
            return null;
        }
        return inner().properties().mode();
    }

    @Override
    public DeploymentOperations deploymentOperations() {
        return new DeploymentOperationsImpl(deploymentOperationsClient, this, resourceGroups);
    }

    @Override
    public DefinitionWithTemplate withNewResourceGroup(String resourceGroupName, Region region) {
        prerequisites().put(resourceGroupName, this.resourceGroups.define(resourceGroupName).withRegion(region));
        this.resourceGroupName = resourceGroupName;
        return this;
    }

    @Override
    public DefinitionWithTemplate withExistingResourceGroup(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
        return this;
    }

    @Override
    public DefinitionWithParameters withTemplate(Object template) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setTemplate(template);
        return this;
    }

    @Override
    public DefinitionWithParameters withTemplate(JsonNode template) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setTemplate(template);
        return this;
    }

    @Override
    public DefinitionWithParameters withTemplateLink(String uri, String contentVersion) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setTemplateLink(new TemplateLink().setUri(uri).setContentVersion(contentVersion));
        return this;
    }

    @Override
    public DefinitionCreatable withMode(DeploymentMode mode) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setMode(mode);
        return this;
    }

    @Override
    public DefinitionWithMode withParameters(Object parameters) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setParameters(parameters);
        return this;
    }

    @Override
    public DefinitionWithMode withParameters(JsonNode parameters) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setParameters(parameters);
        return this;
    }

    @Override
    public DefinitionWithMode withParametersLink(String uri, String contentVersion) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setParametersLink(new ParametersLink().setUri(uri).setContentVersion(contentVersion));
        return this;
    }

    @Override
    public Deployment create() throws Exception {         //  FLUENT: implementation of ResourceGroup.DefinitionCreatable.Creatable<ResourceGroup>
        DeploymentInner inner = new DeploymentInner()
                .setProperties(new DeploymentProperties());
        inner.properties().setMode(mode());
        inner.properties().setTemplate(template());
        inner.properties().setTemplateLink(templateLink());
        inner.properties().setParameters(parameters());
        inner.properties().setParametersLink(parametersLink());
        client.createOrUpdate(resourceGroupName(), name(), inner);
        return this;
    }

    @Override
    public Deployment refresh() throws Exception {
        return null;
    }
}
