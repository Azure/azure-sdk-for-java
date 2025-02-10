// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.utils.RoleAssignmentHelper;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.storage.models.Identity;
import com.azure.resourcemanager.storage.models.IdentityType;
import com.azure.resourcemanager.storage.models.UserAssignedIdentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to set Managed Service Identity (MSI) property on a storage account, install or update MSI extension
 * and create role assignments for the service principal associated with the storage account.
 */
class StorageAccountMsiHandler extends RoleAssignmentHelper {
    private final StorageAccountImpl storageAccount;
    private List<String> creatableIdentityKeys;
    private Map<String, UserAssignedIdentity> userAssignedIdentities;
    private final ClientLogger logger = new ClientLogger(StorageAccountMsiHandler.class);

    /**
     * Creates StorageAccountMsiHandler.
     *
     * @param authorizationManager the graph rbac manager
     * @param storageAccount the storage account to which MSI extension needs to be installed and for which role
     *     assignments needs to be created
     */
    StorageAccountMsiHandler(final AuthorizationManager authorizationManager, StorageAccountImpl storageAccount) {
        super(authorizationManager, storageAccount.taskGroup(), storageAccount.idProvider());
        this.storageAccount = storageAccount;
        this.creatableIdentityKeys = new ArrayList<>();
        this.userAssignedIdentities = new HashMap<>();
    }

    /**
     * Specifies that Local Managed Service Identity needs to be enabled in the storage account. If MSI extension is not
     * already installed then it will be installed with access token port as 50342.
     *
     * @return StorageAccountMsiHandler
     */
    StorageAccountMsiHandler withLocalManagedServiceIdentity() {
        this.initStorageAccountIdentity(IdentityType.SYSTEM_ASSIGNED, false);
        return this;
    }

    /**
     * Specifies that Local Managed Service Identity needs to be disabled in the storage account.
     *
     * @return StorageAccountMsiHandler
     */
    StorageAccountMsiHandler withoutLocalManagedServiceIdentity() {
        if (this.storageAccount.innerModel().identity() == null
            || this.storageAccount.innerModel().identity().type() == null
            || IdentityType.NONE.equals(this.storageAccount.innerModel().identity().type())
            || IdentityType.USER_ASSIGNED.equals(this.storageAccount.innerModel().identity().type())) {
            return this;
        }
        this.initStorageAccountIdentity(IdentityType.SYSTEM_ASSIGNED, true);
        return this;
    }

    /**
     * Specifies that given identity should be set as one of the External Managed Service Identity of the storage
     * account.
     *
     * @param creatableIdentity yet-to-be-created identity to be associated with the storage account
     * @return StorageAccountMsiHandler
     */
    StorageAccountMsiHandler withNewExternalManagedServiceIdentity(
        Creatable<com.azure.resourcemanager.msi.models.Identity> creatableIdentity) {
        this.initStorageAccountIdentity(IdentityType.USER_ASSIGNED, false);

        TaskGroup.HasTaskGroup dependency = (TaskGroup.HasTaskGroup) creatableIdentity;
        Objects.requireNonNull(dependency);

        this.storageAccount.taskGroup().addDependency(dependency);
        this.creatableIdentityKeys.add(creatableIdentity.key());

        return this;
    }

    /**
     * Specifies that given identity should be set as one of the External Managed Service Identity of the storage
     * account.
     *
     * @param identity an identity to associate
     * @return StorageAccountMsiHandler
     */
    StorageAccountMsiHandler
        withExistingExternalManagedServiceIdentity(com.azure.resourcemanager.msi.models.Identity identity) {
        this.initStorageAccountIdentity(IdentityType.USER_ASSIGNED, false);
        this.userAssignedIdentities.put(identity.id(), new UserAssignedIdentity());
        return this;
    }

    /**
     * Specifies that given identity should be set as one of the External Managed Service Identity of the storage
     * account.
     *
     * @param identityId an identity to associate
     * @return StorageAccountMsiHandler
     */
    StorageAccountMsiHandler withExistingExternalManagedServiceIdentity(String identityId) {
        this.initStorageAccountIdentity(IdentityType.USER_ASSIGNED, false);
        this.userAssignedIdentities.put(identityId, new UserAssignedIdentity());
        return this;
    }

    /**
     * Specifies that given identity should be removed from the list of External Managed Service Identity associated
     * with the storage account.
     *
     * @param identityId resource id of the identity
     * @return StorageAccountMsiHandler
     */
    StorageAccountMsiHandler withoutExternalManagedServiceIdentity(String identityId) {
        // mark as to be removed
        if (this.storageAccount.innerModel().identity() == null
            || this.storageAccount.innerModel().identity().type() == null
            || IdentityType.NONE.equals(this.storageAccount.innerModel().identity().type())
            || IdentityType.SYSTEM_ASSIGNED.equals(this.storageAccount.innerModel().identity().type())) {
            return this;
        }
        this.initStorageAccountIdentity(IdentityType.USER_ASSIGNED, true);
        this.userAssignedIdentities.put(identityId, null);
        return this;
    }

    void processCreatedExternalIdentities() {
        for (String key : this.creatableIdentityKeys) {
            com.azure.resourcemanager.msi.models.Identity identity
                = (com.azure.resourcemanager.msi.models.Identity) this.storageAccount.taskGroup().taskResult(key);
            Objects.requireNonNull(identity);
            this.userAssignedIdentities.put(identity.id(), new UserAssignedIdentity());
        }
        this.creatableIdentityKeys.clear();
    }

    void handleExternalIdentities() {
        if (storageAccount.isInCreateMode()) {
            if (!this.userAssignedIdentities.isEmpty()) {
                this.storageAccount.createParameters.identity().withUserAssignedIdentities(this.userAssignedIdentities);
            }
        } else {
            if (!this.handleRemoveAllExternalIdentitiesCase()) {
                // At this point one of the following condition is met:
                //
                // 1. User don't want touch the 'StorageAccount.Identity.userAssignedIdentities' property
                //      [this.userAssignedIdentities.empty() == true]
                // 2. User want to add some identities to 'updateParameters.Identity.userAssignedIdentities'
                //      [this.userAssignedIdentities.empty() == false and this.storageAccount.identity() != null]
                // 3. User want to remove some (not all) identities in 'storageAccount.Identity.userAssignedIdentities'
                //      [this.userAssignedIdentities.empty() == false and this.storageAccount.identity() != null]
                //      Note: The scenario where this.storageAccount.identity() is null in #3 is already handled in
                //      handleRemoveAllExternalIdentitiesCase method
                // 4. User want to add and remove (all or subset) some identities in 'storageAccount.Identity.userAssignedIdentities'
                //      [this.userAssignedIdentities.empty() == false and this.storageAccount.identity() != null]
                //
                if (!this.userAssignedIdentities.isEmpty()) {
                    // At this point its guaranteed that updateParameters.identity() is not null.
                    this.storageAccount.updateParameters.identity()
                        .withUserAssignedIdentities(this.userAssignedIdentities);
                } else {
                    // User don't want to touch 'StorageAccount.Identity.userAssignedIdentities' property
                    if (this.storageAccount.updateParameters.identity() != null) {
                        // and currently there is identity exists or user want to manipulate some other properties of
                        // identity, set identities to null so that it won't send over wire.
                        this.storageAccount.updateParameters.identity().withUserAssignedIdentities(null);
                    }
                }
            }
        }
    }

    /** Clear StorageAccountMsiHandler post-run specific internal state. */
    void clear() {
        this.userAssignedIdentities = new HashMap<>();
    }

    /**
     * Method that handles the case where user request indicates all he/she wants to do is to remove all associated identities。
     *
     * @return true if user intends to remove all the identities
     */
    private boolean handleRemoveAllExternalIdentitiesCase() {
        if (!this.userAssignedIdentities.isEmpty()) {
            int rmCount = 0;
            for (UserAssignedIdentity v : this.userAssignedIdentities.values()) {
                if (v == null) {
                    rmCount++;
                } else {
                    break;
                }
            }
            boolean containsRemoveOnly = rmCount > 0 && rmCount == this.userAssignedIdentities.size();
            // Check if user request contains only request for removal of identities.
            if (containsRemoveOnly) {
                Set<String> currentIds = new HashSet<>();
                Identity currentIdentity = this.storageAccount.updateParameters.identity();
                if (currentIdentity != null && currentIdentity.userAssignedIdentities() != null) {
                    for (String id : currentIdentity.userAssignedIdentities().keySet()) {
                        currentIds.add(id.toLowerCase(Locale.ROOT));
                    }
                }
                Set<String> removeIds = new HashSet<>();
                for (Map.Entry<String, UserAssignedIdentity> entrySet : this.userAssignedIdentities.entrySet()) {
                    if (entrySet.getValue() == null) {
                        removeIds.add(entrySet.getKey().toLowerCase(Locale.ROOT));
                    }
                }
                // If so check user want to remove all the identities
                boolean removeAllCurrentIds
                    = currentIds.size() == removeIds.size() && currentIds.containsAll(removeIds);
                if (removeAllCurrentIds) {
                    // If so adjust  the identity type [Setting type to SYSTEM_ASSIGNED orNONE will remove all the
                    // identities]
                    if (currentIdentity == null || currentIdentity.type() == null) {
                        this.storageAccount.updateParameters.withIdentity(new Identity().withType(IdentityType.NONE));
                    } else if (currentIdentity.type().equals(IdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)) {
                        this.storageAccount.updateParameters.identity().withType(IdentityType.SYSTEM_ASSIGNED);
                    } else if (currentIdentity.type().equals(IdentityType.USER_ASSIGNED)) {
                        this.storageAccount.updateParameters.identity().withType(IdentityType.NONE);
                    }
                    // and set identities property in the payload model to null so that it won't be sent
                    this.storageAccount.updateParameters.identity().withUserAssignedIdentities(null);
                    return true;
                } else {
                    // Check user is asking to remove identities though there is no identities currently associated
                    if (currentIds.isEmpty() && !removeIds.isEmpty() && currentIdentity == null) {
                        // If so we are in a invalid state but we want to send user input to service and let service
                        // handle it (ignore or error).
                        this.storageAccount.updateParameters
                            .withIdentity(new Identity().withType(IdentityType.NONE).withUserAssignedIdentities(null));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Initialize storage account's identity property.
     *
     * @param identityType the identity type to set
     */
    private void initStorageAccountIdentity(IdentityType identityType, Boolean isWithout) {
        if (!identityType.equals(IdentityType.USER_ASSIGNED) && !identityType.equals(IdentityType.SYSTEM_ASSIGNED)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid argument: " + identityType));
        }
        if (this.storageAccount.isInCreateMode()) {
            if (Objects.isNull(this.storageAccount.createParameters.identity())) {
                this.storageAccount.createParameters.withIdentity(new Identity());
            }
            if (Objects.isNull(this.storageAccount.createParameters.identity().type())
                || this.storageAccount.createParameters.identity().type().equals(IdentityType.NONE)
                || this.storageAccount.createParameters.identity().type().equals(identityType)) {
                this.storageAccount.createParameters.identity().withType(identityType);
            } else {
                this.storageAccount.createParameters.identity().withType(IdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED);
            }
        } else {
            if (Objects.isNull(this.storageAccount.updateParameters.identity())) {
                this.storageAccount.updateParameters.withIdentity(this.storageAccount.innerModel().identity());
            }
            if (isWithout) {
                if (IdentityType.SYSTEM_ASSIGNED.equals(identityType)) {
                    if (IdentityType.SYSTEM_ASSIGNED.equals(this.storageAccount.updateParameters.identity().type())) {
                        this.storageAccount.updateParameters.identity().withType(IdentityType.NONE);
                    } else {
                        this.storageAccount.updateParameters.identity().withType(IdentityType.USER_ASSIGNED);
                    }
                }
            } else {
                if (Objects.isNull(this.storageAccount.updateParameters.identity())) {
                    this.storageAccount.updateParameters.withIdentity(new Identity());
                }
                if (Objects.isNull(this.storageAccount.updateParameters.identity().type())
                    || this.storageAccount.updateParameters.identity().type().equals(IdentityType.NONE)
                    || this.storageAccount.updateParameters.identity().type().equals(identityType)) {
                    this.storageAccount.updateParameters.identity().withType(identityType);
                } else {
                    this.storageAccount.updateParameters.identity()
                        .withType(IdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED);
                }
            }
        }
    }
}
