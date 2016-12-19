/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class CertificateOrdersTests extends AppServiceTestBase {
    private static final String RG_NAME = "javacsmrg319";
    private static final String CERTIFICATE_NAME = "graphwildcert319";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        //resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
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