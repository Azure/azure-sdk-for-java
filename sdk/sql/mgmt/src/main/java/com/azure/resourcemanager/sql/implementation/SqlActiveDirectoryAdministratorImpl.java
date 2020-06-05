// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.ManagedInstanceAdministratorType;
import com.azure.resourcemanager.sql.SqlActiveDirectoryAdministrator;
import com.azure.resourcemanager.sql.models.ServerAzureADAdministratorInner;

/** Response containing the SQL Active Directory administrator. */
public class SqlActiveDirectoryAdministratorImpl extends WrapperImpl<ServerAzureADAdministratorInner>
    implements SqlActiveDirectoryAdministrator {

    protected SqlActiveDirectoryAdministratorImpl(ServerAzureADAdministratorInner innerObject) {
        super(innerObject);
    }

    @Override
    public ManagedInstanceAdministratorType administratorType() {
        return this.inner().administratorType();
    }

    @Override
    public String signInName() {
        return this.inner().login();
    }

    @Override
    public String id() {
        return this.inner().sid().toString();
    }

    @Override
    public String tenantId() {
        return this.inner().tenantId().toString();
    }
}
