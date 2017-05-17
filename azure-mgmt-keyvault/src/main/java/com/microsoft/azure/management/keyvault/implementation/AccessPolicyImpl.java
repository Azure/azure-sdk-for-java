/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryGroup;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryUser;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.keyvault.AccessPolicy;
import com.microsoft.azure.management.keyvault.AccessPolicyEntry;
import com.microsoft.azure.management.keyvault.KeyPermissions;
import com.microsoft.azure.management.keyvault.Permissions;
import com.microsoft.azure.management.keyvault.SecretPermissions;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Implementation for AccessPolicy and its parent interfaces.
 */
@LangDefinition
class AccessPolicyImpl
        extends ChildResourceImpl<
                AccessPolicyEntry,
                VaultImpl,
                Vault>
        implements
            AccessPolicy,
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

    @Override
    public AccessPolicyImpl allowKeyPermissions(KeyPermissions... permissions) {
        initializeKeyPermissions();
        inner().permissions().keys().addAll(Arrays.asList(permissions));
        return this;
    }

    @Override
    public AccessPolicyImpl allowKeyPermissions(List<KeyPermissions> permissions) {
        initializeKeyPermissions();
        inner().permissions().keys().addAll(permissions);
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretPermissions(SecretPermissions... permissions) {
        initializeSecretPermissions();
        inner().permissions().secrets().addAll(Arrays.asList(permissions));
        return this;
    }

    @Override
    public AccessPolicyImpl allowSecretPermissions(List<SecretPermissions> permissions) {
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
        return allowKeyPermissions(KeyPermissions.ALL);
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
        return allowSecretPermissions(SecretPermissions.ALL);
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
}
