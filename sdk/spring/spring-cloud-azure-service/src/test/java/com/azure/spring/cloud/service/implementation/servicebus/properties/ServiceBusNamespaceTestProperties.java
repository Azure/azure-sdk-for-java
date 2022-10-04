// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.properties;

public class ServiceBusNamespaceTestProperties extends ServiceBusClientCommonTestProperties
    implements ServiceBusNamespaceProperties {

    private Boolean crossEntityTransactions;

    @Override
    public Boolean getCrossEntityTransactions() {
        return crossEntityTransactions;
    }

    public void setCrossEntityTransactions(Boolean crossEntityTransactions) {
        this.crossEntityTransactions = crossEntityTransactions;
    }
}
