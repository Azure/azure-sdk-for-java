package com.microsoft.azure.management.resources.models.implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import com.microsoft.azure.management.resources.api.DeploymentsInner;
import com.microsoft.azure.management.resources.models.Deployment;
import com.microsoft.azure.management.resources.models.Provider;
import com.microsoft.azure.management.resources.models.implementation.api.*;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class DeploymentImpl extends
        IndexableRefreshableWrapperImpl<Deployment, DeploymentExtendedInner>
        implements
        Deployment,
        Deployment.DefinitionBlank,
        Deployment.DefinitionWithTemplate,
        Deployment.DefinitionProvisionable {

    private final DeploymentsInner client;
    private final String resourceGroupName;
    private final String deploymentName;

    public DeploymentImpl(String resourceGroupName, String deploymentName, DeploymentExtendedInner deployment, DeploymentsInner client) {
        super (deploymentName, deployment);
        this.client = client;
        this.resourceGroupName = resourceGroupName;
        this.deploymentName = deploymentName;
    }

    /***********************************************************
     * Getters
     ***********************************************************/

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

    /**************************************************************
     * Setters (fluent interface)
     **************************************************************/

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
    public DefinitionProvisionable withParameters(Object parameters) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setParameters(parameters);
        return this;
    }

    @Override
    public DefinitionProvisionable withParameters(JsonNode parameters) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setParameters(parameters);
        return this;
    }

    @Override
    public DefinitionProvisionable withParametersLink(String uri, String contentVersion) {
        if (this.inner().properties() == null) {
            this.inner().setProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().setParametersLink(new ParametersLink().setUri(uri).setContentVersion(contentVersion));
        return this;
    }

    /************************************************************
     * Verbs
     ************************************************************/

    @Override
    public Deployment provision() throws Exception {         //  FLUENT: implementation of ResourceGroup.DefinitionProvisionable.Provisionable<ResourceGroup>
        DeploymentInner inner = new DeploymentInner()
                .setProperties(new DeploymentProperties());
        inner.properties().setMode(mode());
        inner.properties().setTemplate(template());
        inner.properties().setTemplateLink(templateLink());
        inner.properties().setParameters(parameters());
        inner.properties().setParametersLink(parametersLink());
        client.createOrUpdate(resourceGroupName, deploymentName, inner);
        return this;
    }

    @Override
    public Deployment refresh() throws Exception {
        return null;
    }
}
