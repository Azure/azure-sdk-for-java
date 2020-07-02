// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.inner.AppResourceInner;
import com.azure.resourcemanager.appplatform.models.AppResourceProperties;
import com.azure.resourcemanager.appplatform.models.ManagedIdentityProperties;
import com.azure.resourcemanager.appplatform.models.PersistentDisk;
import com.azure.resourcemanager.appplatform.models.ResourceUploadDefinition;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringAppDeployment;
import com.azure.resourcemanager.appplatform.models.SpringAppDeployments;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.appplatform.models.TemporaryDisk;
import com.azure.resourcemanager.appplatform.models.UserSourceType;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.OffsetDateTime;

public class SpringAppImpl
    extends ExternalChildResourceImpl<SpringApp, AppResourceInner, SpringServiceImpl, SpringService>
    implements SpringApp, SpringApp.Definition, SpringApp.Update {
    private Creatable<SpringAppDeployment> springAppDeploymentToCreate = null;

    SpringAppImpl(String name, SpringServiceImpl parent, AppResourceInner innerObject) {
        super(name, parent, innerObject);
    }

    @Override
    public boolean isPublic() {
        if (inner().properties() == null) {
            return false;
        }
        return inner().properties().publicProperty();
    }

    @Override
    public boolean isHttpsOnly() {
        if (inner().properties() == null) {
            return false;
        }
        return inner().properties().httpsOnly();
    }

    @Override
    public String url() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().url();
    }

    @Override
    public TemporaryDisk temporaryDisk() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().temporaryDisk();
    }

    @Override
    public PersistentDisk persistentDisk() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().persistentDisk();
    }

    @Override
    public ManagedIdentityProperties identity() {
        return inner().identity();
    }

    @Override
    public OffsetDateTime createdTime() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().createdTime();
    }

    @Override
    public String activeDeployment() {
        if (inner().properties() == null) {
            return null;
        }
        return inner().properties().activeDeploymentName();
    }

    @Override
    public SpringAppDeployments deploy() {
        return new SpringAppDeploymentsImpl(this);
    }

    @Override
    public Mono<ResourceUploadDefinition> getResourceUploadUrlAsync() {
        return manager().inner().getApps().getResourceUploadUrlAsync(
            parent().resourceGroupName(), parent().name(), name());
    }

    @Override
    public ResourceUploadDefinition getResourceUploadUrl() {
        return getResourceUploadUrlAsync().block();
    }

    private void ensureProperty() {
        if (inner().properties() == null) {
            inner().withProperties(new AppResourceProperties());
        }
    }

    @Override
    public SpringAppImpl withPublicEndpoint() {
        ensureProperty();
        inner().properties().withPublicProperty(true);
        return this;
    }

    @Override
    public SpringAppImpl withoutPublicEndpoint() {
        ensureProperty();
        inner().properties().withPublicProperty(false);
        return this;
    }

    @Override
    public SpringAppImpl withCustomDomain(String domain) {
        ensureProperty();
        inner().properties().withFqdn(domain);
        return this;
    }

    @Override
    public SpringAppImpl withoutCustomDomain() {
        ensureProperty();
        inner().properties().withFqdn(null);
        return this;
    }

    @Override
    public SpringAppImpl withHttpsOnly() {
        ensureProperty();
        inner().properties().withHttpsOnly(true);
        return this;
    }

    @Override
    public SpringAppImpl withoutHttpsOnly() {
        ensureProperty();
        inner().properties().withHttpsOnly(false);
        return this;
    }

    @Override
    public SpringAppImpl withTemporaryDisk(int sizeInGB, String mountPath) {
        ensureProperty();
        inner().properties().withTemporaryDisk(new TemporaryDisk().withSizeInGB(sizeInGB).withMountPath(mountPath));
        return this;
    }

    @Override
    public SpringAppImpl withoutTemporaryDisk() {
        ensureProperty();
        inner().properties().withTemporaryDisk(null);
        return this;
    }

    @Override
    public SpringAppImpl withPersistentDisk(int sizeInGB, String mountPath) {
        ensureProperty();
        inner().properties().withPersistentDisk(new PersistentDisk().withSizeInGB(sizeInGB).withMountPath(mountPath));
        return this;
    }

    @Override
    public SpringAppImpl withoutPersistentDisk() {
        ensureProperty();
        inner().properties().withPersistentDisk(null);
        return this;
    }

    @Override
    public SpringAppImpl withActiveDeployment(String name) {
        ensureProperty();
        inner().properties().withActiveDeploymentName(name);
        return this;
    }

    @Override
    public SpringAppImpl withoutDeployment(String name) {
        this.addPostRunDependent(
            context -> deploy().deleteByNameAsync(name)
                .then(context.voidMono())
        );
        return this;
    }

    @Override
    public SpringAppImpl deployJar(String name, File jarFile) {
        ensureProperty();
        inner().properties().withActiveDeploymentName(name);
        springAppDeploymentToCreate = deploy().define(name)
                .withJarPath(jarFile)
                .withCustomSetting();
        return this;
    }

    @Override
    public SpringAppImpl deploySource(String name, File sourceCode, String targetModule) {
        ensureProperty();
        inner().properties().withActiveDeploymentName(name);
        springAppDeploymentToCreate = deploy().define(name)
                .withSourceCodeFolder(sourceCode)
                .withTargetModule(targetModule)
                .withCustomSetting();
        return this;
    }

    @Override
    public Mono<SpringApp> createResourceAsync() {
        if (springAppDeploymentToCreate == null) {
            String defaultDeploymentName = "default";
            withActiveDeployment(defaultDeploymentName);
            springAppDeploymentToCreate = deploy().define(defaultDeploymentName)
                .withExistingSource(UserSourceType.JAR, String.format("<%s>", defaultDeploymentName))
                .withCustomSetting();
        }
        return manager().inner().getApps().createOrUpdateAsync(
            parent().resourceGroupName(), parent().name(), name(), new AppResourceInner())
            .flatMap(inner -> updateResourceAsync());
    }

    @Override
    public Mono<SpringApp> updateResourceAsync() {
        Mono<?> createDeployment;
        if (springAppDeploymentToCreate != null) {
            createDeployment = springAppDeploymentToCreate.createAsync().last();
        } else {
            createDeployment = Mono.empty();
        }
        return createDeployment
            .then(
                manager().inner().getApps().updateAsync(
                    parent().resourceGroupName(), parent().name(), name(), inner()
                )
                .map(inner -> {
                    setInner(inner);
                    springAppDeploymentToCreate = null;
                    return this;
                }));
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return manager().inner().getApps().deleteAsync(parent().resourceGroupName(), parent().name(), name());
    }

    @Override
    protected Mono<AppResourceInner> getInnerAsync() {
        return manager().inner().getApps().getAsync(parent().resourceGroupName(), parent().name(), name());
    }

    @Override
    public String id() {
        return inner().id();
    }

    @Override
    public SpringAppImpl update() {
        prepareUpdate();
        return this;
    }

    public AppPlatformManager manager() {
        return parent().manager();
    }
}
