// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.automation.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.automation.fluent.UsagesClient;
import com.azure.resourcemanager.automation.fluent.models.UsageInner;
import com.azure.resourcemanager.automation.models.Usage;
import com.azure.resourcemanager.automation.models.Usages;

public final class UsagesImpl implements Usages {
    private static final ClientLogger LOGGER = new ClientLogger(UsagesImpl.class);

    private final UsagesClient innerClient;

    private final com.azure.resourcemanager.automation.AutomationManager serviceManager;

    public UsagesImpl(UsagesClient innerClient, com.azure.resourcemanager.automation.AutomationManager serviceManager) {
        this.innerClient = innerClient;
        this.serviceManager = serviceManager;
    }

    public PagedIterable<Usage> listByAutomationAccount(String resourceGroupName, String automationAccountName) {
        PagedIterable<UsageInner> inner
            = this.serviceClient().listByAutomationAccount(resourceGroupName, automationAccountName);
        return ResourceManagerUtils.mapPage(inner, inner1 -> new UsageImpl(inner1, this.manager()));
    }

    public PagedIterable<Usage> listByAutomationAccount(String resourceGroupName, String automationAccountName,
        Context context) {
        PagedIterable<UsageInner> inner
            = this.serviceClient().listByAutomationAccount(resourceGroupName, automationAccountName, context);
        return ResourceManagerUtils.mapPage(inner, inner1 -> new UsageImpl(inner1, this.manager()));
    }

    private UsagesClient serviceClient() {
        return this.innerClient;
    }

    private com.azure.resourcemanager.automation.AutomationManager manager() {
        return this.serviceManager;
    }
}
