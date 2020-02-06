/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.mgmt;

import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.keyvault.Vaults;
import com.microsoft.azure.management.keyvault.implementation.KeyVaultManager;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;

public class KeyVaultTool {

    private Access access;
    private Vaults vaults;
    
    public KeyVaultTool(Access access) {
        this.access = access;
        vaults = KeyVaultManager
                .authenticate(access.credentials(), access.subscription())
                .vaults();
    }
    
    public Vault createVaultInNewGroup(String resourceGroup, String prefix) {
        final String vaultName = keyVaultName(prefix);

        Vault result = vaults
                .define(vaultName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(resourceGroup)
                .withEmptyAccessPolicy()
                .create();
        
        result = result
                .update()
                .defineAccessPolicy()
                .forServicePrincipal(access.servicePrincipal())
                .allowKeyAllPermissions()
                .allowSecretAllPermissions()
                .allowStorageAllPermissions()
                .attach()
                .apply();

        return result;
    }
    
    private String keyVaultName(String prefix) {
        String name;
        do {
            name = SdkContext.randomResourceName(prefix, 20);
        } while (!vaults.checkNameAvailability(name).nameAvailable());
        
        return name;
    }

    public static void grantSystemAssignedMSIAccessToKeyVault(Vault vault, String systemAssignedMSI) {
        vault.update()
                .defineAccessPolicy()
                    .forObjectId(systemAssignedMSI)
                    .allowKeyAllPermissions()
                    .allowSecretAllPermissions()
                    .attach()
                .apply();
    }

 }
