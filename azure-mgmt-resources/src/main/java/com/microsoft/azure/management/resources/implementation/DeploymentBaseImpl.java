/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.management.resources.Dependency;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentBase;
import com.microsoft.azure.management.resources.DeploymentExportResult;
import com.microsoft.azure.management.resources.DeploymentMode;
import com.microsoft.azure.management.resources.DeploymentOperations;
import com.microsoft.azure.management.resources.DeploymentProperties;
import com.microsoft.azure.management.resources.DeploymentPropertiesExtended;
import com.microsoft.azure.management.resources.ParametersLink;
import com.microsoft.azure.management.resources.Provider;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.TemplateLink;
import com.microsoft.azure.management.resources.fluentcore.ActionProxy;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of {@link DeploymentBase} and its nested interfaces.
 */
final class DeploymentBaseImpl extends
        CreatableUpdatableImpl<Deployment, DeploymentExtendedInner, DeploymentBaseImpl>
        implements
        DeploymentBase,
        DeploymentBase.Definition,
        DeploymentBase.Update {

    private final DeploymentsInner client;
    private final DeploymentOperationsInner deploymentOperationsClient;
    private final ResourceManager resourceManager;
    private String resourceGroupName;
    private Creatable<ResourceGroup> creatableResourceGroup;
    private ObjectMapper objectMapper;

    DeploymentBaseImpl(DeploymentExtendedInner innerModel,
                       final DeploymentsInner client,
                       final DeploymentOperationsInner deploymentOperationsClient,
                       final ResourceManager resourceManager) {
        super(innerModel.name(), innerModel);
        this.client = client;
        this.deploymentOperationsClient = deploymentOperationsClient;
        this.resourceGroupName = ResourceUtils.groupFromResourceId(innerModel.id());
        this.resourceManager = resourceManager;
        this.objectMapper = new ObjectMapper();
    }

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
    public String correlationId() {
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
        return new DeploymentOperationsImpl(deploymentOperationsClient, this);
    }

    @Override
    public Observable<Void> cancelAsync() {
        return client.cancelAsync(resourceGroupName, name());
    }

    @Override
    public DeploymentExportResult exportTemplate() {
        DeploymentExportResultInner inner = client.exportTemplate(resourceGroupName(), name());
        return new DeploymentExportResultImpl(inner);
    }

    // Withers

    @Override
    public DeploymentBaseImpl withNewResourceGroup(String resourceGroupName, Region region) {
        this.creatableResourceGroup = this.resourceManager.resourceGroups()
                .define(resourceGroupName)
                .withRegion(region);
        addCreatableDependency(this.creatableResourceGroup);
        this.resourceGroupName = resourceGroupName;
        return this;
    }

    @Override
    public DeploymentBaseImpl withNewResourceGroup(Creatable<ResourceGroup> resourceGroupDefinition) {
        this.resourceGroupName = resourceGroupDefinition.name();
        addCreatableDependency(resourceGroupDefinition);
        this.creatableResourceGroup = resourceGroupDefinition;
        return this;
    }

    @Override
    public DeploymentBaseImpl withExistingResourceGroup(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
        return this;
    }

    @Override
    public DeploymentBaseImpl withExistingResourceGroup(ResourceGroup resourceGroup) {
        this.resourceGroupName = resourceGroup.name();
        return this;
    }

    @Override
    public DeploymentBaseImpl withTemplate(Object template) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().withTemplate(template);
        this.inner().properties().withTemplateLink(null);
        return this;
    }

    @Override
    public DeploymentBaseImpl withTemplate(String templateJson) throws IOException {
        return withTemplate(objectMapper.readTree(templateJson));
    }

    @Override
    public DeploymentBaseImpl withTemplateLink(String uri, String contentVersion) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().withTemplateLink(new TemplateLink().withUri(uri).withContentVersion(contentVersion));
        this.inner().properties().withTemplate(null);
        return this;
    }

    @Override
    public DeploymentBaseImpl withMode(DeploymentMode mode) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().withMode(mode);
        return this;
    }

    @Override
    public DeploymentBaseImpl withParameters(Object parameters) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().withParameters(parameters);
        this.inner().properties().withParametersLink(null);
        return this;
    }

    @Override
    public DeploymentBaseImpl withParameters(String parametersJson) throws IOException {
        return withParameters(objectMapper.readTree(parametersJson));
    }

    @Override
    public DeploymentBaseImpl withParametersLink(String uri, String contentVersion) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().withParametersLink(new ParametersLink().withUri(uri).withContentVersion(contentVersion));
        this.inner().properties().withParameters(null);
        return this;
    }

    @Override
    public Deployment beginCreate() {
        if (creatableResourceGroup != null) {
            creatableResourceGroup.create();
        }
        DeploymentInner inner = new DeploymentInner()
                .withProperties(new DeploymentProperties());
        inner.properties().withMode(mode());
        inner.properties().withTemplate(template());
        inner.properties().withTemplateLink(templateLink());
        inner.properties().withParameters(parameters());
        inner.properties().withParametersLink(parametersLink());
        client.beginCreateOrUpdate(resourceGroupName(), name(), inner);
        return wrap(this);
    }

    @Override
    public Observable<Deployment> createResourceAsync() {
        final DeploymentBaseImpl self = this;
        DeploymentInner inner = new DeploymentInner()
                .withProperties(new DeploymentProperties());
        inner.properties().withMode(mode());
        inner.properties().withTemplate(template());
        inner.properties().withTemplateLink(templateLink());
        inner.properties().withParameters(parameters());
        inner.properties().withParametersLink(parametersLink());
        return client.createOrUpdateAsync(resourceGroupName(), name(), inner)
                .map(new Func1<DeploymentExtendedInner, Deployment>() {
                    @Override
                    public Deployment call(DeploymentExtendedInner deploymentExtendedInner) {
                        self.setInner(deploymentExtendedInner);
                        Deployment toReturn = wrap(self);
                        return toReturn;
                    }
                });
    }


    @Override
    public Observable<Deployment> applyAsync() {
        return updateResourceAsync();
    }

    @Override
    public Observable<Deployment> updateResourceAsync() {
        try {
            if (this.templateLink() != null && this.template() != null) {
                this.withTemplate(null);
            }
            if (this.parametersLink() != null && this.parameters() != null) {
                this.withParameters(null);
            }
        } catch (IOException e) {
            return Observable.error(e);
        }
        return createResourceAsync();
    }

    @Override
    public Deployment refresh() {
        setInner(client.get(resourceGroupName(), name()));
        return wrap(this);
    }

    private Deployment wrap(DeploymentBaseImpl impl) {
        return ActionProxy.newInstance(Deployment.class, impl);
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().id() == null;
    }
}
