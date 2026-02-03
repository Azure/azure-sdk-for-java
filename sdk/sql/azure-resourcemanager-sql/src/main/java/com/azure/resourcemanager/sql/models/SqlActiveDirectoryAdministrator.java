// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;

/** Response containing the Azure SQL Active Directory administrator. */
@Fluent
public interface SqlActiveDirectoryAdministrator {
    /**
     * Gets the type of administrator.
     *
     * @return the type of administrator.
     */
    AdministratorType administratorType();

    /**
     * Gets the server administrator login value.
     *
     * @return the server administrator login value.
     */
    String signInName();

    /**
     * Gets the server administrator ID.
     *
     * @return the server administrator ID.
     */
    String id();

    /**
     * Gets the server Active Directory Administrator tenant ID.
     *
     * @return the server Active Directory Administrator tenant ID.
     */
    String tenantId();
}
