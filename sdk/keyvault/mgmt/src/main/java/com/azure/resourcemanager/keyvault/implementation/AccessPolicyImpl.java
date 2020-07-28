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
        inner().withTenantId(UUID.fromString(parent.tenantId()));
    }

    String userPrincipalName() {
        return userPrincipalName;
    }

    String servicePrincipalName() {
        return servicePrincipalName;
    }

    @Override
    public String tenantId() {
        if (inner().tenantId() == null) {
            return null;
        }
        return inner().tenantId().toString();
    }

    @Override
    public String objectId() {
        if (inner().objectId() == null) {
            return null;
        }
        return inner().objectId();
    }

    @Override
    public String applicationId() {
        if (inner().applicationId() == null) {
            return null;
        }
        return inner().applicationId().toString();
    }

    @Override
    public Permissions permissions() {
        return inner().permissions();
    }

    @Override
    public String name() {
        return inner().objectId();
    }

    private void initializeKeyPermissions() {
        if (inner().permissions() == null) {
            inner().withPermissions(new Permissions());
        }
        if (inner().permissions().keys() == null) {
            inner().permissions().withKeys(new ArrayList<KeyPermissions>());
        }
    }

    private void initializeSecretPermissions() {
        if (inner().permissions() == null) {
            inner().withPermissions(new Permissions());
        }
        if (inner().permissions().secrets() == null) {
            inner().permissions().withSecrets(new ArrayList<SecretPermissions>());
        }
    }

    private void initializeCertificatePermissions() {
        if (inner().permissions() == null) {
            inner().withPermissions(new Permissions());
        }
        if (inner().permissions().certificates() == null) {
            inner().permissions().withCertificates(new ArrayList<CertificatePermissions>());
        }
    }

    private void initializeStoragePermissions() {
        if (inner().permissions() == null) {
            inner().withPermissions(new Permissions());
        }
        if (inner().permissions().storage() == null) {
            inner().permissions().withStorage(new ArrayList<StoragePermissions>());
        }
    }

    @Override
    public AccessPolicyImpl allowKeyPermissions(KeyPermissions... permissions) {
        initializeKeyPermissions();
        for (KeyPermissions permission : permissions) {
            if (!inner().permissions().keys().contains(permission)) {
                inner().permissions().keys().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyPermissions(List<KeyPermissions> permissions) {
        initializeKeyPermissions();
        for (KeyPermissions permission : permissions) {
            if (!inner().permissions().keys().contains(permission)) {
                inner().permissions().keys().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretPermissions(SecretPermissions... permissions) {
        initializeSecretPermissions();
        for (SecretPermissions permission : permissions) {
            if (!inner().permissions().secrets().contains(permission)) {
                inner().permissions().secrets().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretPermissions(List<SecretPermissions> permissions) {
        initializeSecretPermissions();
        for (SecretPermissions permission : permissions) {
            if (!inner().permissions().secrets().contains(permission)) {
                inner().permissions().secrets().add(permission);
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
            if (!inner().permissions().certificates().contains(permission)) {
                inner().permissions().certificates().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowCertificatePermissions(List<CertificatePermissions> permissions) {
        initializeCertificatePermissions();
        for (CertificatePermissions permission : permissions) {
            if (!inner().permissions().certificates().contains(permission)) {
                inner().permissions().certificates().add(permission);
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
            if (!inner().permissions().storage().contains(permission)) {
                inner().permissions().storage().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowStoragePermissions(List<StoragePermissions> permissions) {
        initializeStoragePermissions();
        for (StoragePermissions permission : permissions) {
            if (!inner().permissions().storage().contains(permission)) {
                inner().permissions().storage().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl disallowCertificateAllPermissions() {
        initializeCertificatePermissions();
        inner().permissions().secrets().clear();
        return this;
    }

    @Override
    public AccessPolicyImpl disallowCertificatePermissions(CertificatePermissions... permissions) {
        initializeCertificatePermissions();
        inner().permissions().certificates().removeAll(Arrays.asList(permissions));
        return this;
    }

    @Override
    public AccessPolicyImpl disallowCertificatePermissions(List<CertificatePermissions> permissions) {
        initializeCertificatePermissions();
        inner().permissions().certificates().removeAll(permissions);
        return this;
    }

    @Override
    public VaultImpl attach() {
        parent().withAccessPolicy(this);
        return parent();
    }

    @Override
    public AccessPolicyImpl forObjectId(String objectId) {
        inner().withObjectId(objectId);
        return this;
    }

    @Override
    public AccessPolicyImpl forUser(ActiveDirectoryUser user) {
        inner().withObjectId(user.id());
        return this;
    }

    @Override
    public AccessPolicyImpl forUser(String userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
        return this;
    }

    @Override
    public AccessPolicyImpl forApplicationId(String applicationId) {
        inner().withApplicationId(UUID.fromString(applicationId));
        return this;
    }

    @Override
    public AccessPolicyImpl forTenantId(String tenantId) {
        inner().withTenantId(UUID.fromString(tenantId));
        return this;
    }

    @Override
    public AccessPolicyImpl forGroup(ActiveDirectoryGroup activeDirectoryGroup) {
        inner().withObjectId(activeDirectoryGroup.id());
        return this;
    }

    @Override
    public AccessPolicyImpl forServicePrincipal(ServicePrincipal servicePrincipal) {
        inner().withObjectId(servicePrincipal.id());
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
        inner().permissions().keys().clear();
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyPermissions(KeyPermissions... permissions) {
        initializeSecretPermissions();
        inner().permissions().keys().removeAll(Arrays.asList(permissions));
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyPermissions(List<KeyPermissions> permissions) {
        initializeSecretPermissions();
        inner().permissions().keys().removeAll(permissions);
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
        inner().permissions().secrets().clear();
        return this;
    }

    @Override
    public AccessPolicyImpl disallowSecretPermissions(SecretPermissions... permissions) {
        initializeSecretPermissions();
        inner().permissions().secrets().removeAll(Arrays.asList(permissions));
        return this;
    }

    @Override
    public AccessPolicyImpl disallowSecretPermissions(List<SecretPermissions> permissions) {
        initializeSecretPermissions();
        inner().permissions().secrets().removeAll(permissions);
        return this;
    }

    @Override
    public AccessPolicyImpl disallowStorageAllPermissions() {
        initializeStoragePermissions();
        inner().permissions().storage().clear();
        return this;
    }

    @Override
    public AccessPolicyImpl disallowStoragePermissions(StoragePermissions... permissions) {
        initializeStoragePermissions();
        inner().permissions().storage().removeAll(Arrays.asList(permissions));
        return this;
    }

    @Override
    public AccessPolicyImpl disallowStoragePermissions(List<StoragePermissions> permissions) {
        initializeStoragePermissions();
        inner().permissions().storage().removeAll(permissions);
        return this;
    }
}
