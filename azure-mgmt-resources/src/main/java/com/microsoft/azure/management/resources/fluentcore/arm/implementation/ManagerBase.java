/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.implementation;

import com.microsoft.azure.PollingState;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.resources.fluentcore.arm.CompletableOperationPollingState;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.RestClient;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

import java.lang.reflect.Type;

/**
 * Base class for Azure resource managers.
 */
public abstract class ManagerBase {

    private ResourceManager resourceManager;
    private final String subscriptionId;

    protected ManagerBase(RestClient restClient, String subscriptionId) {
        if (restClient != null) {
            this.resourceManager = ResourceManager.authenticate(restClient).withSubscription(subscriptionId);
        }
        this.subscriptionId = subscriptionId;
    }

    /**
     * @return the ID of the subscription the manager is working with
     */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    protected final void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * @return the {@link ResourceManager} associated with this manager
     */
    public ResourceManager resourceManager() {
        return this.resourceManager;
    }

    /**
     * Given a polling state representing state of a LRO operation, this method returns {@link Single} object,
     * when subscribed to it, a single poll will be performed and emits the latest polling state. A poll will be
     * performed only if the current polling state is not in terminal state.
     *
     * Note: this method does not implicitly introduce concurrency, by default the deferred action will be executed
     * in scheduler (if any) set for the provided observable.
     *
     * @param pollingState the current polling state
     * @return the observable of which a subscription will lead single polling action.
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    public Single<CompletableOperationPollingState> pollSingleAsync(final CompletableOperationPollingState pollingState) {
        return this.resourceManager.inner()
                .getAzureClient()
                .pollSingleAsync(pollingState.innerPollingState(), pollingState.innerResourceType())
                .map(new Func1<PollingState<Void>, CompletableOperationPollingState>() {
                    @Override
                    public CompletableOperationPollingState call(PollingState<Void> voidPollingState) {
                        pollingState.setInnerPollingState(voidPollingState);
                        return pollingState;
                    }
                });
    }

    /**
     * Given a polling state representing state of an LRO operation, this method returns {@link Observable} object,
     * when subscribed to it, a series of polling will be performed and emits each polling state to downstream.
     * Polling will completes when the operation finish with success, failure or exception.
     *
     * @param pollingState the current polling state
     * @return the observable of which a subscription will lead multiple polling action.
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    public Observable<CompletableOperationPollingState> pollAsync(final CompletableOperationPollingState pollingState) {
        return this.resourceManager.inner()
                .getAzureClient()
                .pollAsync(pollingState.innerPollingState(), pollingState.innerResourceType())
                .map(new Func1<PollingState<Void>, CompletableOperationPollingState>() {
                    @Override
                    public CompletableOperationPollingState call(PollingState<Void> voidPollingState) {
                        pollingState.setInnerPollingState(voidPollingState);
                        return pollingState;
                    }
                });
    }
}
