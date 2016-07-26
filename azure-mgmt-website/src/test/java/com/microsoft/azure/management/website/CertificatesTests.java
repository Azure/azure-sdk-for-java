/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class CertificatesTests extends AppServiceTestBase {
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
    public void canCRUDCertificate() throws Exception {
        // CREATE
        Certificate certificate = appServiceManager.certificates()
                .define(CERTIFICATE_NAME)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME)
                .withHostName("zhachuxiang.com")
                .create();
        Assert.assertNotNull(certificate);
        // GET
        Assert.assertNotNull(appServiceManager.certificates().getByGroup(RG_NAME, CERTIFICATE_NAME));
        // LIST
        List<Certificate> certificates = appServiceManager.certificates().listByGroup(RG_NAME);
        boolean found = false;
        for (Certificate asp : certificates) {
            if (CERTIFICATE_NAME.equals(asp.name())) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);
        // UPDATE
    }
}
