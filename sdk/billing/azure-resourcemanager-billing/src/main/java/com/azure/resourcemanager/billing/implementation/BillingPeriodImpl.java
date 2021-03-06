// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.billing.implementation;

import com.azure.resourcemanager.billing.fluent.models.BillingPeriodInner;
import com.azure.resourcemanager.billing.models.BillingPeriod;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public final class BillingPeriodImpl implements BillingPeriod {
    private BillingPeriodInner innerObject;

    private final com.azure.resourcemanager.billing.BillingManager serviceManager;

    BillingPeriodImpl(BillingPeriodInner innerObject, com.azure.resourcemanager.billing.BillingManager serviceManager) {
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

    public LocalDate billingPeriodStartDate() {
        return this.innerModel().billingPeriodStartDate();
    }

    public LocalDate billingPeriodEndDate() {
        return this.innerModel().billingPeriodEndDate();
    }

    public List<String> invoiceIds() {
        List<String> inner = this.innerModel().invoiceIds();
        if (inner != null) {
            return Collections.unmodifiableList(inner);
        } else {
            return Collections.emptyList();
        }
    }

    public BillingPeriodInner innerModel() {
        return this.innerObject;
    }

    private com.azure.resourcemanager.billing.BillingManager manager() {
        return this.serviceManager;
    }
}
