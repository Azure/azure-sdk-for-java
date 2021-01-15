// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.models.AppResourceInner;
import com.azure.resourcemanager.appplatform.models.AppResourceProperties;
import com.azure.resourcemanager.appplatform.models.BindingResourceProperties;
import com.azure.resourcemanager.appplatform.models.CustomDomainProperties;
import com.azure.resourcemanager.appplatform.models.ManagedIdentityProperties;
import com.azure.resourcemanager.appplatform.models.PersistentDisk;
import com.azure.resourcemanager.appplatform.models.ResourceUploadDefinition;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringAppDeployment;
import com.azure.resourcemanager.appplatform.models.SpringAppDeployments;
import com.azure.resourcemanager.appplatform.models.SpringAppDomains;
import com.azure.resourcemanager.appplatform.models.SpringAppServiceBindings;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.appplatform.models.TemporaryDisk;
import com.azure.resourcemanager.appplatform.models.UserSourceType;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public class SpringAppImpl
    extends ExternalChildResourceImpl<SpringApp, AppResourceInner, SpringServiceImpl, SpringService>
    implements SpringApp, SpringApp.Definition, SpringApp.Update {
    private Creatable<SpringAppDeployment> springAppDeploymentToCreate = null;
    private final SpringAppDeploymentsImpl deployments = new SpringAppDeploymentsImpl(this);
    private final SpringAppServiceBindingsImpl serviceBindings = new SpringAppServiceBindingsImpl(this);
    private final SpringAppDomainsImpl domains = new SpringAppDomainsImpl(this);

    SpringAppImpl(String name, SpringServiceImpl parent, AppResourceInner innerObject) {
        super(name, parent, innerObject);
    }

    @Override
    public boolean isPublic() {
        if (innerModel().properties() == null) {
            return false;
        }
        return innerModel().properties().publicProperty();
    }

    @Override
    public boolean isHttpsOnly() {
        if (innerModel().properties() == null) {
            return false;
        }
        return innerModel().properties().httpsOnly();
    }

    @Override
    public String url() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().url();
    }

    @Override
    public String fqdn() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().fqdn();
    }

    @Override
    public TemporaryDisk temporaryDisk() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().temporaryDisk();
    }

    @Override
    public PersistentDisk persistentDisk() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().persistentDisk();
    }

    @Override
    public ManagedIdentityProperties identity() {
        return innerModel().identity();
    }

    @Override
    public OffsetDateTime createdTime() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().createdTime();
    }

    @Override
    public String activeDeploymentName() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().activeDeploymentName();
    }

    @Override
    public SpringAppDeployment getActiveDeployment() {
        return getActiveDeploymentAsync().block();
    }

    @Override
    public Mono<SpringAppDeployment> getActiveDeploymentAsync() {
        String activeDeploymentName = activeDeploymentName();
        if (activeDeploymentName == null || activeDeploymentName.isEmpty()) {
            return Mono.empty();
        }
        return deployments().getByNameAsync(activeDeploymentName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends SpringAppDeployment.DefinitionStages.WithCreate<T>> SpringAppDeployments<T> deployments() {
        return (SpringAppDeployments<T>) deployments;
    }

    @Override
    public SpringAppServiceBindings serviceBindings() {
        return serviceBindings;
    }

    @Override
    public SpringAppDomains customDomains() {
        return domains;
    }

    @Override
    public Mono<ResourceUploadDefinition> getResourceUploadUrlAsync() {
        return manager().serviceClient().getApps().getResourceUploadUrlAsync(
            parent().resourceGroupName(), parent().name(), name());
    }

    @Override
    public ResourceUploadDefinition getResourceUploadUrl() {
        return getResourceUploadUrlAsync().block();
    }

    private void ensureProperty() {
        if (innerModel().properties() == null) {
            innerModel().withProperties(new AppResourceProperties());
        }
    }

    @Override
    public SpringAppImpl withDefaultPublicEndpoint() {
        ensureProperty();
        innerModel().properties().withPublicProperty(true);
        return this;
    }

    @Override
    public SpringAppImpl withoutDefaultPublicEndpoint() {
        ensureProperty();
        innerModel().properties().withPublicProperty(false);
        return this;
    }

    @Override
    public SpringAppImpl withCustomDomain(String domain) {
        domains.prepareCreateOrUpdate(domain, new CustomDomainProperties());
        return this;
    }

    @Override
    public SpringAppImpl withCustomDomain(String domain, String certThumbprint) {
        domains.prepareCreateOrUpdate(domain, new CustomDomainProperties().withThumbprint(certThumbprint));
        return this;
    }

    @Override
    public Update withoutCustomDomain(String domain) {
        domains.prepareDelete(domain);
        return this;
    }

    @Override
    public SpringAppImpl withHttpsOnly() {
        ensureProperty();
        innerModel().properties().withHttpsOnly(true);
        return this;
    }

    @Override
    public SpringAppImpl withoutHttpsOnly() {
        ensureProperty();
        innerModel().properties().withHttpsOnly(false);
        return this;
    }

    @Override
    public SpringAppImpl withTemporaryDisk(int sizeInGB, String mountPath) {
        ensureProperty();
        innerModel().properties().withTemporaryDisk(
            new TemporaryDisk().withSizeInGB(sizeInGB).withMountPath(mountPath));
        return this;
    }

    @Override
    public SpringAppImpl withPersistentDisk(int sizeInGB, String mountPath) {
        ensureProperty();
        innerModel().properties().withPersistentDisk(
            new PersistentDisk().withSizeInGB(sizeInGB).withMountPath(mountPath));
        return this;
    }

    @Override
    public SpringAppImpl withActiveDeployment(String name) {
        ensureProperty();
        innerModel().properties().withActiveDeploymentName(name);
        return this;
    }

    @Override
    public Mono<SpringApp> createResourceAsync() {
        if (springAppDeploymentToCreate == null) {
            withDefaultActiveDeployment();
        }
        return manager().serviceClient().getApps().createOrUpdateAsync(
            parent().resourceGroupName(), parent().name(), name(), new AppResourceInner())
            .thenMany(springAppDeploymentToCreate.createAsync())
            .then(updateResourceAsync());
    }

    @Override
    public Mono<SpringApp> updateResourceAsync() {
        return manager().serviceClient().getApps().updateAsync(
            parent().resourceGroupName(), parent().name(), name(), innerModel())
            .map(inner -> {
                setInner(inner);
                return this;
            });
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return manager().serviceClient().getApps().deleteAsync(parent().resourceGroupName(), parent().name(), name());
    }

    @Override
    protected Mono<AppResourceInner> getInnerAsync() {
        return manager().serviceClient().getApps().getAsync(parent().resourceGroupName(), parent().name(), name());
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public SpringAppImpl update() {
        prepareUpdate();
        return this;
    }

    public AppPlatformManager manager() {
        return parent().manager();
    }

    @Override
    public SpringAppImpl withServiceBinding(String name, BindingResourceProperties bindingProperties) {
        serviceBindings.prepareCreateOrUpdate(name, bindingProperties);
        return this;
    }

    @Override
    public SpringAppImpl withoutServiceBinding(String name) {
        serviceBindings.prepareDelete(name);
        return this;
    }

    @Override
    public SpringAppImpl withDefaultActiveDeployment() {
        String defaultDeploymentName = "default";
        withActiveDeployment(defaultDeploymentName);
        springAppDeploymentToCreate = deployments().define(defaultDeploymentName)
            .withExistingSource(UserSourceType.JAR, String.format("<%s>", defaultDeploymentName));
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends
        SpringAppDeployment.DefinitionStages.WithAttach<? extends SpringApp.DefinitionStages.WithCreate, T>>
        SpringAppDeployment.DefinitionStages.Blank<T> defineActiveDeployment(String name) {
        return (SpringAppDeployment.DefinitionStages.Blank<T>) deployments.define(name);
    }

    SpringAppImpl addActiveDeployment(SpringAppDeploymentImpl deployment) {
        withActiveDeployment(deployment.name());
        springAppDeploymentToCreate = deployment;
        return this;
    }
}
