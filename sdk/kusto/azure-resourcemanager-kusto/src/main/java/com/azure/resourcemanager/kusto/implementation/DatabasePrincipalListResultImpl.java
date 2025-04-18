// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.kusto.implementation;

import com.azure.resourcemanager.kusto.fluent.models.DatabasePrincipalInner;
import com.azure.resourcemanager.kusto.fluent.models.DatabasePrincipalListResultInner;
import com.azure.resourcemanager.kusto.models.DatabasePrincipal;
import com.azure.resourcemanager.kusto.models.DatabasePrincipalListResult;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class DatabasePrincipalListResultImpl implements DatabasePrincipalListResult {
    private DatabasePrincipalListResultInner innerObject;

    private final com.azure.resourcemanager.kusto.KustoManager serviceManager;

    DatabasePrincipalListResultImpl(DatabasePrincipalListResultInner innerObject,
        com.azure.resourcemanager.kusto.KustoManager serviceManager) {
        this.innerObject = innerObject;
        this.serviceManager = serviceManager;
    }

    public List<DatabasePrincipal> value() {
        List<DatabasePrincipalInner> inner = this.innerModel().value();
        if (inner != null) {
            return Collections.unmodifiableList(inner.stream()
                .map(inner1 -> new DatabasePrincipalImpl(inner1, this.manager()))
                .collect(Collectors.toList()));
        } else {
            return Collections.emptyList();
        }
    }

    public DatabasePrincipalListResultInner innerModel() {
        return this.innerObject;
    }

    private com.azure.resourcemanager.kusto.KustoManager manager() {
        return this.serviceManager;
    }
}
