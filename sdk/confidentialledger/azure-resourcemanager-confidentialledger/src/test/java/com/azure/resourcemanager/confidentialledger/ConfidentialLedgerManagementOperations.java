// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.confidentialledger;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.confidentialledger.models.AadBasedSecurityPrincipal;
import com.azure.resourcemanager.confidentialledger.models.CertBasedSecurityPrincipal;
import com.azure.resourcemanager.confidentialledger.models.LedgerProperties;
import com.azure.resourcemanager.confidentialledger.models.LedgerType;
import com.azure.resourcemanager.confidentialledger.models.ConfidentialLedger;


import java.util.List;
import java.util.Map;

public class ConfidentialLedgerManagementOperations {
    private ConfidentialLedgerManager ledgerManager;
    public ConfidentialLedgerManagementOperations(ConfidentialLedgerManager ledgerManager) {
        this.ledgerManager = ledgerManager;
    }
    public void createLedger(
            String ledgerName,
            String resourceGroupName,
            String location,
            Map<String, String> tags,
            LedgerType ledgerType,
            List<AadBasedSecurityPrincipal> aadBasedMembers,
            List<CertBasedSecurityPrincipal> certBasedMembers) {
        ledgerManager
                .ledgers()
                .define(ledgerName)
                .withExistingResourceGroup(resourceGroupName)
                .withRegion(location)
                .withTags(tags)
                .withProperties(
                        new LedgerProperties()
                                .withLedgerType(ledgerType)
                                .withAadBasedSecurityPrincipals(aadBasedMembers)
                                .withCertBasedSecurityPrincipals(certBasedMembers)
                )
                .create();
    }
    public void createLedger(
        String ledgerName,
        String resourceGroupName,
        String location) {
        ledgerManager
            .ledgers()
            .define(ledgerName)
            .withExistingResourceGroup(resourceGroupName)
            .withRegion(location)
            .create();
    }
    public void deleteLedger(String resourceGroupName, String ledgerName) {
        ledgerManager
                .ledgers()
                .deleteByResourceGroup(resourceGroupName, ledgerName);
    }

    public ConfidentialLedger getLedger(String resourceGroupName, String ledgerName) {
        try {
            ConfidentialLedger ledger = ledgerManager.ledgers().getByResourceGroup(resourceGroupName, ledgerName);
            return ledger;
        } catch (ManagementException ex) {
            return null;
        }
    }
}
