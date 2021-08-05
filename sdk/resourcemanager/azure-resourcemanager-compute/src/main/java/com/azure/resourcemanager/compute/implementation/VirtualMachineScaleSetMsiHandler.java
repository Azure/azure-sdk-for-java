// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.compute.models.ResourceIdentityType;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetIdentity;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetIdentityUserAssignedIdentities;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetUpdate;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineScaleSetInner;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.utils.RoleAssignmentHelper;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.resources.fluentcore.dag.TaskGroup;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class to set Managed Service Identity (MSI) property on a virtual machine scale set, install or update MSI
 * extension and create role assignments for the service principal (LMSI) associated with the virtual machine scale set.
 */
class VirtualMachineScaleSetMsiHandler extends RoleAssignmentHelper {
    private final VirtualMachineScaleSetImpl scaleSet;

    private List<String> creatableIdentityKeys;
    private Map<String, VirtualMachineScaleSetIdentityUserAssignedIdentities> userAssignedIdentities;
    private final ClientLogger logger = new ClientLogger(VirtualMachineScaleSetMsiHandler.class);

    /**
     * Creates VirtualMachineScaleSetMsiHandler.
     *
     * @param authorizationManager the graph rbac manager
     */
    VirtualMachineScaleSetMsiHandler(AuthorizationManager authorizationManager, VirtualMachineScaleSetImpl scaleSet) {
        super(authorizationManager, scaleSet.taskGroup(), scaleSet.idProvider());
        this.scaleSet = scaleSet;
        this.creatableIdentityKeys = new ArrayList<>();
        this.userAssignedIdentities = new HashMap<>();
    }

    /**
     * Specifies that Local Managed Service Identity needs to be enabled in the virtual machine scale set. If MSI
     * extension is not already installed then it will be installed with access token port as 50342.
     *
     * @return VirtualMachineScaleSetMsiHandler
     */
    VirtualMachineScaleSetMsiHandler withLocalManagedServiceIdentity() {
        this.initVMSSIdentity(ResourceIdentityType.SYSTEM_ASSIGNED);
        return this;
    }

    /**
     * Specifies that Local Managed Service Identity needs to be disabled in the virtual machine scale set.
     *
     * @return VirtualMachineScaleSetMsiHandler
     */
    VirtualMachineScaleSetMsiHandler withoutLocalManagedServiceIdentity() {
        if (this.scaleSet.innerModel().identity() == null
            || this.scaleSet.innerModel().identity().type() == null
            || this.scaleSet.innerModel().identity().type().equals(ResourceIdentityType.NONE)
            || this.scaleSet.innerModel().identity().type().equals(ResourceIdentityType.USER_ASSIGNED)) {
            return this;
        } else if (this.scaleSet.innerModel().identity().type().equals(ResourceIdentityType.SYSTEM_ASSIGNED)) {
            this.scaleSet.innerModel().identity().withType(ResourceIdentityType.NONE);
        } else if (this
            .scaleSet
            .innerModel()
            .identity()
            .type()
            .equals(ResourceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)) {
            this.scaleSet.innerModel().identity().withType(ResourceIdentityType.USER_ASSIGNED);
        }
        return this;
    }

    /**
     * Specifies that given identity should be set as one of the External Managed Service Identity of the virtual
     * machine scale set.
     *
     * @param creatableIdentity yet-to-be-created identity to be associated with the virtual machine scale set
     * @return VirtualMachineScaleSetMsiHandler
     */
    VirtualMachineScaleSetMsiHandler withNewExternalManagedServiceIdentity(Creatable<Identity> creatableIdentity) {
        this.initVMSSIdentity(ResourceIdentityType.USER_ASSIGNED);

        TaskGroup.HasTaskGroup dependency = (TaskGroup.HasTaskGroup) creatableIdentity;
        Objects.requireNonNull(dependency);

        this.scaleSet.taskGroup().addDependency(dependency);
        this.creatableIdentityKeys.add(creatableIdentity.key());
        return this;
    }

    /**
     * Specifies that given identity should be set as one of the External Managed Service Identity of the virtual
     * machine scale set.
     *
     * @param identity an identity to associate
     * @return VirtualMachineScaleSetMsiHandler
     */
    VirtualMachineScaleSetMsiHandler withExistingExternalManagedServiceIdentity(Identity identity) {
        this.initVMSSIdentity(ResourceIdentityType.USER_ASSIGNED);
        this.userAssignedIdentities.put(identity.id(), new VirtualMachineScaleSetIdentityUserAssignedIdentities());
        return this;
    }

    /**
     * Specifies that given identity should be removed from the list of External Managed Service Identity associated
     * with the virtual machine machine scale set.
     *
     * @param identityId resource id of the identity
     * @return VirtualMachineScaleSetMsiHandler
     */
    VirtualMachineScaleSetMsiHandler withoutExternalManagedServiceIdentity(String identityId) {
        this.userAssignedIdentities.put(identityId, null);
        return this;
    }

    /** Update the VMSS payload model using the created External Managed Service Identities. */
    void processCreatedExternalIdentities() {
        for (String key : this.creatableIdentityKeys) {
            Identity identity = (Identity) this.scaleSet.taskGroup().taskResult(key);
            Objects.requireNonNull(identity);
            this.userAssignedIdentities.put(identity.id(), new VirtualMachineScaleSetIdentityUserAssignedIdentities());
        }
        this.creatableIdentityKeys.clear();
    }

    void handleExternalIdentities() {
        if (!this.userAssignedIdentities.isEmpty()) {
            this.scaleSet.innerModel().identity().withUserAssignedIdentities(this.userAssignedIdentities);
        }
    }

    void handleExternalIdentities(VirtualMachineScaleSetUpdate vmssUpdate) {
        if (this.handleRemoveAllExternalIdentitiesCase(vmssUpdate)) {
            return;
        } else {
            // At this point one of the following condition is met:
            //
            // 1. User don't want touch the 'VMSS.Identity.userAssignedIdentities' property
            //      [this.userAssignedIdentities.empty() == true]
            // 2. User want to add some identities to 'VMSS.Identity.userAssignedIdentities'
            //      [this.userAssignedIdentities.empty() == false and this.scaleSet.inner().identity() != null]
            // 3. User want to remove some (not all) identities in 'VMSS.Identity.userAssignedIdentities'
            //      [this.userAssignedIdentities.empty() == false and this.scaleSet.inner().identity() != null]
            //      Note: The scenario where this.scaleSet.inner().identity() is null in #3 is already handled in
            //      handleRemoveAllExternalIdentitiesCase method
            // 4. User want to add and remove (all or subset) some identities in 'VMSS.Identity.userAssignedIdentities'
            //      [this.userAssignedIdentities.empty() == false and this.scaleSet.inner().identity() != null]
            //
            VirtualMachineScaleSetIdentity currentIdentity = this.scaleSet.innerModel().identity();
            vmssUpdate.withIdentity(currentIdentity);
            if (!this.userAssignedIdentities.isEmpty()) {
                // At this point its guaranteed that 'currentIdentity' is not null so vmUpdate.identity() is.
                vmssUpdate.identity().withUserAssignedIdentities(this.userAssignedIdentities);
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

    /** Clear VirtualMachineScaleSetMsiHandler post-run specific internal state. */
    void clear() {
        this.userAssignedIdentities = new HashMap<>();
    }

    /**
     * Method that handle the case where user request indicates all it want to do is remove all identities associated
     * with the virtual machine.
     *
     * @param vmssUpdate the vm update payload model
     * @return true if user indented to remove all the identities.
     */
    private boolean handleRemoveAllExternalIdentitiesCase(VirtualMachineScaleSetUpdate vmssUpdate) {
        if (!this.userAssignedIdentities.isEmpty()) {
            int rmCount = 0;
            for (VirtualMachineScaleSetIdentityUserAssignedIdentities v : this.userAssignedIdentities.values()) {
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
                VirtualMachineScaleSetIdentity currentIdentity = this.scaleSet.innerModel().identity();
                if (currentIdentity != null && currentIdentity.userAssignedIdentities() != null) {
                    for (String id : currentIdentity.userAssignedIdentities().keySet()) {
                        currentIds.add(id.toLowerCase(Locale.ROOT));
                    }
                }
                Set<String> removeIds = new HashSet<>();
                for (Map.Entry<String, VirtualMachineScaleSetIdentityUserAssignedIdentities> entrySet
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
                        vmssUpdate
                            .withIdentity(new VirtualMachineScaleSetIdentity().withType(ResourceIdentityType.NONE));
                    } else if (currentIdentity.type().equals(ResourceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)) {
                        vmssUpdate.withIdentity(currentIdentity);
                        vmssUpdate.identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED);
                    } else if (currentIdentity.type().equals(ResourceIdentityType.USER_ASSIGNED)) {
                        vmssUpdate.withIdentity(currentIdentity);
                        vmssUpdate.identity().withType(ResourceIdentityType.NONE);
                    }
                    // and set identities property in the payload model to null so that it won't be sent
                    vmssUpdate.identity().withUserAssignedIdentities(null);
                    return true;
                } else {
                    // Check user is asking to remove identities though there is no identities currently associated
                    if (currentIds.size() == 0 && removeIds.size() != 0 && currentIdentity == null) {
                        // If so we are in a invalid state but we want to send user input to service and let service
                        // handle it (ignore or error).
                        vmssUpdate
                            .withIdentity(new VirtualMachineScaleSetIdentity().withType(ResourceIdentityType.NONE));
                        vmssUpdate.identity().withUserAssignedIdentities(null);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Initialize VMSS's identity property.
     *
     * @param identityType the identity type to set
     */
    private void initVMSSIdentity(ResourceIdentityType identityType) {
        if (!identityType.equals(ResourceIdentityType.USER_ASSIGNED)
            && !identityType.equals(ResourceIdentityType.SYSTEM_ASSIGNED)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid argument: " + identityType));
        }

        final VirtualMachineScaleSetInner scaleSetInner = this.scaleSet.innerModel();
        if (scaleSetInner.identity() == null) {
            scaleSetInner.withIdentity(new VirtualMachineScaleSetIdentity());
        }
        if (scaleSetInner.identity().type() == null
            || scaleSetInner.identity().type().equals(ResourceIdentityType.NONE)
            || scaleSetInner.identity().type().equals(identityType)) {
            scaleSetInner.identity().withType(identityType);
        } else {
            scaleSetInner.identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED);
        }
    }
}
