/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.*;

import java.util.List;

public class CertificateOrdersTests extends AppServiceTest {
    private static final String CERTIFICATE_NAME = "graphwildcert319";

    @Override
    protected void cleanUpResources() {
        //super.cleanUpResources();
    }

    @Test
    @Ignore("Test is failing fix it. we may not intent to create a resource here but just to fetch existing resource.")
    public void canCRUDCertificateOrder() throws Exception {
        // CREATE
        AppServiceCertificateOrder certificateOrder = appServiceManager.certificateOrders()
                .define(CERTIFICATE_NAME)
                .withExistingResourceGroup(RG_NAME)
                .withHostName("*.graph-webapp-319.com")
                .withWildcardSku()
                .withDomainVerification(appServiceManager.domains().getByGroup(RG_NAME, "graph-webapp-319.com"))
                .withNewKeyVault("graphvault", Region.US_WEST)
                .withValidYears(1)
                .create();
        Assert.assertNotNull(certificateOrder);
        // GET
        Assert.assertNotNull(appServiceManager.certificateOrders().getByGroup(RG_NAME, CERTIFICATE_NAME));
        // LIST
        List<AppServiceCertificateOrder> certificateOrders = appServiceManager.certificateOrders().listByGroup(RG_NAME);
        boolean found = false;
        for (AppServiceCertificateOrder co : certificateOrders) {
            if (CERTIFICATE_NAME.equals(co.name())) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
        // UPDATE
    }
}