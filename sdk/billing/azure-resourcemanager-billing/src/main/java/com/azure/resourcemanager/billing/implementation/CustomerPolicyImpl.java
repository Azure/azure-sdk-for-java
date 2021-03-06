// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.billing.implementation;

import com.azure.resourcemanager.billing.fluent.models.CustomerPolicyInner;
import com.azure.resourcemanager.billing.models.CustomerPolicy;
import com.azure.resourcemanager.billing.models.ViewCharges;

public final class CustomerPolicyImpl implements CustomerPolicy {
    private CustomerPolicyInner innerObject;

    private final com.azure.resourcemanager.billing.BillingManager serviceManager;

    CustomerPolicyImpl(
        CustomerPolicyInner innerObject, com.azure.resourcemanager.billing.BillingManager serviceManager) {
        this.innerObject = innerObject;
        this.serviceManager = serviceManager;
    }

    public String id() {
        return this.innerModel().id();
    }

    public String name() {
        return this.innerModel().name();
    }

    public String type() {
        return this.innerModel().type();
    }

    public ViewCharges viewCharges() {
        return this.innerModel().viewCharges();
    }

    public CustomerPolicyInner innerModel() {
        return this.innerObject;
    }

    private com.azure.resourcemanager.billing.BillingManager manager() {
        return this.serviceManager;
    }
}
