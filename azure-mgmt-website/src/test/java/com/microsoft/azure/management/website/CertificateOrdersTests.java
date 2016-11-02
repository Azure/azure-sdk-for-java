/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class CertificateOrdersTests extends AppServiceTestBase {
    private static final String RG_NAME = "javacsmrg319";
    private static final String CERTIFICATE_NAME = "javatestcert319";

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
        CertificateOrder certificateOrder = appServiceManager.certificateOrders()
                .define(CERTIFICATE_NAME)
                .withExistingResourceGroup(RG_NAME)
                .withHostName("javatest319.com")
                .withSku(CertificateProductType.STANDARD_DOMAIN_VALIDATED_SSL)
                .withValidYears(1)
//                .withExistingKeyVault("/subscriptions/0b1f6471-1bf0-4dda-aec3-cb9272f09590/resourceGroups/javacsmrg905/providers/Microsoft.KeyVault/vaults/java-keyvault-905")
                .create();
        Assert.assertNotNull(certificateOrder);
        // GET
        Assert.assertNotNull(appServiceManager.certificateOrders().getByGroup(RG_NAME, CERTIFICATE_NAME));
        // LIST
        List<CertificateOrder> certificateOrders = appServiceManager.certificateOrders().listByGroup(RG_NAME);
        boolean found = false;
        for (CertificateOrder co : certificateOrders) {
            if (CERTIFICATE_NAME.equals(co.name())) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
        // UPDATE
    }
}