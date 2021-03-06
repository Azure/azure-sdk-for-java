// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.billing.implementation;

import com.azure.resourcemanager.billing.fluent.models.EnrollmentAccountSummaryInner;
import com.azure.resourcemanager.billing.models.EnrollmentAccountSummary;

public final class EnrollmentAccountSummaryImpl implements EnrollmentAccountSummary {
    private EnrollmentAccountSummaryInner innerObject;

    private final com.azure.resourcemanager.billing.BillingManager serviceManager;

    EnrollmentAccountSummaryImpl(
        EnrollmentAccountSummaryInner innerObject, com.azure.resourcemanager.billing.BillingManager serviceManager) {
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

    public String principalName() {
        return this.innerModel().principalName();
    }

    public EnrollmentAccountSummaryInner innerModel() {
        return this.innerObject;
    }

    private com.azure.resourcemanager.billing.BillingManager manager() {
        return this.serviceManager;
    }
}
