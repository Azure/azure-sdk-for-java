// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.inner.ServiceResourceInner;
import com.azure.resourcemanager.appplatform.models.ClusterResourceProperties;
import com.azure.resourcemanager.appplatform.models.ConfigServerGitProperty;
import com.azure.resourcemanager.appplatform.models.ConfigServerProperties;
import com.azure.resourcemanager.appplatform.models.ConfigServerSettings;
import com.azure.resourcemanager.appplatform.models.Sku;
import com.azure.resourcemanager.appplatform.models.SpringApps;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.appplatform.models.TestKeyType;
import com.azure.resourcemanager.appplatform.models.TestKeys;
import com.azure.resourcemanager.appplatform.models.TraceProperties;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

public class SpringServiceImpl
    extends GroupableResourceImpl<SpringService, ServiceResourceInner, SpringServiceImpl, AppPlatformManager>
    implements SpringService, SpringService.Definition, SpringService.Update {

    SpringServiceImpl(String name, ServiceResourceInner innerObject, AppPlatformManager manager) {
        super(name, innerObject, manager);
    }
    @Override
    public Sku sku() {
        return inner().sku();
    }

    @Override
    public TraceProperties traceProperties() {
        return inner().properties().trace();
    }

    @Override
    public ConfigServerProperties serverProperties() {
        return inner().properties().configServerProperties();
    }

    @Override
    public SpringApps apps() {
        return new SpringAppsImpl(this);
    }

    @Override
    public TestKeys listTestKeys() {
        return listTestKeysAsync().block();
    }

    @Override
    public Mono<TestKeys> listTestKeysAsync() {
        return manager().inner().getServices().listTestKeysAsync(resourceGroupName(), name());
    }

    @Override
    public TestKeys regenerateTestKeys(TestKeyType keyType) {
        return regenerateTestKeysAsync(keyType).block();
    }

    @Override
    public Mono<TestKeys> regenerateTestKeysAsync(TestKeyType keyType) {
        return manager().inner().getServices().regenerateTestKeyAsync(resourceGroupName(), name(), keyType);
    }

    @Override
    public void disableTestEndpoint() {
        disableTestEndpointAsync().block();
    }

    @Override
    public Mono<Void> disableTestEndpointAsync() {
        return manager().inner().getServices().disableTestEndpointAsync(resourceGroupName(), name());
    }

    @Override
    public TestKeys enableTestEndpoint() {
        return enableTestEndpointAsync().block();
    }

    @Override
    public Mono<TestKeys> enableTestEndpointAsync() {
        return manager().inner().getServices().enableTestEndpointAsync(resourceGroupName(), name());
    }

    @Override
    public SpringServiceImpl withSku(String skuName) {
        return withSku(new Sku().withName(skuName));
    }

    @Override
    public SpringServiceImpl withSku(String skuName, int capacity) {
        return withSku(new Sku().withName(skuName).withCapacity(capacity));
    }

    @Override
    public SpringServiceImpl withSku(Sku sku) {
        inner().withSku(sku);
        return this;
    }

    @Override
    public SpringServiceImpl withTracing(String appInsightInstrumentationKey) {
        if (inner().properties() == null) {
            inner().withProperties(new ClusterResourceProperties());
        }
        inner().properties().withTrace(
            new TraceProperties().withAppInsightInstrumentationKey(appInsightInstrumentationKey).withEnabled(true));
        return this;
    }

    @Override
    public SpringServiceImpl withoutTracing() {
        if (inner().properties() == null) {
            inner().withProperties(new ClusterResourceProperties());
        }
        inner().properties().withTrace(new TraceProperties().withEnabled(false));
        return this;
    }

    private void ensureGitConfig() {
        if (inner().properties() == null) {
            inner().withProperties(new ClusterResourceProperties());
        }
        if (inner().properties().configServerProperties() == null) {
            inner().properties().withConfigServerProperties(new ConfigServerProperties());
        }
        if (inner().properties().configServerProperties().configServer() == null) {
            inner().properties().configServerProperties().withConfigServer(new ConfigServerSettings());
        }
        if (inner().properties().configServerProperties().configServer().gitProperty() == null) {
            inner().properties().configServerProperties().configServer().withGitProperty(new ConfigServerGitProperty());
        }
    }

    @Override
    public SpringServiceImpl withGitUri(String uri) {
        ensureGitConfig();
        inner().properties().configServerProperties().configServer().gitProperty().withUri(uri);
        return this;
    }

    @Override
    public SpringServiceImpl withGitUriAndCredential(String uri, String username, String password) {
        ensureGitConfig();
        inner().properties().configServerProperties().configServer().gitProperty()
            .withUri(uri).withUsername(username).withPassword(password);
        return this;
    }

    @Override
    public SpringServiceImpl withGitConfig(ConfigServerGitProperty gitConfig) {
        ensureGitConfig();
        inner().properties().configServerProperties().configServer().withGitProperty(gitConfig);
        return this;
    }

    @Override
    public SpringServiceImpl withoutGitConfig() {
        ensureGitConfig();
        inner().properties().configServerProperties().configServer().withGitProperty(null);
        return this;
    }

    @Override
    public Mono<SpringService> createResourceAsync() {
        Mono<ServiceResourceInner> createOrUpdate;
        if (isInCreateMode()) {
            createOrUpdate = manager().inner().getServices().createOrUpdateAsync(resourceGroupName(), name(), inner());
        } else {
            createOrUpdate = manager().inner().getServices().updateAsync(resourceGroupName(), name(), inner());
        }
        return createOrUpdate
            .map(inner -> {
                this.setInner(inner);
                return this;
            });
    }

    @Override
    protected Mono<ServiceResourceInner> getInnerAsync() {
        return manager().inner().getServices().getByResourceGroupAsync(resourceGroupName(), name());
    }
}
