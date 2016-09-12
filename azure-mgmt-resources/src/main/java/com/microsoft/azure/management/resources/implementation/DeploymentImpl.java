/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.Dependency;
import com.microsoft.azure.management.resources.Deployment;
import com.microsoft.azure.management.resources.DeploymentExportResult;
import com.microsoft.azure.management.resources.DeploymentMode;
import com.microsoft.azure.management.resources.DeploymentOperations;
import com.microsoft.azure.management.resources.DeploymentProperties;
import com.microsoft.azure.management.resources.DeploymentPropertiesExtended;
import com.microsoft.azure.management.resources.ParametersLink;
import com.microsoft.azure.management.resources.Provider;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.TemplateLink;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of {@link Deployment} and its nested interfaces.
 */
final class DeploymentImpl extends
        CreatableUpdatableImpl<Deployment, DeploymentExtendedInner, DeploymentImpl>
        implements
        Deployment,
        Deployment.Definition,
        Deployment.Update {

    private final DeploymentsInner client;
    private final DeploymentOperationsInner deploymentOperationsClient;
    private final ResourceManager resourceManager;
    private String resourceGroupName;
    private Creatable<ResourceGroup> creatableResourceGroup;
    private ObjectMapper objectMapper;

    DeploymentImpl(DeploymentExtendedInner innerModel,
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
    public void cancel() throws CloudException, IOException {
        client.cancel(resourceGroupName, name());
    }

    @Override
    public DeploymentExportResult exportTemplate() throws CloudException, IOException {
        DeploymentExportResultInner inner = client.exportTemplate(resourceGroupName(), name());
        return new DeploymentExportResultImpl(inner);
    }

    // Withers

    @Override
    public DeploymentImpl withNewResourceGroup(String resourceGroupName, Region region) {
        this.creatableResourceGroup = this.resourceManager.resourceGroups()
                .define(resourceGroupName)
                .withRegion(region);
        this.resourceGroupName = resourceGroupName;
        return this;
    }

    @Override
    public DeploymentImpl withNewResourceGroup(Creatable<ResourceGroup> resourceGroupDefinition) {
        this.resourceGroupName = resourceGroupDefinition.name();
        this.creatableResourceGroup = resourceGroupDefinition;
        return this;
    }

    @Override
    public DeploymentImpl withExistingResourceGroup(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
        return this;
    }

    @Override
    public DeploymentImpl withExistingResourceGroup(ResourceGroup resourceGroup) {
        this.resourceGroupName = resourceGroup.name();
        return this;
    }

    @Override
    public DeploymentImpl withTemplate(Object template) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().withTemplate(template);
        this.inner().properties().withTemplateLink(null);
        return this;
    }

    @Override
    public DeploymentImpl withTemplate(String templateJson) throws IOException {
        return withTemplate(objectMapper.readTree(templateJson));
    }

    @Override
    public DeploymentImpl withTemplateLink(String uri, String contentVersion) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().withTemplateLink(new TemplateLink().withUri(uri).withContentVersion(contentVersion));
        this.inner().properties().withTemplate(null);
        return this;
    }

    @Override
    public DeploymentImpl withMode(DeploymentMode mode) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().withMode(mode);
        return this;
    }

    @Override
    public DeploymentImpl withParameters(Object parameters) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().withParameters(parameters);
        this.inner().properties().withParametersLink(null);
        return this;
    }

    @Override
    public DeploymentImpl withParameters(String parametersJson) throws IOException {
        return withParameters(objectMapper.readTree(parametersJson));
    }

    @Override
    public DeploymentImpl withParametersLink(String uri, String contentVersion) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new DeploymentPropertiesExtended());
        }
        this.inner().properties().withParametersLink(new ParametersLink().withUri(uri).withContentVersion(contentVersion));
        this.inner().properties().withParameters(null);
        return this;
    }

    @Override
    public DeploymentImpl beginCreate() throws Exception {
        DeploymentInner inner = new DeploymentInner()
                .withProperties(new DeploymentProperties());
        inner.properties().withMode(mode());
        inner.properties().withTemplate(template());
        inner.properties().withTemplateLink(templateLink());
        inner.properties().withParameters(parameters());
        inner.properties().withParametersLink(parametersLink());
        client.beginCreateOrUpdate(resourceGroupName(), name(), inner);
        return this;
    }

    @Override
    public DeploymentImpl create() throws Exception {
        if (this.creatableResourceGroup != null) {
            this.creatableResourceGroup.create();
        }
        createResource();
        return this;
    }

    @Override
    public Observable<Deployment> createAsync() {
        Observable<Deployment> observable;
        if (this.creatableResourceGroup != null) {
            observable = this.creatableResourceGroup.createAsync()
                    .subscribeOn(Schedulers.io())
                    .flatMap(new Func1<ResourceGroup, Observable<Deployment>>() {
                        @Override
                        public Observable<Deployment> call(ResourceGroup resourceGroup) {
                            return createResourceAsync();
                        }
                    });
        } else {
            observable = createResourceAsync();
        }
        return observable;
    }

    @Override
    public Deployment refresh() throws Exception {
        return null;
    }

    @Override
    public Observable<Deployment> createResourceAsync() {
        DeploymentInner inner = new DeploymentInner()
                .withProperties(new DeploymentProperties());
        inner.properties().withMode(mode());
        inner.properties().withTemplate(template());
        inner.properties().withTemplateLink(templateLink());
        inner.properties().withParameters(parameters());
        inner.properties().withParametersLink(parametersLink());
        return client.createOrUpdateAsync(resourceGroupName(), name(), inner)
                .map(innerToFluentMap(this));
    }

    @Override
    public Observable<Deployment> applyAsync() {
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
        return this.createAsync();
    }
}
