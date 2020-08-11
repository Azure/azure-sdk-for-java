// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.NetworkWatcher;
import com.azure.resourcemanager.network.models.Troubleshooting;
import com.azure.resourcemanager.network.models.TroubleshootingDetails;
import com.azure.resourcemanager.network.models.TroubleshootingParameters;
import com.azure.resourcemanager.network.fluent.inner.TroubleshootingResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.ExecutableImpl;
import java.time.OffsetDateTime;
import java.util.List;
import reactor.core.publisher.Mono;

/** Implementation of Troubleshooting interface. */
class TroubleshootingImpl extends ExecutableImpl<Troubleshooting>
    implements Troubleshooting, Troubleshooting.Definition {

    private final NetworkWatcherImpl parent;
    private TroubleshootingParameters parameters = new TroubleshootingParameters();
    private TroubleshootingResultInner result;

    TroubleshootingImpl(NetworkWatcherImpl parent) {
        this.parent = parent;
    }

    @Override
    public TroubleshootingImpl withTargetResourceId(String targetResourceId) {
        parameters.withTargetResourceId(targetResourceId);
        return this;
    }

    @Override
    public TroubleshootingImpl withStorageAccount(String storageAccountId) {
        parameters.withStorageId(storageAccountId);
        return this;
    }

    @Override
    public TroubleshootingImpl withStoragePath(String storagePath) {
        parameters.withStoragePath(storagePath);
        return this;
    }

    @Override
    public NetworkWatcher parent() {
        return parent;
    }

    @Override
    public Mono<Troubleshooting> executeWorkAsync() {
        return this
            .parent()
            .manager()
            .inner()
            .getNetworkWatchers()
            .getTroubleshootingAsync(parent.resourceGroupName(), parent.name(), parameters)
            .map(
                troubleshootingResultInner -> {
                    TroubleshootingImpl.this.result = troubleshootingResultInner;
                    return TroubleshootingImpl.this;
                });
    }

    // Getters

    @Override
    public String targetResourceId() {
        return parameters.targetResourceId();
    }

    @Override
    public String storageId() {
        return parameters.storageId();
    }

    @Override
    public String storagePath() {
        return parameters.storagePath();
    }

    @Override
    public OffsetDateTime startTime() {
        return result.startTime();
    }

    @Override
    public OffsetDateTime endTime() {
        return result.endTime();
    }

    @Override
    public String code() {
        return result.code();
    }

    @Override
    public List<TroubleshootingDetails> results() {
        return result.results();
    }
}
