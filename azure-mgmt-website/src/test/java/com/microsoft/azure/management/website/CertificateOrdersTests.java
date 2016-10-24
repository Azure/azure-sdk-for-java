/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class CertificateOrdersTests extends AppServiceTestBase {
    private static final String RG_NAME = "javacsmrg325";
    private static final String CERTIFICATE_NAME = "javatestcert325";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().delete(RG_NAME);
    }

    @Test
    public void canCRUBAppServicePlan() throws Exception {
        // CREATE
        Creatable<ResourceGroup> rgCreatable = resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(Region.US_WEST);
        CertificateOrder certificateOrder = appServiceManager.certificateOrders()
                .define(CERTIFICATE_NAME)
                .withNewResourceGroup(rgCreatable)
                .withHostName("zhachuxiang.com")
                .withSku(CertificateProductType.STANDARD_DOMAIN_VALIDATED_SSL)
                .withValidYears(1)
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