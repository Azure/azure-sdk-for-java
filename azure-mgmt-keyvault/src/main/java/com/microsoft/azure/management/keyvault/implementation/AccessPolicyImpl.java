/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault.implementation;

import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.graphrbac.User;
import com.microsoft.azure.management.keyvault.AccessPolicy;
import com.microsoft.azure.management.keyvault.AccessPolicyEntry;
import com.microsoft.azure.management.keyvault.Permissions;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation for StorageAccount and its parent interfaces.
 */
class AccessPolicyImpl
        extends ChildResourceImpl<
                AccessPolicyEntry,
                VaultImpl>
        implements
            AccessPolicy,
            AccessPolicy.Definition<Vault.DefinitionStages.WithCreate> {
    private enum ALLOWED_KEY_PERMISSIONS {
        DECRYPT("Decrypt"), ENCRYPT("Encrypt"), UNWRAPKEY("UnwrapKey"), WRAPKEY("WrapKey"), VERIFY("Verify"), SIGN("Sign"), GET("Get"), LIST("List"), UPDATE("Update"), CREATE("Create"), IMPORT("Import"), DELETE("Delete"), BACKUP("Backup"), RESTORE("Restore"), ALL("All");
        private String value;
        ALLOWED_KEY_PERMISSIONS(String value) {
            this.value = value;
        }
        @Override
        public String toString() {
            return value;
        }
    }

    private enum ALLOWED_SECRET_PERMISSIONS {
        GET("Get"), LIST("List"), SET("Set"), DELETE("Delete"), ALL("All");
        private String value;
        ALLOWED_SECRET_PERMISSIONS(String value) {
            this.value = value;
        }
        @Override
        public String toString() {
            return value;
        }
    }

    AccessPolicyImpl(AccessPolicyEntry innerObject, VaultImpl parent) {
        super(null, innerObject, parent);
    }

    @Override
    public UUID tenantId() {
        return inner().tenantId();
    }

    @Override
    public UUID objectId() {
        return inner().objectId();
    }

    @Override
    public UUID applicationId() {
        return inner().applicationId();
    }

    @Override
    public Permissions permissions() {
        return inner().permissions();
    }

    @Override
    public String name() {
        return inner().objectId().toString();
    }

    private void initializeKeyPermissions() {
        if (inner().permissions() == null) {
            inner().withPermissions(new Permissions());
        }
        if (inner().permissions().keys() == null) {
            inner().permissions().withKeys(new ArrayList<String>());
        }
    }

    private void initializeSecretPermissions() {
        if (inner().permissions() == null) {
            inner().withPermissions(new Permissions());
        }
        if (inner().permissions().secrets() == null) {
            inner().permissions().withSecrets(new ArrayList<String>());
        }
    }

    @Override
    public AccessPolicyImpl allowKeyPermission(String permission) {
        initializeKeyPermissions();
        inner().permissions().keys().add(permission);
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyPermissions(List<String> permissions) {
        initializeKeyPermissions();
        inner().permissions().keys().addAll(permissions);
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretPermission(String permission) {
        initializeSecretPermissions();
        inner().permissions().secrets().add(permission);
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretPermissions(List<String> permissions) {
        initializeSecretPermissions();
        inner().permissions().secrets().addAll(permissions);
        return this;
    }

    @Override
    public VaultImpl attach() {
        parent().withAccessPolicy(this);
        return parent();
    }

    @Override
    public AccessPolicyImpl forObjectId(UUID objectId) {
        inner().withObjectId(objectId);
        return this;
    }

    @Override
    public AccessPolicyImpl forUser(User user) {
        inner().withObjectId(UUID.fromString(user.objectId()));
        return this;
    }

    @Override
    public AccessPolicyImpl forServicePrincipal(ServicePrincipal servicePrincipal) {
        inner().withObjectId(UUID.fromString(servicePrincipal.objectId()));
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyDecrypting() {
        initializeKeyPermissions();
        inner().permissions().keys().add(ALLOWED_KEY_PERMISSIONS.DECRYPT.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyEncrypting() {
        initializeKeyPermissions();
        inner().permissions().keys().add(ALLOWED_KEY_PERMISSIONS.ENCRYPT.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyUnwrapping() {
        initializeKeyPermissions();
        inner().permissions().keys().add(ALLOWED_KEY_PERMISSIONS.UNWRAPKEY.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyWrapping() {
        initializeKeyPermissions();
        inner().permissions().keys().add(ALLOWED_KEY_PERMISSIONS.WRAPKEY.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyVerifying() {
        initializeKeyPermissions();
        inner().permissions().keys().add(ALLOWED_KEY_PERMISSIONS.VERIFY.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeySigning() {
        initializeKeyPermissions();
        inner().permissions().keys().add(ALLOWED_KEY_PERMISSIONS.SIGN.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyGetting() {
        initializeKeyPermissions();
        inner().permissions().keys().add(ALLOWED_KEY_PERMISSIONS.GET.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyListing() {
        initializeKeyPermissions();
        inner().permissions().keys().add(ALLOWED_KEY_PERMISSIONS.LIST.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyUpdating() {
        initializeKeyPermissions();
        inner().permissions().keys().add(ALLOWED_KEY_PERMISSIONS.UPDATE.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyCreating() {
        initializeKeyPermissions();
        inner().permissions().keys().add(ALLOWED_KEY_PERMISSIONS.CREATE.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyImporting() {
        initializeKeyPermissions();
        inner().permissions().keys().add(ALLOWED_KEY_PERMISSIONS.IMPORT.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyDeleting() {
        initializeKeyPermissions();
        inner().permissions().keys().add(ALLOWED_KEY_PERMISSIONS.DELETE.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyBackingUp() {
        initializeKeyPermissions();
        inner().permissions().keys().add(ALLOWED_KEY_PERMISSIONS.BACKUP.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyRestoring() {
        initializeKeyPermissions();
        inner().permissions().keys().add(ALLOWED_KEY_PERMISSIONS.RESTORE.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyAllPermissions() {
        initializeKeyPermissions();
        for(ALLOWED_KEY_PERMISSIONS permission : ALLOWED_KEY_PERMISSIONS.values()) {
            inner().permissions().keys().add(permission.toString());
        }
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyDecrypting() {
        initializeKeyPermissions();
        inner().permissions().keys().remove(ALLOWED_KEY_PERMISSIONS.DECRYPT.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyEncrypting() {
        initializeKeyPermissions();
        inner().permissions().keys().remove(ALLOWED_KEY_PERMISSIONS.ENCRYPT.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyUnwrapping() {
        initializeKeyPermissions();
        inner().permissions().keys().remove(ALLOWED_KEY_PERMISSIONS.UNWRAPKEY.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyWrapping() {
        initializeKeyPermissions();
        inner().permissions().keys().remove(ALLOWED_KEY_PERMISSIONS.WRAPKEY.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyVerifying() {
        initializeKeyPermissions();
        inner().permissions().keys().remove(ALLOWED_KEY_PERMISSIONS.VERIFY.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeySigning() {
        initializeKeyPermissions();
        inner().permissions().keys().remove(ALLOWED_KEY_PERMISSIONS.SIGN.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyGetting() {
        initializeKeyPermissions();
        inner().permissions().keys().remove(ALLOWED_KEY_PERMISSIONS.GET.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyListing() {
        initializeKeyPermissions();
        inner().permissions().keys().remove(ALLOWED_KEY_PERMISSIONS.LIST.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyUpdating() {
        initializeKeyPermissions();
        inner().permissions().keys().remove(ALLOWED_KEY_PERMISSIONS.UPDATE.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyCreating() {
        initializeKeyPermissions();
        inner().permissions().keys().remove(ALLOWED_KEY_PERMISSIONS.CREATE.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyImporting() {
        initializeKeyPermissions();
        inner().permissions().keys().remove(ALLOWED_KEY_PERMISSIONS.IMPORT.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyDeleting() {
        initializeKeyPermissions();
        inner().permissions().keys().remove(ALLOWED_KEY_PERMISSIONS.DELETE.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyBackingUp() {
        initializeKeyPermissions();
        inner().permissions().keys().remove(ALLOWED_KEY_PERMISSIONS.BACKUP.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyRestoring() {
        initializeKeyPermissions();
        inner().permissions().keys().remove(ALLOWED_KEY_PERMISSIONS.RESTORE.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyAllPermissions() {
        initializeKeyPermissions();
        inner().permissions().keys().clear();
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyPermission(String permission) {
        initializeKeyPermissions();
        inner().permissions().keys().remove(permission);
        return this;
    }

    @Override
    public AccessPolicyImpl disallowKeyPermissions(List<String> permissions) {
        initializeKeyPermissions();
        inner().permissions().keys().removeAll(permissions);
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretGetting() {
        initializeSecretPermissions();
        inner().permissions().keys().add(ALLOWED_SECRET_PERMISSIONS.GET.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretListing() {
        initializeSecretPermissions();
        inner().permissions().keys().add(ALLOWED_SECRET_PERMISSIONS.LIST.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretSetting() {
        initializeSecretPermissions();
        inner().permissions().keys().add(ALLOWED_SECRET_PERMISSIONS.SET.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretDeleting() {
        initializeSecretPermissions();
        inner().permissions().keys().add(ALLOWED_SECRET_PERMISSIONS.DELETE.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretAllPermissions() {
        return null;
    }

    @Override
    public AccessPolicyImpl disallowSecretGetting() {
        initializeSecretPermissions();
        inner().permissions().keys().remove(ALLOWED_SECRET_PERMISSIONS.GET.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowSecretListing() {
        initializeSecretPermissions();
        inner().permissions().keys().remove(ALLOWED_SECRET_PERMISSIONS.LIST.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowSecretSetting() {
        initializeSecretPermissions();
        inner().permissions().keys().remove(ALLOWED_SECRET_PERMISSIONS.SET.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowSecretDeleting() {
        initializeSecretPermissions();
        inner().permissions().keys().remove(ALLOWED_SECRET_PERMISSIONS.DELETE.toString());
        return this;
    }

    @Override
    public AccessPolicyImpl disallowSecretAllPermissions() {
        initializeSecretPermissions();
        inner().permissions().keys().clear();
        return this;
    }

    @Override
    public AccessPolicyImpl disallowSecretPermission(String permission) {
        initializeSecretPermissions();
        inner().permissions().keys().remove(permission);
        return this;
    }

    @Override
    public AccessPolicyImpl disallowSecretPermissions(List<String> permissions) {
        initializeSecretPermissions();
        inner().permissions().keys().removeAll(permissions);
        return this;
    }

    @Override
    public String key() {
        return objectId().toString();
    }
}
