// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.models.ConfigurationServiceResourceInner;
import com.azure.resourcemanager.appplatform.models.ConfigurationServiceGitProperty;
import com.azure.resourcemanager.appplatform.models.ConfigurationServiceGitRepository;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringConfigurationService;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpringConfigurationServiceImpl
    extends ExternalChildResourceImpl<SpringConfigurationService, ConfigurationServiceResourceInner, SpringServiceImpl, SpringService>
    implements SpringConfigurationService {
    protected SpringConfigurationServiceImpl(String name, SpringServiceImpl parent, ConfigurationServiceResourceInner innerObject) {
        super(name, parent, innerObject);
    }

    @Override
    public Double cpu() {
        return Utils.fromCpuString(innerModel().properties().resourceRequests().cpu());
    }

    @Override
    public Double memory() {
        return Utils.fromMemoryString(innerModel().properties().resourceRequests().memory());
    }

    @Override
    public String gitUri() {
        return findDefaultRepository()
            .map(ConfigurationServiceGitRepository::uri)
            .orElse(null);
    }

    @Override
    public List<String> filePatterns() {
        return findDefaultRepository()
            .map(ConfigurationServiceGitRepository::patterns)
            .orElse(Collections.emptyList());
    }

    public String branch() {
        return findDefaultRepository()
            .map(ConfigurationServiceGitRepository::label)
            .orElse(null);
    }

    @Override
    public ConfigurationServiceGitRepository getGitRepository(String name) {
        return findRepository(name).orElse(null);
    }

    private Optional<ConfigurationServiceGitRepository> findDefaultRepository() {
        return findRepository(Constants.DEFAULT_TANZU_COMPONENT_NAME);
    }

    @Override
    public List<SpringApp> getAppBindings() {
        return parent().apps().list().stream().filter(SpringApp::hasConfigurationServiceBinding).collect(Collectors.toList());
    }

    private Optional<ConfigurationServiceGitRepository> findRepository(String name) {
        if (name == null || innerModel().properties() == null || innerModel().properties().settings() == null) {
            return Optional.empty();
        }
        ConfigurationServiceGitProperty property = innerModel().properties().settings().gitProperty();
        if (property != null && property.repositories() != null) {
            return property.repositories()
                .stream()
                .filter(repository -> name.equals(repository.name()))
                .findFirst();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public Mono<SpringConfigurationService> createResourceAsync() {
        return manager().serviceClient().getConfigurationServices()
            .createOrUpdateAsync(parent().resourceGroupName(), parent().name(), name(), innerModel())
            .map(inner -> {
                setInner(inner);
                return this;
            });
    }

    @Override
    public Mono<SpringConfigurationService> updateResourceAsync() {
        return createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return manager().serviceClient().getConfigurationServices().deleteAsync(parent().resourceGroupName(), parent().name(), name());
    }

    @Override
    protected Mono<ConfigurationServiceResourceInner> getInnerAsync() {
        return manager().serviceClient().getConfigurationServices().getAsync(parent().resourceGroupName(), parent().name(), name());
    }

    public AppPlatformManager manager() {
        return parent().manager();
    }
}
