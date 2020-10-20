// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.AzureReachabilityReport;
import com.azure.resourcemanager.network.models.AzureReachabilityReportItem;
import com.azure.resourcemanager.network.models.AzureReachabilityReportLocation;
import com.azure.resourcemanager.network.models.AzureReachabilityReportParameters;
import com.azure.resourcemanager.network.models.NetworkWatcher;
import com.azure.resourcemanager.network.fluent.models.AzureReachabilityReportInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.ExecutableImpl;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import reactor.core.publisher.Mono;

/** The implementation of AzureReachabilityReport. */
class AzureReachabilityReportImpl extends ExecutableImpl<AzureReachabilityReport>
    implements AzureReachabilityReport, AzureReachabilityReport.Definition {
    private final NetworkWatcherImpl parent;
    private AzureReachabilityReportParameters parameters = new AzureReachabilityReportParameters();
    private AzureReachabilityReportInner inner;

    AzureReachabilityReportImpl(NetworkWatcherImpl parent) {
        this.parent = parent;
    }

    @Override
    public String aggregationLevel() {
        return inner.aggregationLevel();
    }

    @Override
    public AzureReachabilityReportLocation providerLocation() {
        return inner.providerLocation();
    }

    @Override
    public List<AzureReachabilityReportItem> reachabilityReport() {
        return Collections.unmodifiableList(inner.reachabilityReport());
    }

    @Override
    public AzureReachabilityReportParameters azureReachabilityReportParameters() {
        return parameters;
    }

    @Override
    public NetworkWatcher parent() {
        return parent;
    }

    @Override
    public AzureReachabilityReportInner innerModel() {
        return this.inner;
    }

    @Override
    public Mono<AzureReachabilityReport> executeWorkAsync() {
        return this
            .parent()
            .manager()
            .serviceClient()
            .getNetworkWatchers()
            .getAzureReachabilityReportAsync(parent().resourceGroupName(), parent().name(), parameters)
            .map(
                azureReachabilityReportListInner -> {
                    AzureReachabilityReportImpl.this.inner = azureReachabilityReportListInner;
                    return AzureReachabilityReportImpl.this;
                });
    }

    @Override
    public AzureReachabilityReportImpl withProviderLocation(String country) {
        parameters.withProviderLocation(new AzureReachabilityReportLocation().withCountry(country));
        return this;
    }

    @Override
    public AzureReachabilityReportImpl withProviderLocation(String country, String state) {
        parameters.withProviderLocation(new AzureReachabilityReportLocation().withCountry(country).withState(state));
        return this;
    }

    @Override
    public AzureReachabilityReportImpl withProviderLocation(String country, String state, String city) {
        parameters
            .withProviderLocation(
                new AzureReachabilityReportLocation().withCountry(country).withState(state).withCity(city));
        return this;
    }

    @Override
    public AzureReachabilityReportImpl withStartTime(OffsetDateTime startTime) {
        parameters.withStartTime(startTime);
        return this;
    }

    @Override
    public AzureReachabilityReportImpl withEndTime(OffsetDateTime endTime) {
        parameters.withEndTime(endTime);
        return this;
    }

    @Override
    public DefinitionStages.WithExecute withAzureLocations(String... azureLocations) {
        parameters.withAzureLocations(Arrays.asList(azureLocations));
        return this;
    }

    @Override
    public DefinitionStages.WithExecute withProviders(String... providers) {
        parameters.withProviders(Arrays.asList(providers));
        return this;
    }
}
