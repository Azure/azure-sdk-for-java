// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.confidentialledger;


import com.azure.core.test.annotation.LiveOnly;
import com.azure.resourcemanager.confidentialledger.models.AadBasedSecurityPrincipal;
import com.azure.resourcemanager.confidentialledger.models.CertBasedSecurityPrincipal;
import com.azure.resourcemanager.confidentialledger.models.LedgerRoleName;
import com.azure.resourcemanager.confidentialledger.models.LedgerType;
import com.azure.resourcemanager.confidentialledger.models.ConfidentialLedger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConfidentialLedgerManagementTest extends ConfidentialLedgerManagementTestBase {

    @LiveOnly
    @Test
    public void ledgerCreateTest() {
        ConfidentialLedgerManagementOperations ledgerOperations = getLedgerOperationsInstance();

        // Ledger meta data
        String ledgerName = "acl-java-sdk-create-test";
        String resourceGroupName = getTestResourceGroup().name();
        String ledgerId = "/subscriptions/" + getAzureProfile().getSubscriptionId()
            + "/resourceGroups/" + resourceGroupName.toLowerCase()
            + "/providers/Microsoft.ConfidentialLedger"
            + "/Ledgers/" + ledgerName.toLowerCase();
        String location = "eastus";
        Map<String, String> tags = mapOf("tag1", "value");
        String ledgerUri = "https://" + ledgerName.toLowerCase() + ".confidential-ledger.azure.com";
        String identityServiceUri = "https://identity.confidential-ledger.core.azure.com/ledgerIdentity/" + ledgerName.toLowerCase();
        LedgerType ledgerType = LedgerType.PUBLIC;
        List<AadBasedSecurityPrincipal> aadBasedMembers = Arrays.asList(
            new AadBasedSecurityPrincipal()
                .withPrincipalId("074307f6-5baa-46bd-afd0-304b1f46345f")
                .withTenantId("72f988bf-86f1-41af-91ab-2d7cd011db47")
                .withLedgerRoleName(LedgerRoleName.ADMINISTRATOR)
        );
        List<CertBasedSecurityPrincipal> certBasedMembers = Arrays.asList(
            new CertBasedSecurityPrincipal()
                .withCert("-----BEGIN CERTIFICATE-----MIIBxTCCAUygAwIBAgIUFXobF1KhnQzlaPcPRe2tI8fdjjwwCgYIKoZIzj0EAwMwGjEYMBYGA1UEAwwPQUNMIENsaWVudCBDZXJ0MB4XDTIyMDgxNjE2Mjk1NFoXDTIzMDgxNjE2Mjk1NFowGjEYMBYGA1UEAwwPQUNMIENsaWVudCBDZXJ0MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE1GafPSAqwzZyTJDnzD5DSyue0hANYCwr1XIMjbRDJHOPEPsPK/fMThMHPXjV4LXOVVL0bwj+V5LD3U07ZaMJztvZOrRf+c6bP59U0GZlZM/DvPYfml9es/O2e62m1il9o1MwUTAdBgNVHQ4EFgQUg9kRNgnyP1nILNXBKfGvFjgaYP0wHwYDVR0jBBgwFoAUg9kRNgnyP1nILNXBKfGvFjgaYP0wDwYDVR0TAQH/BAUwAwEB/zAKBggqhkjOPQQDAwNnADBkAjAK/K4xWMTVq9uZwiOlo80MaYjA5V8xbWQBCsyrQzfyaxr1ol61lGlGyUqM1rGVUm4CMA9D8Z2Vblz3SaJwKBTe5PlBebZfS5a6gRs/fmL1RdmIQVjSFsx8OB1/nyIdARwhaQ==-----END CERTIFICATE-----")
                .withLedgerRoleName(LedgerRoleName.CONTRIBUTOR)
        );

        // Create the ledger
        ledgerOperations.createLedger(ledgerName, resourceGroupName, location, tags, ledgerType, aadBasedMembers, certBasedMembers);

        // Check if it was created successfully
        ConfidentialLedger ledger = ledgerOperations.getLedger(resourceGroupName, ledgerName);
        assertNotNull(ledger);

        // Check ledger's metadata
        assertEquals(ledgerName, ledger.name());
        assertEquals(resourceGroupName, ledger.resourceGroupName());
        assertEquals(ledgerId, ledger.id());
        assertEquals(location, ledger.location());
        assertTrue(tags.equals(ledger.tags()));

        // Check ledger properties
        assertEquals(ledgerUri, ledger.properties().ledgerUri());
        assertEquals(identityServiceUri, ledger.properties().identityServiceUri());
        assertEquals(ledgerType, ledger.properties().ledgerType());

        // Check ledger members
        assertEquals(aadBasedMembers.size() + 1, ledger.properties().aadBasedSecurityPrincipals().size());
        assertEquals(certBasedMembers.size(), ledger.properties().certBasedSecurityPrincipals().size());
    }

    @Test
    public void ledgerDeleteTest() {
        ConfidentialLedgerManagementOperations ledgerOperations = getLedgerOperationsInstance();

        String ledgerName = "acl-java-sdk-delete-test";
        String resourceGroupName = getTestResourceGroup().name();
        String location = "eastus";
        ledgerOperations.createLedger(ledgerName, resourceGroupName, location);

        assumeTrue(ledgerOperations.getLedger(resourceGroupName, ledgerName) != null);

        ledgerOperations.deleteLedger(resourceGroupName, ledgerName);

        assertNull(ledgerOperations.getLedger(resourceGroupName, ledgerName));
    }

}
