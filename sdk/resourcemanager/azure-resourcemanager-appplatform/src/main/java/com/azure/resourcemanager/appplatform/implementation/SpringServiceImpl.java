// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.inner.ConfigServerResourceInner;
import com.azure.resourcemanager.appplatform.fluent.inner.MonitoringSettingResourceInner;
import com.azure.resourcemanager.appplatform.fluent.inner.ServiceResourceInner;
import com.azure.resourcemanager.appplatform.models.CertificateProperties;
import com.azure.resourcemanager.appplatform.models.ConfigServerGitProperty;
import com.azure.resourcemanager.appplatform.models.ConfigServerProperties;
import com.azure.resourcemanager.appplatform.models.ConfigServerSettings;
import com.azure.resourcemanager.appplatform.models.MonitoringSettingProperties;
import com.azure.resourcemanager.appplatform.models.Sku;
import com.azure.resourcemanager.appplatform.models.SpringApps;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.appplatform.models.SpringServiceCertificates;
import com.azure.resourcemanager.appplatform.models.TestKeyType;
import com.azure.resourcemanager.appplatform.models.TestKeys;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.dag.FunctionalTaskItem;
import reactor.core.publisher.Mono;

public class SpringServiceImpl
    extends GroupableResourceImpl<SpringService, ServiceResourceInner, SpringServiceImpl, AppPlatformManager>
    implements SpringService, SpringService.Definition, SpringService.Update {
    private final SpringServiceCertificatesImpl certificates = new SpringServiceCertificatesImpl(this);
    private final SpringAppsImpl apps = new SpringAppsImpl(this);
    private FunctionalTaskItem configServerTask = null;
    private FunctionalTaskItem monitoringSettingTask = null;
    private boolean needUpdate = false;

    SpringServiceImpl(String name, ServiceResourceInner innerObject, AppPlatformManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public Sku sku() {
        return inner().sku();
    }

    @Override
    public SpringApps apps() {
        return apps;
    }

    @Override
    public SpringServiceCertificates certificates() {
        return certificates;
    }

    @Override
    public MonitoringSettingProperties getMonitoringSetting() {
        return getMonitoringSettingAsync().block();
    }

    @Override
    public Mono<MonitoringSettingProperties> getMonitoringSettingAsync() {
        return manager().inner().getMonitoringSettings().getAsync(resourceGroupName(), name())
            .map(MonitoringSettingResourceInner::properties);
    }

    @Override
    public ConfigServerProperties getServerProperties() {
        return getServerPropertiesAsync().block();
    }

    @Override
    public Mono<ConfigServerProperties> getServerPropertiesAsync() {
        return manager().inner().getConfigServers().getAsync(resourceGroupName(), name())
            .map(ConfigServerResourceInner::properties);
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
        needUpdate = true;
        inner().withSku(sku);
        return this;
    }

    @Override
    public SpringServiceImpl withTracing(String appInsightInstrumentationKey) {
        monitoringSettingTask =
            context -> manager().inner().getMonitoringSettings()
                .updatePatchAsync(resourceGroupName(), name(), new MonitoringSettingProperties()
                    .withAppInsightsInstrumentationKey(appInsightInstrumentationKey)
                    .withTraceEnabled(true))
                .then(context.voidMono());
        return this;
    }

    @Override
    public SpringServiceImpl withoutTracing() {
        monitoringSettingTask =
            context -> manager().inner().getMonitoringSettings()
                .updatePatchAsync(
                    resourceGroupName(), name(), new MonitoringSettingProperties().withTraceEnabled(false))
                .then(context.voidMono());
        return this;
    }

    @Override
    public SpringServiceImpl withGitUri(String uri) {
        configServerTask =
            context -> manager().inner().getConfigServers()
                .updatePatchAsync(resourceGroupName(), name(), new ConfigServerProperties()
                    .withConfigServer(new ConfigServerSettings().withGitProperty(
                        new ConfigServerGitProperty().withUri(uri)
                    )))
                .then(context.voidMono());
        return this;
    }

    @Override
    public SpringServiceImpl withGitUriAndCredential(String uri, String username, String password) {
        configServerTask =
            context -> manager().inner().getConfigServers()
                .updatePatchAsync(resourceGroupName(), name(), new ConfigServerProperties()
                    .withConfigServer(new ConfigServerSettings().withGitProperty(
                        new ConfigServerGitProperty()
                            .withUri(uri)
                            .withUsername(username)
                            .withPassword(password)
                    )))
                .then(context.voidMono());
        return this;
    }

    @Override
    public SpringServiceImpl withGitConfig(ConfigServerGitProperty gitConfig) {
        configServerTask =
            context -> manager().inner().getConfigServers()
                .updatePatchAsync(resourceGroupName(), name(), new ConfigServerProperties()
                    .withConfigServer(new ConfigServerSettings().withGitProperty(gitConfig)))
                .then(context.voidMono());
        return this;
    }

    @Override
    public SpringServiceImpl withoutGitConfig() {
        configServerTask =
            context -> manager().inner().getConfigServers()
                .updatePatchAsync(resourceGroupName(), name(), new ConfigServerProperties())
                .then(context.voidMono());
        return this;
    }

    @Override
    public void beforeGroupCreateOrUpdate() {
        if (configServerTask != null) {
            this.addPostRunDependent(configServerTask);
        }
        if (monitoringSettingTask != null) {
            this.addPostRunDependent(monitoringSettingTask);
        }
        configServerTask = null;
        monitoringSettingTask = null;
    }

    @Override
    public Mono<SpringService> createResourceAsync() {
        Mono<ServiceResourceInner> createOrUpdate;
        if (isInCreateMode()) {
            createOrUpdate = manager().inner().getServices().createOrUpdateAsync(resourceGroupName(), name(), inner());
        } else if (needUpdate) {
            needUpdate = false;
            createOrUpdate = manager().inner().getServices().updateAsync(resourceGroupName(), name(), inner());
        } else {
            return Mono.just(this);
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

    @Override
    public SpringServiceImpl withCertificate(String name, String keyVaultUri, String certNameInKeyVault) {
        certificates.prepareCreateOrUpdate(
            name,
            new CertificateProperties().withVaultUri(keyVaultUri).withKeyVaultCertName(certNameInKeyVault)
        );
        return this;
    }

    @Override
    public SpringServiceImpl withCertificate(String name, String keyVaultUri,
                                             String certNameInKeyVault, String certVersion) {
        certificates.prepareCreateOrUpdate(
            name,
            new CertificateProperties()
                .withVaultUri(keyVaultUri)
                .withKeyVaultCertName(certNameInKeyVault)
                .withCertVersion(certVersion)
        );
        return this;
    }

    @Override
    public SpringServiceImpl withoutCertificate(String name) {
        certificates.prepareDelete(name);
        return this;
    }
}
