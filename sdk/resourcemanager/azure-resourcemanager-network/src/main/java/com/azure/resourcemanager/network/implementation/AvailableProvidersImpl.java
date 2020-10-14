// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.AvailableProviders;
import com.azure.resourcemanager.network.models.AvailableProvidersListCountry;
import com.azure.resourcemanager.network.models.AvailableProvidersListParameters;
import com.azure.resourcemanager.network.models.NetworkWatcher;
import com.azure.resourcemanager.network.fluent.models.AvailableProvidersListInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.ExecutableImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import reactor.core.publisher.Mono;

/** The implementation of AvailableProviders. */
class AvailableProvidersImpl extends ExecutableImpl<AvailableProviders>
    implements AvailableProviders, AvailableProviders.Definition {
    private Map<String, AvailableProvidersListCountry> providersByCountry;
    private final NetworkWatcherImpl parent;
    private AvailableProvidersListParameters parameters = new AvailableProvidersListParameters();
    private AvailableProvidersListInner inner;

    AvailableProvidersImpl(NetworkWatcherImpl parent) {
        this.parent = parent;
    }

    @Override
    public AvailableProvidersListParameters availableProvidersParameters() {
        return parameters;
    }

    @Override
    public Map<String, AvailableProvidersListCountry> providersByCountry() {
        return Collections.unmodifiableMap(this.providersByCountry);
    }

    private void initializeResourcesFromInner() {
        this.providersByCountry = new TreeMap<>();
        List<AvailableProvidersListCountry> availableProvidersList = this.innerModel().countries();
        if (availableProvidersList != null) {
            for (AvailableProvidersListCountry resource : availableProvidersList) {
                this.providersByCountry.put(resource.countryName(), resource);
            }
        }
    }

    @Override
    public NetworkWatcher parent() {
        return parent;
    }

    @Override
    public AvailableProvidersListInner innerModel() {
        return this.inner;
    }

    @Override
    public Mono<AvailableProviders> executeWorkAsync() {
        return this
            .parent()
            .manager()
            .serviceClient()
            .getNetworkWatchers()
            .listAvailableProvidersAsync(parent().resourceGroupName(), parent().name(), parameters)
            .map(
                availableProvidersListInner -> {
                    AvailableProvidersImpl.this.inner = availableProvidersListInner;
                    AvailableProvidersImpl.this.initializeResourcesFromInner();
                    return AvailableProvidersImpl.this;
                });
    }

    @Override
    public AvailableProvidersImpl withAzureLocations(String... azureLocations) {
        parameters.withAzureLocations(Arrays.asList(azureLocations));
        return this;
    }

    @Override
    public AvailableProvidersImpl withAzureLocation(String azureLocation) {
        if (parameters.azureLocations() == null) {
            parameters.withAzureLocations(new ArrayList<String>());
        }
        parameters.azureLocations().add(azureLocation);
        return this;
    }

    @Override
    public AvailableProvidersImpl withCountry(String country) {
        parameters.withCountry(country);
        return this;
    }

    @Override
    public AvailableProvidersImpl withState(String state) {
        parameters.withState(state);
        return this;
    }

    @Override
    public AvailableProvidersImpl withCity(String city) {
        parameters.withCity(city);
        return this;
    }
}
