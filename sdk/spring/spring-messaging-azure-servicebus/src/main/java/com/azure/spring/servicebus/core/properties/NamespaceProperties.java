// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.properties;

import com.azure.spring.service.servicebus.properties.ServiceBusNamespaceDescriptor;

/**
 * A service bus namespace related properties.
 */
public class NamespaceProperties extends CommonProperties implements ServiceBusNamespaceDescriptor {

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
