/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.keyvault.implementation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.management.graphrbac.ActiveDirectoryGroup;
import com.azure.management.graphrbac.ActiveDirectoryUser;
import com.azure.management.graphrbac.ServicePrincipal;
import com.azure.management.keyvault.AccessPolicy;
import com.azure.management.keyvault.AccessPolicyEntry;
import com.azure.management.keyvault.CertificatePermissions;
import com.azure.management.keyvault.KeyPermissions;
import com.azure.management.keyvault.Permissions;
import com.azure.management.keyvault.SecretPermissions;
import com.azure.management.keyvault.StoragePermissions;
import com.azure.management.keyvault.Vault;

/**
 * Implementation for AccessPolicy and its parent interfaces.
 */
class AccessPolicyImpl extends ChildResourceImpl<AccessPolicyEntry, VaultImpl, Vault>
        implements AccessPolicy, AccessPolicy.Definition<Vault.DefinitionStages.WithCreate>,
        AccessPolicy.UpdateDefinition<Vault.Update>, AccessPolicy.Update {
    private String userPrincipalName;
    private String servicePrincipalName;

    AccessPolicyImpl(AccessPolicyEntry innerObject, VaultImpl parent) {
        super(innerObject, parent);
        inner().setTenantId(UUID.fromString(parent.tenantId()));
    }

    String userPrincipalName() {
        return userPrincipalName;
    }

    String servicePrincipalName() {
        return servicePrincipalName;
    }

    @Override
    public String tenantId() {
        if (inner().getTenantId() == null) {
            return null;
        }
        return inner().getTenantId().toString();
    }

    @Override
    public String objectId() {
        if (inner().getObjectId() == null) {
            return null;
        }
        return inner().getObjectId();
    }

    @Override
    public String applicationId() {
        if (inner().getApplicationId() == null) {
            return null;
        }
        return inner().getApplicationId().toString();
    }

    @Override
    public Permissions permissions() {
        return inner().getPermissions();
    }

    @Override
    public String name() {
        return inner().getObjectId();
    }

    private void initializeKeyPermissions() {
        if (inner().getPermissions() == null) {
            inner().setPermissions(new Permissions());
        }
        if (inner().getPermissions().getKeys() == null) {
            inner().getPermissions().setKeys(new ArrayList<KeyPermissions>());
        }
    }

    private void initializeSecretPermissions() {
        if (inner().getPermissions() == null) {
            inner().setPermissions(new Permissions());
        }
        if (inner().getPermissions().getSecrets() == null) {
            inner().getPermissions().setSecrets(new ArrayList<SecretPermissions>());
        }
    }

    private void initializeCertificatePermissions() {
        if (inner().getPermissions() == null) {
            inner().setPermissions(new Permissions());
        }
        if (inner().getPermissions().getCertificates() == null) {
            inner().getPermissions().setCertificates(new ArrayList<CertificatePermissions>());
        }
    }

    private void initializeStoragePermissions() {
        if (inner().getPermissions() == null) {
            inner().setPermissions(new Permissions());
        }
        if (inner().getPermissions().getStorage() == null) {
            inner().getPermissions().setStorage(new ArrayList<StoragePermissions>());
        }
    }

    @Override
    public AccessPolicyImpl allowKeyPermissions(KeyPermissions... permissions) {
        initializeKeyPermissions();
        for (KeyPermissions permission : permissions) {
            if (!inner().getPermissions().getKeys().contains(permission)) {
                inner().getPermissions().getKeys().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyPermissions(List<KeyPermissions> permissions) {
        initializeKeyPermissions();
        for (KeyPermissions permission : permissions) {
            if (!inner().getPermissions().getKeys().contains(permission)) {
                inner().getPermissions().getKeys().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretPermissions(SecretPermissions... permissions) {
        initializeSecretPermissions();
        for (SecretPermissions permission : permissions) {
            if (!inner().getPermissions().getSecrets().contains(permission)) {
                inner().getPermissions().getSecrets().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretPermissions(List<SecretPermissions> permissions) {
        initializeSecretPermissions();
        for (SecretPermissions permission : permissions) {
            if (!inner().getPermissions().getSecrets().contains(permission)) {
                inner().getPermissions().getSecrets().add(permission);
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
            if (!inner().getPermissions().getCertificates().contains(permission)) {
                inner().getPermissions().getCertificates().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowCertificatePermissions(List<CertificatePermissions> permissions) {
        initializeCertificatePermissions();
        for (CertificatePermissions permission : permissions) {
            if (!inner().getPermissions().getCertificates().contains(permission)) {
                inner().getPermissions().getCertificates().add(permission);
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
            if (!inner().getPermissions().getStorage().contains(permission)) {
                inner().getPermissions().getStorage().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl allowStoragePermissions(List<StoragePermissions> permissions) {
        initializeStoragePermissions();
        for (StoragePermissions permission : permissions) {
            if (!inner().getPermissions().getStorage().contains(permission)) {
                inner().getPermissions().getStorage().add(permission);
            }
        }
        return this;
    }

    @Override
    public AccessPolicyImpl disallowCertificateAllPermissions() {
        initializeCertificatePermissions();
        inner().getPermissions().getSecrets().clear();
        return this;
    }

    @Override
    public AccessPolicyImpl disallowCertificatePermissions(CertificatePermissions... permissions) {
        initializeCertificatePermissions();
        inner().getPermissions().getCertificates().removeAll(Arrays.asList(permissions));
        return this;
    }

    @Override
    public AccessPolicyImpl disallowCertificatePermissions(List<CertificatePermissions> permissions) {
        initializeCertificatePermissions();
        inner().getPermissions().getCertificates().removeAll(permissions);
        return this;
    }

    @Override
    public VaultImpl attach() {
        parent().withAccessPolicy(this);
        return parent();
    }

    @Override
    public AccessPolicyImpl forObjectId(String objectId) {
        inner().setObjectId(objectId);
        return this;
    }

    @Override
    public AccessPolicyImpl forUser(ActiveDirectoryUser user) {
        inner().setObjectId(user.id());
        return this;
    }

    @Override
    public AccessPolicyImpl forUser(String userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
        return this;
    }

    @Override
    public AccessPolicyImpl forApplicationId(String applicationId) {
        inner().setApplicationId(UUID.fromString(applicationId));
        return this;
    }

    @Override
    public AccessPolicyImpl forTenantId(String tenantId) {
        inner().setTenantId(UUID.fromString(tenantId));
        return this;
    }

    @Override
    public AccessPolicyImpl forGroup(ActiveDirectoryGroup activeDirectoryGroup) {
        inner().setObjectId(activeDirectoryGroup.id());
        return this;
    }

    @Override
    public AccessPolicyImpl forServicePrincipal(ServicePrincipal servicePrincipal) {
        inner().setObjectId(servicePrincipal.id());
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
        inner().getPermissions().getKeys().clear();
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyPermissions(KeyPermissions... permissions) {
        initializeSecretPermissions();
        inner().getPermissions().getKeys().removeAll(Arrays.asList(permissions));
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyPermissions(List<KeyPermissions> permissions) {
        initializeSecretPermissions();
        inner().getPermissions().getKeys().removeAll(permissions);
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
        inner().getPermissions().getSecrets().clear();
        return this;
    }

    @Override
    public AccessPolicyImpl disallowSecretPermissions(SecretPermissions... permissions) {
        initializeSecretPermissions();
        inner().getPermissions().getSecrets().removeAll(Arrays.asList(permissions));
        return this;
    }

    @Override
    public AccessPolicyImpl disallowSecretPermissions(List<SecretPermissions> permissions) {
        initializeSecretPermissions();
        inner().getPermissions().getSecrets().removeAll(permissions);
        return this;
    }

    @Override
    public AccessPolicyImpl disallowStorageAllPermissions() {
        initializeStoragePermissions();
        inner().getPermissions().getStorage().clear();
        return this;
    }

    @Override
    public AccessPolicyImpl disallowStoragePermissions(StoragePermissions... permissions) {
        initializeStoragePermissions();
        inner().getPermissions().getStorage().removeAll(Arrays.asList(permissions));
        return this;
    }

    @Override
    public AccessPolicyImpl disallowStoragePermissions(List<StoragePermissions> permissions) {
        initializeStoragePermissions();
        inner().getPermissions().getStorage().removeAll(permissions);
        return this;
    }

}
