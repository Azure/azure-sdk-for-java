// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.implementation;

import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroup;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUser;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.keyvault.models.AccessPolicy;
import com.azure.resourcemanager.keyvault.models.AccessPolicyEntry;
import com.azure.resourcemanager.keyvault.models.CertificatePermissions;
import com.azure.resourcemanager.keyvault.models.KeyPermissions;
import com.azure.resourcemanager.keyvault.models.Permissions;
import com.azure.resourcemanager.keyvault.models.SecretPermissions;
import com.azure.resourcemanager.keyvault.models.StoragePermissions;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Implementation for AccessPolicy and its parent interfaces. */
class AccessPolicyImpl extends ChildResourceImpl<AccessPolicyEntry, VaultImpl, Vault>
    implements AccessPolicy,
        AccessPolicy.Definition<Vault.DefinitionStages.WithCreate>,
        AccessPolicy.UpdateDefinition<Vault.Update>,
        AccessPolicy.Update {
    private String userPrincipalName;
    private String servicePrincipalName;

    AccessPolicyImpl(AccessPolicyEntry innerObject, VaultImpl parent) {
        super(innerObject, parent);
        innerModel().withTenantId(UUID.fromString(parent.tenantId()));
    }

    String userPrincipalName() {
        return userPrincipalName;
    }

    String servicePrincipalName() {
        return servicePrincipalName;
    }

    @Override
    public String tenantId() {
        if (innerModel().tenantId() == null) {
            return null;
        }
        return innerModel().tenantId().toString();
    }

    @Override
    public String objectId() {
        if (innerModel().objectId() == null) {
            return null;
        }
        return innerModel().objectId();
    }

    @Override
    public String applicationId() {
        if (innerModel().applicationId() == null) {
            return null;
        }
        return innerModel().applicationId().toString();
    }

    @Override
    public Permissions permissions() {
        return innerModel().permissions();
    }

    @Override
    public String name() {
        return innerModel().objectId();
    }

    private void initializeKeyPermissions() {
        if (innerModel().permissions() == null) {
            innerModel().withPermissions(new Permissions());
        }
        if (innerModel().permissions().keys() == null) {
            innerModel().permissions().withKeys(new ArrayList<KeyPermissions>());
        }
    }

    private void initializeSecretPermissions() {
        if (innerModel().permissions() == null) {
            innerModel().withPermissions(new Permissions());
        }
        if (innerModel().permissions().secrets() == null) {
            innerModel().permissions().withSecrets(new ArrayList<SecretPermissions>());
        }
    }

    private void initializeCertificatePermissions() {
        if (innerModel().permissions() == null) {
            innerModel().withPermissions(new Permissions());
        }
        if (innerModel().permissions().certificates() == null) {
            innerModel().permissions().withCertificates(new ArrayList<CertificatePermissions>());
        }
    }

    private void initializeStoragePermissions() {
        if (innerModel().permissions() == null) {
            innerModel().withPermissions(new Permissions());
        }
        if (innerModel().permissions().storage() == null) {
            innerModel().permissions().withStorage(new ArrayList<StoragePermissions>());
        }
    }

    @Override
    public AccessPolicyImpl allowKeyPermissions(KeyPermissions... permissions) {
        initializeKeyPermissions();
        for (KeyPermissions permission : permissions) {
            if (!innerModel().permissions().keys().contains(permission)) {
                innerModel().permissions().keys().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyPermissions(List<KeyPermissions> permissions) {
        initializeKeyPermissions();
        for (KeyPermissions permission : permissions) {
            if (!innerModel().permissions().keys().contains(permission)) {
                innerModel().permissions().keys().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretPermissions(SecretPermissions... permissions) {
        initializeSecretPermissions();
        for (SecretPermissions permission : permissions) {
            if (!innerModel().permissions().secrets().contains(permission)) {
                innerModel().permissions().secrets().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretPermissions(List<SecretPermissions> permissions) {
        initializeSecretPermissions();
        for (SecretPermissions permission : permissions) {
            if (!innerModel().permissions().secrets().contains(permission)) {
                innerModel().permissions().secrets().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowCertificateAllPermissions() {
        for (CertificatePermissions permission : CertificatePermissions.values()) {
            allowCertificatePermissions(permission);
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowCertificatePermissions(CertificatePermissions... permissions) {
        initializeCertificatePermissions();
        for (CertificatePermissions permission : permissions) {
            if (!innerModel().permissions().certificates().contains(permission)) {
                innerModel().permissions().certificates().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowCertificatePermissions(List<CertificatePermissions> permissions) {
        initializeCertificatePermissions();
        for (CertificatePermissions permission : permissions) {
            if (!innerModel().permissions().certificates().contains(permission)) {
                innerModel().permissions().certificates().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowStorageAllPermissions() {
        for (StoragePermissions permission : StoragePermissions.values()) {
            allowStoragePermissions(permission);
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowStoragePermissions(StoragePermissions... permissions) {
        initializeStoragePermissions();
        for (StoragePermissions permission : permissions) {
            if (!innerModel().permissions().storage().contains(permission)) {
                innerModel().permissions().storage().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowStoragePermissions(List<StoragePermissions> permissions) {
        initializeStoragePermissions();
        for (StoragePermissions permission : permissions) {
            if (!innerModel().permissions().storage().contains(permission)) {
                innerModel().permissions().storage().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl disallowCertificateAllPermissions() {
        initializeCertificatePermissions();
        innerModel().permissions().secrets().clear();
        return this;
    }

    @Override
    public AccessPolicyImpl disallowCertificatePermissions(CertificatePermissions... permissions) {
        initializeCertificatePermissions();
        innerModel().permissions().certificates().removeAll(Arrays.asList(permissions));
        return this;
    }

    @Override
    public AccessPolicyImpl disallowCertificatePermissions(List<CertificatePermissions> permissions) {
        initializeCertificatePermissions();
        innerModel().permissions().certificates().removeAll(permissions);
        return this;
    }

    @Override
    public VaultImpl attach() {
        parent().withAccessPolicy(this);
        return parent();
    }

    @Override
    public AccessPolicyImpl forObjectId(String objectId) {
        innerModel().withObjectId(objectId);
        return this;
    }

    @Override
    public AccessPolicyImpl forUser(ActiveDirectoryUser user) {
        innerModel().withObjectId(user.id());
        return this;
    }

    @Override
    public AccessPolicyImpl forUser(String userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
        return this;
    }

    @Override
    public AccessPolicyImpl forApplicationId(String applicationId) {
        innerModel().withApplicationId(UUID.fromString(applicationId));
        return this;
    }

    @Override
    public AccessPolicyImpl forTenantId(String tenantId) {
        innerModel().withTenantId(UUID.fromString(tenantId));
        return this;
    }

    @Override
    public AccessPolicyImpl forGroup(ActiveDirectoryGroup activeDirectoryGroup) {
        innerModel().withObjectId(activeDirectoryGroup.id());
        return this;
    }

    @Override
    public AccessPolicyImpl forServicePrincipal(ServicePrincipal servicePrincipal) {
        innerModel().withObjectId(servicePrincipal.id());
        return this;
    }

    @Override
    public AccessPolicyImpl forServicePrincipal(String servicePrincipalName) {
        this.servicePrincipalName = servicePrincipalName;
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyAllPermissions() {
        for (KeyPermissions permission : KeyPermissions.values()) {
            allowKeyPermissions(permission);
        }
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyAllPermissions() {
        initializeKeyPermissions();
        innerModel().permissions().keys().clear();
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyPermissions(KeyPermissions... permissions) {
        initializeSecretPermissions();
        innerModel().permissions().keys().removeAll(Arrays.asList(permissions));
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyPermissions(List<KeyPermissions> permissions) {
        initializeSecretPermissions();
        innerModel().permissions().keys().removeAll(permissions);
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretAllPermissions() {
        for (SecretPermissions permission : SecretPermissions.values()) {
            allowSecretPermissions(permission);
        }
        return this;
    }

    @Override
    public AccessPolicyImpl disallowSecretAllPermissions() {
        initializeSecretPermissions();
        innerModel().permissions().secrets().clear();
        return this;
    }

    @Override
    public AccessPolicyImpl disallowSecretPermissions(SecretPermissions... permissions) {
        initializeSecretPermissions();
        innerModel().permissions().secrets().removeAll(Arrays.asList(permissions));
        return this;
    }

    @Override
    public AccessPolicyImpl disallowSecretPermissions(List<SecretPermissions> permissions) {
        initializeSecretPermissions();
        innerModel().permissions().secrets().removeAll(permissions);
        return this;
    }

    @Override
    public AccessPolicyImpl disallowStorageAllPermissions() {
        initializeStoragePermissions();
        innerModel().permissions().storage().clear();
        return this;
    }

    @Override
    public AccessPolicyImpl disallowStoragePermissions(StoragePermissions... permissions) {
        initializeStoragePermissions();
        innerModel().permissions().storage().removeAll(Arrays.asList(permissions));
        return this;
    }

    @Override
    public AccessPolicyImpl disallowStoragePermissions(List<StoragePermissions> permissions) {
        initializeStoragePermissions();
        innerModel().permissions().storage().removeAll(permissions);
        return this;
    }
}
