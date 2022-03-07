// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core.properties;

import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusNamespaceProperties;

/**
 * A service bus namespace related properties.
 */
public class NamespaceProperties extends CommonProperties implements ServiceBusNamespaceProperties {

    private Boolean crossEntityTransactions;

    @Override
    public Boolean getCrossEntityTransactions() {
        return crossEntityTransactions;
    }

    /**
     * Set the cross entity transaction.
     * @param crossEntityTransactions the cross entity transaction.
     */
    public void setCrossEntityTransactions(Boolean crossEntityTransactions) {
        this.crossEntityTransactions = crossEntityTransactions;
    }
}
