// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.utils.RoleAssignmentHelper;

/**
 * Utility class to set Managed Service Identity (MSI) property on a disk encryption set,
 * create role assignments for the service principal associated with the disk encryption set.
 */
class DiskEncryptionSetMsiHandler extends RoleAssignmentHelper {
    /**
     * Creates DiskEncryptionSetMsiHandler.
     *
     * @param authorizationManager the graph rbac manager
     * @param diskEncryptionSet    disk encryption set
     */
    DiskEncryptionSetMsiHandler(AuthorizationManager authorizationManager, DiskEncryptionSetImpl diskEncryptionSet) {
        super(authorizationManager, diskEncryptionSet.taskGroup(), diskEncryptionSet.idProvider());
    }
}
