// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.properties;

import com.azure.spring.service.servicebus.properties.ServiceBusNamespaceProperties;

/**
 * A service bus namespace related properties.
 */
public class NamespaceProperties extends CommonProperties implements ServiceBusNamespaceProperties {

    private Boolean crossEntityTransactions;

    @Override
    public Boolean getCrossEntityTransactions() {
        return crossEntityTransactions;
    }

    public void setCrossEntityTransactions(Boolean crossEntityTransactions) {
        this.crossEntityTransactions = crossEntityTransactions;
    }
}
