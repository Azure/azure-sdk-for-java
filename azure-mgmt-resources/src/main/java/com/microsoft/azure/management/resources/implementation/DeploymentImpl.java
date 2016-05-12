package com.microsoft.azure.management.resources.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.management.resources.DeploymentOperations;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.model.Provisionable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import com.microsoft.azure.management.resources.implementation.api.DeploymentsInner;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.Provider;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.api.*;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class DeploymentImpl extends
        IndexableRefreshableWrapperImpl<Deployment, DeploymentExtendedInner>
        implements
        Deployment,
        Deployment.DefinitionBlank,
        Deployment.DefinitionWithGroup,
        Deployment.DefinitionWithTemplate,
        Deployment.DefinitionWithParameters,
        Deployment.DefinitionProvisionable {

    private final DeploymentsInner client;
    private final DeploymentOperationsInner deploymentOperationsClient;
    private final ResourceGroups resourceGroups;
    private String resourceGroupName;

    public DeploymentImpl(DeploymentExtendedInner innerModel,
                          final DeploymentsInner client,
                          final DeploymentOperationsInner deploymentOperationsClient,
                          final ResourceGroups resourceGroups) {
        super (innerModel.name(), innerModel);
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

    /**************************************************************
     * Setters (fluent interface)
     **************************************************************/

    @Override
    public DefinitionWithGroup withNewResourceGroup(String resourceGroupName, Region location) throws Exception {
        ResourceGroup group = this.resourceGroups.define(resourceGroupName).withLocation(location).provision();
        this.resourceGroupName = group.name();
        return this;
    }

    @Override
    public DefinitionWithGroup withExistingResourceGroup(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
        return this;
    }

    @Override
    public DefinitionWithTemplate withTemplate(Object template) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setTemplate(template);
        return this;
    }

    @Override
    public DefinitionWithTemplate withTemplate(JsonNode template) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setTemplate(template);
        return this;
    }

    @Override
    public DefinitionWithTemplate withTemplateLink(String uri, String contentVersion) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setTemplateLink(new TemplateLink().setUri(uri).setContentVersion(contentVersion));
        return this;
    }

    @Override
    public DefinitionProvisionable withMode(DeploymentMode mode) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setMode(mode);
        return this;
    }

    @Override
    public DefinitionWithParameters withParameters(Object parameters) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setParameters(parameters);
        return this;
    }

    @Override
    public DefinitionWithParameters withParameters(JsonNode parameters) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setParameters(parameters);
        return this;
    }

    @Override
    public DefinitionWithParameters withParametersLink(String uri, String contentVersion) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setParametersLink(new ParametersLink().setUri(uri).setContentVersion(contentVersion));
        return this;
    }

    @Override
    public Deployment provision() throws Exception {         //  FLUENT: implementation of ResourceGroup.DefinitionProvisionable.Provisionable<ResourceGroup>
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
    public List<Provisionable<?>> prerequisites() {
        return null;
    }

    @Override
    public Deployment refresh() throws Exception {
        return null;
    }
}
