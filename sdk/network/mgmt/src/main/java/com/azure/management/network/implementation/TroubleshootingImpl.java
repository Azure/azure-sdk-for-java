/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.NetworkWatcher;
import com.azure.management.network.Troubleshooting;
import com.azure.management.network.TroubleshootingDetails;
import com.azure.management.network.TroubleshootingParameters;
import com.azure.management.network.models.TroubleshootingResultInner;
import com.azure.management.resources.fluentcore.model.implementation.ExecutableImpl;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Implementation of Troubleshooting interface.
 */
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
        return this.parent().manager().inner().networkWatchers()
                .getTroubleshootingAsync(parent.resourceGroupName(), parent.name(), parameters)
                .map(troubleshootingResultInner -> {
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
