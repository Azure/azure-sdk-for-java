// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;

/** Response containing the Azure SQL Active Directory administrator. */
@Fluent
public interface SqlActiveDirectoryAdministrator {
    /** @return the type of administrator. */
    ManagedInstanceAdministratorType administratorType();

    /** @return the server administrator login value. */
    String signInName();

    /** @return the server administrator ID. */
    String id();

    /** @return the server Active Directory Administrator tenant ID. */
    String tenantId();
}
