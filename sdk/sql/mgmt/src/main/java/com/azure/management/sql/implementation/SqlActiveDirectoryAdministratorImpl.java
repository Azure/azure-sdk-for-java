/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.sql.SqlActiveDirectoryAdministrator;
import com.azure.management.sql.models.ServerAzureADAdministratorInner;

/**
 * Response containing the SQL Active Directory administrator.
 */
public class SqlActiveDirectoryAdministratorImpl extends WrapperImpl<ServerAzureADAdministratorInner>
    implements SqlActiveDirectoryAdministrator {

    protected SqlActiveDirectoryAdministratorImpl(ServerAzureADAdministratorInner innerObject) {
        super(innerObject);
    }

    @Override
    public String administratorType() {
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
