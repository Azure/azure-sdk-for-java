/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class CertificateOrdersTests extends AppServiceTest {
    private final String CERTIFICATE_NAME = "graphwildcert319";

    @Override
    protected void cleanUpResources() {
        //super.cleanUpResources();
    }

    @Test
    @Disabled("Test is failing fix it. we may not intent to create a resource here but just to fetch existing resource.")
    public void canCRUDCertificateOrder() throws Exception {
        // CREATE
        AppServiceCertificateOrder certificateOrder = appServiceManager.certificateOrders()
                .define(CERTIFICATE_NAME)
                .withExistingResourceGroup(RG_NAME)
                .withHostName("*.graph-webapp-319.com")
                .withWildcardSku()
                .withDomainVerification(appServiceManager.domains().getByResourceGroup(RG_NAME, "graph-webapp-319.com"))
                .withNewKeyVault("graphvault", Region.US_WEST)
                .withValidYears(1)
                .create();
        Assertions.assertNotNull(certificateOrder);
        // GET
        Assertions.assertNotNull(appServiceManager.certificateOrders().getByResourceGroup(RG_NAME, CERTIFICATE_NAME));
        // LIST
        PagedIterable<AppServiceCertificateOrder> certificateOrders = appServiceManager.certificateOrders().listByResourceGroup(RG_NAME);
        boolean found = false;
        for (AppServiceCertificateOrder co : certificateOrders) {
            if (CERTIFICATE_NAME.equals(co.name())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found);
        // UPDATE
    }
}