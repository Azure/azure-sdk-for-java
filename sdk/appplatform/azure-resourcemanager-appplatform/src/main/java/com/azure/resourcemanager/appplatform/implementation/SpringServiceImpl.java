// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.models.BuildServiceAgentPoolResourceInner;
import com.azure.resourcemanager.appplatform.fluent.models.ConfigServerResourceInner;
import com.azure.resourcemanager.appplatform.fluent.models.MonitoringSettingResourceInner;
import com.azure.resourcemanager.appplatform.fluent.models.ServiceResourceInner;
import com.azure.resourcemanager.appplatform.models.BuildServiceAgentPoolProperties;
import com.azure.resourcemanager.appplatform.models.BuildServiceAgentPoolSizeProperties;
import com.azure.resourcemanager.appplatform.models.ConfigServerGitProperty;
import com.azure.resourcemanager.appplatform.models.ConfigServerProperties;
import com.azure.resourcemanager.appplatform.models.ConfigServerSettings;
import com.azure.resourcemanager.appplatform.models.ConfigurationServiceGitProperty;
import com.azure.resourcemanager.appplatform.models.ConfigurationServiceGitRepository;
import com.azure.resourcemanager.appplatform.models.KeyVaultCertificateProperties;
import com.azure.resourcemanager.appplatform.models.MonitoringSettingProperties;
import com.azure.resourcemanager.appplatform.models.RegenerateTestKeyRequestPayload;
import com.azure.resourcemanager.appplatform.models.Sku;
import com.azure.resourcemanager.appplatform.models.SkuName;
import com.azure.resourcemanager.appplatform.models.SpringApps;
import com.azure.resourcemanager.appplatform.models.SpringConfigurationService;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.appplatform.models.SpringServiceCertificates;
import com.azure.resourcemanager.appplatform.models.SpringServiceRegistry;
import com.azure.resourcemanager.appplatform.models.TestKeyType;
import com.azure.resourcemanager.appplatform.models.TestKeys;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.dag.FunctionalTaskItem;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SpringServiceImpl
    extends GroupableResourceImpl<SpringService, ServiceResourceInner, SpringServiceImpl, AppPlatformManager>
    implements SpringService, SpringService.Definition, SpringService.Update {
    private final SpringServiceCertificatesImpl certificates = new SpringServiceCertificatesImpl(this);
    private final SpringAppsImpl apps = new SpringAppsImpl(this);
    private final SpringConfigurationServicesImpl configurationServices = new SpringConfigurationServicesImpl(this);
    private final SpringServiceRegistriesImpl serviceRegistries = new SpringServiceRegistriesImpl(this);
    private FunctionalTaskItem configServerTask = null;
    private FunctionalTaskItem monitoringSettingTask = null;
    private ServiceResourceInner patchToUpdate = new ServiceResourceInner();
    private boolean updated;
    private final ConfigurationServiceConfig configurationServiceConfig = new ConfigurationServiceConfig();

    SpringServiceImpl(String name, ServiceResourceInner innerObject, AppPlatformManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public SpringServiceImpl update() {
        return super.update();
    }

    @Override
    public Sku sku() {
        return innerModel().sku();
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
        return manager().serviceClient()
            .getMonitoringSettings()
            .getAsync(resourceGroupName(), name())
            .map(MonitoringSettingResourceInner::properties);
    }

    @Override
    public ConfigServerProperties getServerProperties() {
        return getServerPropertiesAsync().block();
    }

    @Override
    public Mono<ConfigServerProperties> getServerPropertiesAsync() {
        return manager().serviceClient()
            .getConfigServers()
            .getAsync(resourceGroupName(), name())
            .map(ConfigServerResourceInner::properties);
    }

    @Override
    public TestKeys listTestKeys() {
        return listTestKeysAsync().block();
    }

    @Override
    public Mono<TestKeys> listTestKeysAsync() {
        return manager().serviceClient().getServices().listTestKeysAsync(resourceGroupName(), name());
    }

    @Override
    public TestKeys regenerateTestKeys(TestKeyType keyType) {
        return regenerateTestKeysAsync(keyType).block();
    }

    @Override
    public Mono<TestKeys> regenerateTestKeysAsync(TestKeyType keyType) {
        return manager().serviceClient()
            .getServices()
            .regenerateTestKeyAsync(resourceGroupName(), name(),
                new RegenerateTestKeyRequestPayload().withKeyType(keyType));
    }

    @Override
    public void disableTestEndpoint() {
        disableTestEndpointAsync().block();
    }

    @Override
    public Mono<Void> disableTestEndpointAsync() {
        return manager().serviceClient().getServices().disableTestEndpointAsync(resourceGroupName(), name());
    }

    @Override
    public TestKeys enableTestEndpoint() {
        return enableTestEndpointAsync().block();
    }

    @Override
    public Mono<TestKeys> enableTestEndpointAsync() {
        return manager().serviceClient().getServices().enableTestEndpointAsync(resourceGroupName(), name());
    }

    @Override
    public SpringConfigurationService getDefaultConfigurationService() {
        return manager().serviceClient()
            .getConfigurationServices()
            .list(resourceGroupName(), name())
            .stream()
            .filter(inner -> Objects.equals(inner.name(), Constants.DEFAULT_TANZU_COMPONENT_NAME))
            .map(inner -> new SpringConfigurationServiceImpl(inner.name(), this, inner))
            .findFirst()
            .orElse(null);
    }

    @Override
    public SpringServiceRegistry getDefaultServiceRegistry() {
        return manager().serviceClient()
            .getServiceRegistries()
            .list(resourceGroupName(), name())
            .stream()
            .filter(inner -> Objects.equals(inner.name(), Constants.DEFAULT_TANZU_COMPONENT_NAME))
            .map(inner -> new SpringServiceRegistryImpl(inner.name(), this, inner))
            .findFirst()
            .orElse(null);
    }

    @Override
    public SpringServiceImpl withSku(String skuName) {
        return withSku(new Sku().withName(skuName));
    }

    @Override
    public SpringServiceImpl withSku(SkuName skuName) {
        return withSku(skuName.toString());
    }

    @Override
    public SpringServiceImpl withSku(String skuName, int capacity) {
        return withSku(new Sku().withName(skuName).withCapacity(capacity));
    }

    @Override
    public SpringServiceImpl withSku(Sku sku) {
        innerModel().withSku(sku);
        if (isInUpdateMode()) {
            patchToUpdate.withSku(sku);
            updated = true;
        }
        return this;
    }

    @Override
    public SpringServiceImpl withEnterpriseTierSku() {
        withSku(SkuName.E0);
        return this;
    }

    @Override
    public SpringServiceImpl withTracing(String appInsightInstrumentationKey) {
        monitoringSettingTask
            = context -> manager().serviceClient()
                .getMonitoringSettings()
                .updatePatchAsync(resourceGroupName(), name(),
                    new MonitoringSettingResourceInner().withProperties(new MonitoringSettingProperties()
                        .withAppInsightsInstrumentationKey(appInsightInstrumentationKey)
                        .withTraceEnabled(true)))
                .then(context.voidMono());
        return this;
    }

    @Override
    public SpringServiceImpl withoutTracing() {
        monitoringSettingTask = context -> manager().serviceClient()
            .getMonitoringSettings()
            .updatePatchAsync(resourceGroupName(), name(),
                new MonitoringSettingResourceInner()
                    .withProperties(new MonitoringSettingProperties().withTraceEnabled(false)))
            .then(context.voidMono());
        return this;
    }

    @Override
    public SpringServiceImpl withGitUri(String uri) {
        configServerTask = context -> manager().serviceClient()
            .getConfigServers()
            .updatePatchAsync(resourceGroupName(), name(),
                new ConfigServerResourceInner().withProperties(new ConfigServerProperties().withConfigServer(
                    new ConfigServerSettings().withGitProperty(new ConfigServerGitProperty().withUri(uri)))))
            .then(context.voidMono());
        return this;
    }

    @Override
    public SpringServiceImpl withGitUriAndCredential(String uri, String username, String password) {
        configServerTask = context -> manager().serviceClient()
            .getConfigServers()
            .updatePatchAsync(resourceGroupName(), name(),
                new ConfigServerResourceInner().withProperties(
                    new ConfigServerProperties().withConfigServer(new ConfigServerSettings().withGitProperty(
                        new ConfigServerGitProperty().withUri(uri).withUsername(username).withPassword(password)))))
            .then(context.voidMono());
        return this;
    }

    @Override
    public SpringServiceImpl withGitConfig(ConfigServerGitProperty gitConfig) {
        configServerTask = context -> manager().serviceClient()
            .getConfigServers()
            .updatePatchAsync(resourceGroupName(), name(),
                new ConfigServerResourceInner().withProperties(new ConfigServerProperties()
                    .withConfigServer(new ConfigServerSettings().withGitProperty(gitConfig))))
            .then(context.voidMono());
        return this;
    }

    @Override
    public SpringServiceImpl withoutGitConfig() {
        return withGitConfig(null);
    }

    @Override
    public void beforeGroupCreateOrUpdate() {
        if (configServerTask != null) {
            this.addPostRunDependent(configServerTask);
        }
        if (monitoringSettingTask != null) {
            this.addPostRunDependent(monitoringSettingTask);
        }
        if (isEnterpriseTier()) {
            if (configurationServiceConfig.needCreateOrUpdate()) {
                prepareCreateOrUpdateConfigurationService();
                configurationServiceConfig.clearUpdate();
            }
            if (isInCreateMode()) {
                prepareCreateServiceRegistry();
            }
        }
        configServerTask = null;
        monitoringSettingTask = null;
    }

    @Override
    public Mono<SpringService> createResourceAsync() {
        Mono<ServiceResourceInner> createOrUpdate;
        if (isInCreateMode()) {
            createOrUpdate = manager().serviceClient()
                .getServices()
                .createOrUpdateAsync(resourceGroupName(), name(), innerModel());
            if (isEnterpriseTier()) {
                createOrUpdate = createOrUpdate
                    // initialize build service agent pool
                    .flatMap(inner -> manager().serviceClient()
                        .getBuildServiceAgentPools()
                        .updatePutAsync(resourceGroupName(), name(), Constants.DEFAULT_TANZU_COMPONENT_NAME,
                            Constants.DEFAULT_TANZU_COMPONENT_NAME,
                            new BuildServiceAgentPoolResourceInner()
                                .withProperties(new BuildServiceAgentPoolProperties()
                                    .withPoolSize(new BuildServiceAgentPoolSizeProperties().withName("S1"))) // S1, S2, S3, S4, S5.
                        )
                        .then(Mono.just(inner)));
            }
        } else if (updated) {
            createOrUpdate
                = manager().serviceClient().getServices().updateAsync(resourceGroupName(), name(), patchToUpdate);
            patchToUpdate = new ServiceResourceInner();
            updated = false;
        } else {
            return Mono.just(this);
        }
        return createOrUpdate.map(inner -> {
            this.setInner(inner);
            return this;
        });
    }

    @Override
    public Mono<Void> afterPostRunAsync(boolean isGroupFaulted) {
        return Mono.just(true).map(ignored -> {
            clearCache();
            return ignored;
        }).then();
    }

    @Override
    protected Mono<ServiceResourceInner> getInnerAsync() {
        return manager().serviceClient()
            .getServices()
            .getByResourceGroupAsync(resourceGroupName(), name())
            .map(inner -> {
                clearCache();
                return inner;
            });
    }

    @Override
    public SpringServiceImpl withCertificate(String name, String keyVaultUri, String certNameInKeyVault) {
        certificates.prepareCreateOrUpdate(name,
            new KeyVaultCertificateProperties().withVaultUri(keyVaultUri).withKeyVaultCertName(certNameInKeyVault));
        return this;
    }

    @Override
    public SpringServiceImpl withCertificate(String name, String keyVaultUri, String certNameInKeyVault,
        String certVersion) {
        certificates.prepareCreateOrUpdate(name,
            new KeyVaultCertificateProperties().withVaultUri(keyVaultUri)
                .withKeyVaultCertName(certNameInKeyVault)
                .withCertVersion(certVersion));
        return this;
    }

    @Override
    public SpringServiceImpl withoutCertificate(String name) {
        certificates.prepareDelete(name);
        return this;
    }

    @Override
    public SpringServiceImpl withDefaultGitRepository(String uri, String branch, List<String> filePatterns) {
        return withGitRepository(Constants.DEFAULT_TANZU_COMPONENT_NAME, uri, branch, filePatterns);
    }

    @Override
    public SpringServiceImpl withGitRepository(String name, String uri, String branch, List<String> filePatterns) {
        if (CoreUtils.isNullOrEmpty(name)) {
            return this;
        }
        this.configurationServiceConfig.addRepository(new ConfigurationServiceGitRepository().withName(name)
            .withUri(uri)
            .withPatterns(filePatterns)
            .withLabel(branch));
        return this;
    }

    @Override
    public SpringServiceImpl withGitRepositoryConfig(ConfigurationServiceGitProperty gitConfig) {
        this.configurationServiceConfig.clearRepositories();
        if (gitConfig != null && !CoreUtils.isNullOrEmpty(gitConfig.repositories())) {
            for (ConfigurationServiceGitRepository repository : gitConfig.repositories()) {
                this.configurationServiceConfig.addRepository(repository);
            }
        }
        return this;
    }

    @Override
    public SpringServiceImpl withoutGitRepository(String name) {
        this.configurationServiceConfig.removeRepository(name);
        return this;
    }

    @Override
    public SpringServiceImpl withoutGitRepositories() {
        this.configurationServiceConfig.clearRepositories();
        return this;
    }

    private void prepareCreateOrUpdateConfigurationService() {
        List<ConfigurationServiceGitRepository> repositories = this.configurationServiceConfig.mergeRepositories();
        this.configurationServices
            .prepareCreateOrUpdate(new ConfigurationServiceGitProperty().withRepositories(repositories));
    }

    private void prepareCreateServiceRegistry() {
        this.serviceRegistries.prepareCreate();
    }

    private boolean isInUpdateMode() {
        return !isInCreateMode();
    }

    boolean isEnterpriseTier() {
        return innerModel().sku() != null && SkuName.E0.toString().equals(innerModel().sku().name());
    }

    private void clearCache() {
        this.configurationServices.clear();
        this.configurationServiceConfig.reset();
        this.serviceRegistries.clear();
    }

    // Configuration Service config for Enterprise Tier
    private class ConfigurationServiceConfig {
        private final Map<String, ConfigurationServiceGitRepository> gitRepositoryMap = new ConcurrentHashMap<>();
        private final Set<String> repositoriesToDelete = new HashSet<>();
        private boolean update;
        private boolean clearRepositories;

        boolean needCreateOrUpdate() {
            return update;
        }

        public void clearUpdate() {
            this.update = false;
        }

        void reset() {
            this.gitRepositoryMap.clear();
            this.update = false;
            this.repositoriesToDelete.clear();
            this.clearRepositories = false;
        }

        public void addRepository(ConfigurationServiceGitRepository repository) {
            this.gitRepositoryMap.putIfAbsent(repository.name(), repository);
            this.update = true;
        }

        public void clearRepositories() {
            this.gitRepositoryMap.clear();
            this.clearRepositories = true;
            this.update = true;
        }

        public void removeRepository(String name) {
            this.repositoriesToDelete.add(name);
            this.update = true;
        }

        public List<ConfigurationServiceGitRepository> mergeRepositories() {
            if (this.clearRepositories) {
                // in case addRepository() is called after calling clearRepositories()
                return new ArrayList<>(this.gitRepositoryMap.values());
            } else {
                Map<String, ConfigurationServiceGitRepository> existingGitRepositories = new HashMap<>();
                if (isInUpdateMode()) {
                    // get existing git repositories
                    SpringConfigurationService configurationService = getDefaultConfigurationService();
                    if (configurationService != null) {
                        List<ConfigurationServiceGitRepository> repositoryList
                            = configurationService.innerModel().properties().settings() == null
                                ? Collections.emptyList()
                                : configurationService.innerModel()
                                    .properties()
                                    .settings()
                                    .gitProperty()
                                    .repositories();
                        if (repositoryList != null) {
                            repositoryList
                                .forEach(repository -> existingGitRepositories.put(repository.name(), repository));
                        }
                    }
                }
                // merge with updated ones
                existingGitRepositories.putAll(gitRepositoryMap);
                for (String repositoryToDelete : repositoriesToDelete) {
                    existingGitRepositories.remove(repositoryToDelete);
                }
                return new ArrayList<>(existingGitRepositories.values());
            }
        }
    }
}
