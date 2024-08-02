// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.core.util.Context;
import com.azure.resourcemanager.keyvault.fluent.models.ManagedHsmInner;
import com.azure.resourcemanager.keyvault.models.ManagedHsm;
import com.azure.resourcemanager.keyvault.models.ManagedHsmProperties;
import com.azure.resourcemanager.keyvault.models.ManagedHsmSku;
import com.azure.resourcemanager.keyvault.models.ManagedHsmSkuFamily;
import com.azure.resourcemanager.keyvault.models.ManagedHsmSkuName;
import com.azure.resourcemanager.keyvault.models.MhsmNetworkRuleSet;
import com.azure.resourcemanager.keyvault.models.PublicNetworkAccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Managed HSM tests.
 * Live tests can be skipped and use only playback as a validation.
 */
public class ManagedHsmTests extends KeyVaultManagementTest {
    @Override
    protected void cleanUpResources() {
        if (rgName != null) {
            keyVaultManager.resourceManager().resourceGroups().beginDeleteByName(rgName);
        }
    }

    /**
     * Note: Managed HSM instance is costly and it'll still cost you even if you delete the instance or the associated
     *       resource group, unless the instance is <string>purged</string>.
     *       <p>So, please be careful when running this test and always double check that the instance has been
     *       {@link com.azure.resourcemanager.keyvault.fluent.ManagedHsmsClient#purgeDeleted(String, String)} after the test. </p>
     *       <p>You can use {@link com.azure.resourcemanager.keyvault.fluent.ManagedHsmsClient#listDeleted()} to list all deleted instances
     *       that's not purged after deletion.</p>
     * @see <a href="https://learn.microsoft.com/en-us/azure/key-vault/managed-hsm/soft-delete-overview">soft-delete-overview</a>
     */
    @Disabled("Disabled for test-proxy migration. Please enable it again, next time api-version update.")
    @Test
    public void canCrudManagedHsms() {
        String mhsmName = generateRandomResourceName("mhsm", 10);
        ManagedHsm managedHsm = createManagedHsm(mhsmName);

        try {
            // listByResourceGroups
            PagedIterable<ManagedHsm> hsms = keyVaultManager.managedHsms()
                .listByResourceGroup(rgName);
            Assertions.assertTrue(hsms.stream().anyMatch(mhsm -> mhsm.name().equals(mhsmName)));

            // getByResourceGroup
            ManagedHsm hsm = keyVaultManager.managedHsms()
                .getByResourceGroup(rgName, managedHsm.name());

            // ManagedHsm properties
            // The Azure Active Directory tenant ID that should be used for authenticating requests to the managed HSM pool.
            String tenantId = hsm.tenantId();
            Assertions.assertNotNull(tenantId);

            ManagedHsmSku sku = hsm.sku();
            Assertions.assertNotNull(sku);

            // Array of initial administrators object ids for this managed hsm pool.
            List<String> initialAdminObjectIds = hsm.initialAdminObjectIds();
            Assertions.assertEquals(1, initialAdminObjectIds.size());

            // The URI of the managed hsm pool for performing operations on keys.
            String hsmUri = hsm.hsmUri();
            Assertions.assertNotNull(hsmUri);

            // Property to specify whether the 'soft delete' functionality is enabled for this managed HSM pool.
            boolean softDelete = hsm.isSoftDeleteEnabled();
            Assertions.assertTrue(softDelete);

            // softDelete data retention days. It accepts >=7 and <=90.
            Integer softDeleteRetentionDays = hsm.softDeleteRetentionInDays();
            Assertions.assertEquals(7, softDeleteRetentionDays);

            // Property specifying whether protection against purge is enabled for this managed HSM pool.
            boolean purgeProtectionEnabled = hsm.isPurgeProtectionEnabled();
            Assertions.assertFalse(purgeProtectionEnabled);

            // Rules governing the accessibility of the key vault from specific network locations.
            MhsmNetworkRuleSet ruleSet = hsm.networkRuleSet();
            Assertions.assertNotNull(ruleSet);

            // Whether data plane traffic coming from public networks is allowed while private endpoint is enabled
            PublicNetworkAccess publicNetworkAccess = hsm.publicNetworkAccess();
            Assertions.assertEquals(PublicNetworkAccess.ENABLED, publicNetworkAccess);

            // The scheduled purge date in UTC.
            OffsetDateTime scheduledPurgeDate = hsm.scheduledPurgeDate();
            Assertions.assertNull(scheduledPurgeDate);
        } finally {
            keyVaultManager.managedHsms().deleteById(managedHsm.id());
            keyVaultManager.serviceClient().getManagedHsms().purgeDeleted(managedHsm.name(), managedHsm.regionName());
        }
    }

    /*
     * create or get managed hsm instance
     */
    // TODO(xiaofei) support managedHsm creation
    private ManagedHsm createManagedHsm(String mhsmName) {
        String objectId = azureCliSignedInUser().id();

        keyVaultManager.resourceManager().resourceGroups().define(rgName).withRegion(Region.US_EAST2).create();
        ManagedHsmInner inner = keyVaultManager.serviceClient()
            .getManagedHsms()
            .createOrUpdate(
                rgName,
                mhsmName,
                new ManagedHsmInner()
                    .withLocation(Region.US_EAST2.name())
                    .withSku(
                        new ManagedHsmSku().withFamily(ManagedHsmSkuFamily.B).withName(ManagedHsmSkuName.STANDARD_B1))
                    .withProperties(
                        new ManagedHsmProperties()
                            .withTenantId(UUID.fromString(authorizationManager.tenantId()))
                            .withInitialAdminObjectIds(Arrays.asList(objectId))
                            .withEnableSoftDelete(true)
                            .withSoftDeleteRetentionInDays(7)
                            .withEnablePurgeProtection(false)), // DO NOT set it to true, otherwise you can't purge the instance
                Context.NONE);

        keyVaultManager.serviceClient()
            .getManagedHsms()
            .createOrUpdate(rgName, inner.name(), inner);

        return keyVaultManager.managedHsms().getByResourceGroup(rgName, inner.name());
    }
}
