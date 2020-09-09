// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplications;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroups;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUsers;
import com.azure.resourcemanager.authorization.models.RoleAssignments;
import com.azure.resourcemanager.authorization.models.RoleDefinitions;
import com.azure.resourcemanager.authorization.models.ServicePrincipals;

/** Exposes methods related to managing access permissions in Azure. */
public interface AccessManagement {
    /**
     * Entry point to AD user management APIs.
     *
     * @return ActiveDirectoryUsers interface providing access to tenant management
     */
    ActiveDirectoryUsers activeDirectoryUsers();

    /**
     * Entry point to AD group management APIs.
     *
     * @return ActiveDirectoryGroups interface providing access to tenant management
     */
    ActiveDirectoryGroups activeDirectoryGroups();

    /**
     * Entry point to AD service principal management APIs.
     *
     * @return ServicePrincipals interface providing access to tenant management
     */
    ServicePrincipals servicePrincipals();

    /**
     * Entry point to AD application management APIs.
     *
     * @return Applications interface providing access to tenant management
     */
    ActiveDirectoryApplications activeDirectoryApplications();

    /**
     * Entry point to role definition management APIs.
     *
     * @return RoleDefinitions interface providing access to tenant management
     */
    RoleDefinitions roleDefinitions();

    /**
     * Entry point to role assignment management APIs.
     *
     * @return RoleAssignments interface providing access to tenant management
     */
    RoleAssignments roleAssignments();
}
