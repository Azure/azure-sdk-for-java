// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.utils.RoleAssignmentHelper;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.storage.fluent.models.StorageAccountInner;
import com.azure.resourcemanager.storage.models.Identity;
import com.azure.resourcemanager.storage.models.IdentityType;
import com.azure.resourcemanager.storage.models.StorageAccountCreateParameters;
import com.azure.resourcemanager.storage.models.StorageAccountUpdateParameters;
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
 * Utility class to set Managed Service Identity (MSI) property on a virtual machine, install or update MSI extension
 * and create role assignments for the service principal associated with the virtual machine.
 */
class StorageAccountMsiHandler extends RoleAssignmentHelper {
    private final StorageAccountImpl storageAccount;

    private List<String> creatableIdentityKeys;
    private Map<String, UserAssignedIdentity> userAssignedIdentities;
    private final ClientLogger logger = new ClientLogger(StorageAccountMsiHandler.class);

    /**
     * Creates VirtualMachineMsiHandler.
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
     * @return VirtualMachineMsiHandler
     */
    StorageAccountMsiHandler withLocalManagedServiceIdentity() {
        this.initStorageAccountIdentity(IdentityType.SYSTEM_ASSIGNED);
        return this;
    }

    /**
     * Specifies that Local Managed Service Identity needs to be disabled in the storage account.
     *
     * @return VirtualMachineMsiHandler
     */
    StorageAccountMsiHandler withoutLocalManagedServiceIdentity() {
        if (this.storageAccount.innerModel().identity() == null
            || this.storageAccount.innerModel().identity().type() == null
            || this.storageAccount.innerModel().identity().type().equals(IdentityType.NONE)
            || this.storageAccount.innerModel().identity().type().equals(IdentityType.USER_ASSIGNED)) {
            return this;
        } else if (this.storageAccount.innerModel().identity().type().equals(IdentityType.SYSTEM_ASSIGNED)) {
            this.storageAccount.innerModel().identity().withType(IdentityType.NONE);
        } else if (this
            .storageAccount
            .innerModel()
            .identity()
            .type()
            .equals(IdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)) {
            this.storageAccount.innerModel().identity().withType(IdentityType.USER_ASSIGNED);
        }
        return this;
    }

    /**
     * Specifies that given identity should be set as one of the External Managed Service Identity of the virtual
     * machine.
     *
     * @param creatableIdentity yet-to-be-created identity to be associated with the storage account
     * @return VirtualMachineMsiHandler
     */
    StorageAccountMsiHandler withNewExternalManagedServiceIdentity(Creatable<com.azure.resourcemanager.msi.models.Identity> creatableIdentity) {
        this.initStorageAccountIdentity(IdentityType.USER_ASSIGNED);

        TaskGroup.HasTaskGroup dependency = (TaskGroup.HasTaskGroup) creatableIdentity;
        Objects.requireNonNull(dependency);

        this.storageAccount.taskGroup().addDependency(dependency);
        this.creatableIdentityKeys.add(creatableIdentity.key());

        return this;
    }

    /**
     * Specifies that given identity should be set as one of the External Managed Service Identity of the virtual
     * machine.
     *
     * @param identity an identity to associate
     * @return VirtualMachineMsiHandler
     */
    StorageAccountMsiHandler withExistingExternalManagedServiceIdentity(com.azure.resourcemanager.msi.models.Identity identity) {
        this.initStorageAccountIdentity(IdentityType.USER_ASSIGNED);
        this.userAssignedIdentities.put(identity.id(), new UserAssignedIdentity());
        return this;
    }

    /**
     * Specifies that given identity should be removed from the list of External Managed Service Identity associated
     * with the storage account machine.
     *
     * @param identityId resource id of the identity
     * @return VirtualMachineMsiHandler
     */
    StorageAccountMsiHandler withoutExternalManagedServiceIdentity(String identityId) {
        // mark as to be removed
        this.userAssignedIdentities.put(identityId, null);
        return this;
    }

    void processCreatedExternalIdentities() {
        for (String key : this.creatableIdentityKeys) {
            com.azure.resourcemanager.msi.models.Identity identity = (com.azure.resourcemanager.msi.models.Identity) this.storageAccount.taskGroup().taskResult(key);
            Objects.requireNonNull(identity);
            this.userAssignedIdentities.put(identity.id(), new UserAssignedIdentity());
        }
        this.creatableIdentityKeys.clear();
    }

    void handleExternalIdentities(StorageAccountCreateParameters storageCreate) {
        if (!this.userAssignedIdentities.isEmpty()) {
            storageCreate.identity().withUserAssignedIdentities(this.userAssignedIdentities);
        }
    }

    void handleExternalIdentities(StorageAccountUpdateParameters storageUpdate) {
        if (this.handleRemoveAllExternalIdentitiesCase(storageUpdate)) {
            return;
        } else {
            // At this point one of the following condition is met:
            //
            // 1. User don't want touch the 'VM.Identity.userAssignedIdentities' property
            //      [this.userAssignedIdentities.empty() == true]
            // 2. User want to add some identities to 'VM.Identity.userAssignedIdentities'
            //      [this.userAssignedIdentities.empty() == false and this.virtualMachine.inner().identity() != null]
            // 3. User want to remove some (not all) identities in 'VM.Identity.userAssignedIdentities'
            //      [this.userAssignedIdentities.empty() == false and this.virtualMachine.inner().identity() != null]
            //      Note: The scenario where this.virtualMachine.inner().identity() is null in #3 is already handled in
            //      handleRemoveAllExternalIdentitiesCase method
            // 4. User want to add and remove (all or subset) some identities in 'VM.Identity.userAssignedIdentities'
            //      [this.userAssignedIdentities.empty() == false and this.virtualMachine.inner().identity() != null]
            //
            Identity currentIdentity = this.storageAccount.innerModel().identity();
            storageUpdate.withIdentity(currentIdentity);
            if (!this.userAssignedIdentities.isEmpty()) {
                // At this point its guaranteed that 'currentIdentity' is not null so vmUpdate.identity() is.
                storageUpdate.identity().withUserAssignedIdentities(this.userAssignedIdentities);
            } else {
                // User don't want to touch 'VM.Identity.userAssignedIdentities' property
                if (currentIdentity != null) {
                    // and currently there is identity exists or user want to manipulate some other properties of
                    // identity, set identities to null so that it won't send over wire.
                    currentIdentity.withUserAssignedIdentities(null);
                }
            }
        }
    }

    /** Clear VirtualMachineMsiHandler post-run specific internal state. */
    void clear() {
        this.userAssignedIdentities = new HashMap<>();
    }

    /**
     * Method that handle the case where user request indicates all it want to do is remove all identities associated
     * with the storage account.
     *
     * @param storageUpdate the vm update payload model
     * @return true if user indented to remove all the identities.
     */
    private boolean handleRemoveAllExternalIdentitiesCase(StorageAccountUpdateParameters storageUpdate) {
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
                Identity currentIdentity = this.storageAccount.innerModel().identity();
                if (currentIdentity != null && currentIdentity.userAssignedIdentities() != null) {
                    for (String id : currentIdentity.userAssignedIdentities().keySet()) {
                        currentIds.add(id.toLowerCase(Locale.ROOT));
                    }
                }
                Set<String> removeIds = new HashSet<>();
                for (Map.Entry<String, UserAssignedIdentity> entrySet
                    : this.userAssignedIdentities.entrySet()) {
                    if (entrySet.getValue() == null) {
                        removeIds.add(entrySet.getKey().toLowerCase(Locale.ROOT));
                    }
                }
                // If so check user want to remove all the identities
                boolean removeAllCurrentIds =
                    currentIds.size() == removeIds.size() && currentIds.containsAll(removeIds);
                if (removeAllCurrentIds) {
                    // If so adjust  the identity type [Setting type to SYSTEM_ASSIGNED orNONE will remove all the
                    // identities]
                    if (currentIdentity == null || currentIdentity.type() == null) {
                        storageUpdate.withIdentity(new Identity().withType(IdentityType.NONE));
                    } else if (currentIdentity.type().equals(IdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)) {
                        storageUpdate.withIdentity(currentIdentity);
                        storageUpdate.identity().withType(IdentityType.SYSTEM_ASSIGNED);
                    } else if (currentIdentity.type().equals(IdentityType.USER_ASSIGNED)) {
                        storageUpdate.withIdentity(currentIdentity);
                        storageUpdate.identity().withType(IdentityType.NONE);
                    }
                    // and set identities property in the payload model to null so that it won't be sent
                    storageUpdate.identity().withUserAssignedIdentities(null);
                    return true;
                } else {
                    // Check user is asking to remove identities though there is no identities currently associated
                    if (currentIds.isEmpty() && !removeIds.isEmpty() && currentIdentity == null) {
                        // If so we are in a invalid state but we want to send user input to service and let service
                        // handle it (ignore or error).
                        storageUpdate.withIdentity(new Identity().withType(IdentityType.NONE));
                        storageUpdate.identity().withUserAssignedIdentities(null);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Initialize VM's identity property.
     *
     * @param identityType the identity type to set
     */
    private void initStorageAccountIdentity(IdentityType identityType) {
        if (!identityType.equals(IdentityType.USER_ASSIGNED)
            && !identityType.equals(IdentityType.SYSTEM_ASSIGNED)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid argument: " + identityType));
        }

        StorageAccountInner storageAccountInner = this.storageAccount.innerModel();
        if (storageAccountInner.identity() == null) {
            storageAccountInner.withIdentity(new Identity());
        }
        if (storageAccountInner.identity().type() == null
            || storageAccountInner.identity().type().equals(IdentityType.NONE)
            || storageAccountInner.identity().type().equals(identityType)) {
            storageAccountInner.identity().withType(identityType);
        } else {
            storageAccountInner.identity().withType(IdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED);
        }
    }
}
