// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.utils.RoleAssignmentHelper;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.models.DiskEncryptionSetInner;
import com.azure.resourcemanager.compute.models.DiskEncryptionSet;
import com.azure.resourcemanager.compute.models.DiskEncryptionSetIdentityType;
import com.azure.resourcemanager.compute.models.DiskEncryptionSetType;
import com.azure.resourcemanager.compute.models.DiskEncryptionSetUpdate;
import com.azure.resourcemanager.compute.models.EncryptionSetIdentity;
import com.azure.resourcemanager.compute.models.KeyForDiskEncryptionSet;
import com.azure.resourcemanager.compute.models.SourceVault;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

public class DiskEncryptionSetImpl
    extends GroupableResourceImpl<DiskEncryptionSet, DiskEncryptionSetInner, DiskEncryptionSetImpl, ComputeManager>
    implements DiskEncryptionSet,
        DiskEncryptionSet.Definition,
        DiskEncryptionSet.Update {
    private DiskEncryptionSetUpdate patchToUpdate = new DiskEncryptionSetUpdate();
    private boolean updated;
    private final DiskEncryptionSetMsiHandler msiHandler;

    protected DiskEncryptionSetImpl(String name, DiskEncryptionSetInner innerObject, ComputeManager manager) {
        super(name, innerObject, manager);
        this.msiHandler = new DiskEncryptionSetMsiHandler(manager.authorizationManager(), this);
    }

    @Override
    public String keyVaultId() {
        if (innerModel().activeKey() == null || innerModel().activeKey().sourceVault() == null) {
            return null;
        }
        return innerModel().activeKey().sourceVault().id();
    }

    @Override
    public String encryptionKeyId() {
        if (innerModel().activeKey() == null) {
            return null;
        }
        return innerModel().activeKey().keyUrl();
    }

    @Override
    public String systemAssignedManagedServiceIdentityPrincipalId() {
        if (innerModel().identity() == null || innerModel().identity().type() == DiskEncryptionSetIdentityType.NONE) {
            return null;
        }
        return innerModel().identity().principalId();
    }

    @Override
    public Boolean isAutomaticKeyRotationEnabled() {
        return innerModel().rotationToLatestKeyVersionEnabled();
    }

    @Override
    public DiskEncryptionSetType encryptionType() {
        return innerModel().encryptionType();
    }

    @Override
    public DiskEncryptionSetImpl withAutomaticKeyRotation() {
        innerModel().withRotationToLatestKeyVersionEnabled(true);
        if (isInUpdateMode()) {
            patchToUpdate.withRotationToLatestKeyVersionEnabled(true);
            updated = true;
        }
        return this;
    }

    @Override
    public DiskEncryptionSetImpl withoutAutomaticKeyRotation() {
        innerModel().withRotationToLatestKeyVersionEnabled(false);
        if (isInUpdateMode()) {
            patchToUpdate.withRotationToLatestKeyVersionEnabled(false);
            updated = true;
        }
        return this;
    }

    @Override
    public DiskEncryptionSetImpl withSystemAssignedManagedServiceIdentity() {
        innerModel().withIdentity(new EncryptionSetIdentity().withType(DiskEncryptionSetIdentityType.SYSTEM_ASSIGNED));
        if (isInUpdateMode()) {
            patchToUpdate.withIdentity(innerModel().identity());
            updated = true;
        }
        return this;
    }

    @Override
    public DiskEncryptionSetImpl withoutSystemAssignedManagedServiceIdentity() {
        innerModel().withIdentity(new EncryptionSetIdentity().withType(DiskEncryptionSetIdentityType.NONE));
        if (isInUpdateMode()) {
            patchToUpdate.withIdentity(innerModel().identity());
            updated = true;
        }
        return this;
    }

    @Override
    public DiskEncryptionSetImpl withExistingKeyVault(String keyVaultId) {
        ensureActiveKey();
        innerModel().activeKey().withSourceVault(new SourceVault().withId(keyVaultId));
        if (isInUpdateMode()) {
            ensureActiveKey(patchToUpdate);
            patchToUpdate.activeKey().withSourceVault(innerModel().activeKey().sourceVault());
            updated = true;
        }
        return this;
    }

    @Override
    public DiskEncryptionSetImpl withRBACBasedAccessToCurrentKeyVault(BuiltInRole builtInRole) {
        if (keyVaultId() != null) {
            msiHandler.withAccessTo(keyVaultId(), builtInRole);
        }
        return this;
    }

    @Override
    public DiskEncryptionSetImpl withRBACBasedAccessToCurrentKeyVault() {
        return withRBACBasedAccessToCurrentKeyVault(BuiltInRole.KEY_VAULT_CRYPTO_SERVICE_ENCRYPTION_USER);
    }

    @Override
    public Mono<DiskEncryptionSet> createResourceAsync() {
        return manager().serviceClient().getDiskEncryptionSets().createOrUpdateAsync(
            resourceGroupName(), name(), innerModel()
        ).map(inner -> {
            setInner(inner);
            return this;
        });
    }

    @Override
    public DiskEncryptionSetImpl update() {
        this.patchToUpdate = new DiskEncryptionSetUpdate();
        return this;
    }

    @Override
    public Mono<DiskEncryptionSet> updateResourceAsync() {
        if (!updated) {
            return Mono.just(this);
        }
        return manager().serviceClient().getDiskEncryptionSets().updateAsync(
            resourceGroupName(), name(), patchToUpdate
        ).map(inner -> {
            setInner(inner);
            this.updated = false;
            return this;
        });
    }

    @Override
    protected Mono<DiskEncryptionSetInner> getInnerAsync() {
        return manager().serviceClient().getDiskEncryptionSets().getByResourceGroupAsync(
            resourceGroupName(), name()
        ).map(inner -> {
            this.updated = false;
            return inner;
        });
    }

    @Override
    public DiskEncryptionSetImpl withExistingKey(String keyId) {
        ensureActiveKey();
        innerModel().activeKey().withKeyUrl(keyId);
        if (isInUpdateMode()) {
            ensureActiveKey(patchToUpdate);
            patchToUpdate.activeKey().withKeyUrl(keyId);
            updated = true;
        }
        return this;
    }

    @Override
    public DiskEncryptionSetImpl withEncryptionType(DiskEncryptionSetType type) {
        innerModel().withEncryptionType(type);
        return this;
    }

    RoleAssignmentHelper.IdProvider idProvider() {
        return new RoleAssignmentHelper.IdProvider() {
            @Override
            public String principalId() {
                return systemAssignedManagedServiceIdentityPrincipalId();
            }

            @Override
            public String resourceId() {
                return id();
            }
        };
    }

    private void ensureActiveKey() {
        if (innerModel().activeKey() == null) {
            innerModel().withActiveKey(new KeyForDiskEncryptionSet());
        }
    }

    private void ensureActiveKey(DiskEncryptionSetUpdate patchToUpdate) {
        if (patchToUpdate.activeKey() == null) {
            patchToUpdate.withActiveKey(new KeyForDiskEncryptionSet());
        }
    }

    private boolean isInUpdateMode() {
        return !isInCreateMode();
    }
}
