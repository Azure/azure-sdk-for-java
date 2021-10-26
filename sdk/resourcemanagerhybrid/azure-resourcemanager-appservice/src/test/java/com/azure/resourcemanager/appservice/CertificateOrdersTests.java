// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appservice.models.AppServiceCertificateOrder;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class CertificateOrdersTests extends AppServiceTest {
    private final String certificateName = "graphwildcert319";

    @Override
    protected void cleanUpResources() {
        // super.cleanUpResources();
    }

    @Test
    @Disabled(
        "Test is failing fix it. we may not intent to create a resource here but just to fetch existing resource.")
    public void canCRUDCertificateOrder() throws Exception {
        // CREATE
        AppServiceCertificateOrder certificateOrder =
            appServiceManager
                .certificateOrders()
                .define(certificateName)
                .withExistingResourceGroup(rgName)
                .withHostName("*.graph-webapp-319.com")
                .withWildcardSku()
                .withDomainVerification(appServiceManager.domains().getByResourceGroup(rgName, "graph-webapp-319.com"))
                .withNewKeyVault("graphvault", Region.US_WEST)
                .withValidYears(1)
                .create();
        Assertions.assertNotNull(certificateOrder);
        // GET
        Assertions.assertNotNull(appServiceManager.certificateOrders().getByResourceGroup(rgName, certificateName));
        // LIST
        PagedIterable<AppServiceCertificateOrder> certificateOrders =
            appServiceManager.certificateOrders().listByResourceGroup(rgName);
        boolean found = false;
        for (AppServiceCertificateOrder co : certificateOrders) {
            if (certificateName.equals(co.name())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
        // UPDATE
    }
}
