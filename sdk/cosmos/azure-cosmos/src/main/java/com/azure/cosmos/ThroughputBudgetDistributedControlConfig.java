// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.time.Duration;

public final class ThroughputBudgetDistributedControlConfig {
    private final static Duration DEFAULT_DOCUMENT_RENEWAL_INTERVAL = Duration.ofSeconds(10); // how quickly the RU will be reload balanced
    private final static Duration DEFAULT_DOCUMENT_EXPIRE_INTERVAL = Duration.ofSeconds(60); // how quickly the client will stop taking any RU
    private final static Duration DEFAULT_DOCUMENT_TTL = Duration.ofDays(1); // how quickly the document will be deleted from the container, history purpose

    private CosmosAsyncContainer controllerContainer;
    private Duration documentRenewalInterval;
    private Duration documentExpireInterval;
    private Duration documentTtl;

    public ThroughputBudgetDistributedControlConfig() {
        this.documentExpireInterval = DEFAULT_DOCUMENT_EXPIRE_INTERVAL;
        this.documentRenewalInterval = DEFAULT_DOCUMENT_RENEWAL_INTERVAL;
        this.documentTtl = DEFAULT_DOCUMENT_TTL;
    }

    public Duration getDocumentRenewalInterval() {
        return documentRenewalInterval;
    }

    public ThroughputBudgetDistributedControlConfig documentRenewalInterval(Duration documentRenewalInterval) {
        this.documentRenewalInterval = documentRenewalInterval;
        return this;
    }

    public Duration getDocumentExpireInterval() {
        return documentExpireInterval;
    }

    public ThroughputBudgetDistributedControlConfig documentExpireInterval(Duration documentExpireInterval) {
        this.documentExpireInterval = documentExpireInterval;
        return this;
    }

    public Duration getDocumentTtl() {
        return documentTtl;
    }

    public ThroughputBudgetDistributedControlConfig documentTtl(Duration documentTtl) {
        this.documentTtl = documentTtl;
        return this;
    }

    public CosmosAsyncContainer getControllerContainer() {
        return controllerContainer;
    }

    public ThroughputBudgetDistributedControlConfig controllerContainer(CosmosAsyncContainer controllerContainer) {
        this.controllerContainer = controllerContainer;
        return this;
    }
}
