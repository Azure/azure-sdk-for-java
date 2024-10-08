// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.automation.implementation;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.automation.fluent.LinkedWorkspacesClient;
import com.azure.resourcemanager.automation.fluent.models.LinkedWorkspaceInner;
import com.azure.resourcemanager.automation.models.LinkedWorkspace;
import com.azure.resourcemanager.automation.models.LinkedWorkspaces;

public final class LinkedWorkspacesImpl implements LinkedWorkspaces {
    private static final ClientLogger LOGGER = new ClientLogger(LinkedWorkspacesImpl.class);

    private final LinkedWorkspacesClient innerClient;

    private final com.azure.resourcemanager.automation.AutomationManager serviceManager;

    public LinkedWorkspacesImpl(LinkedWorkspacesClient innerClient,
        com.azure.resourcemanager.automation.AutomationManager serviceManager) {
        this.innerClient = innerClient;
        this.serviceManager = serviceManager;
    }

    public Response<LinkedWorkspace> getWithResponse(String resourceGroupName, String automationAccountName,
        Context context) {
        Response<LinkedWorkspaceInner> inner
            = this.serviceClient().getWithResponse(resourceGroupName, automationAccountName, context);
        if (inner != null) {
            return new SimpleResponse<>(inner.getRequest(), inner.getStatusCode(), inner.getHeaders(),
                new LinkedWorkspaceImpl(inner.getValue(), this.manager()));
        } else {
            return null;
        }
    }

    public LinkedWorkspace get(String resourceGroupName, String automationAccountName) {
        LinkedWorkspaceInner inner = this.serviceClient().get(resourceGroupName, automationAccountName);
        if (inner != null) {
            return new LinkedWorkspaceImpl(inner, this.manager());
        } else {
            return null;
        }
    }

    private LinkedWorkspacesClient serviceClient() {
        return this.innerClient;
    }

    private com.azure.resourcemanager.automation.AutomationManager manager() {
        return this.serviceManager;
    }
}
