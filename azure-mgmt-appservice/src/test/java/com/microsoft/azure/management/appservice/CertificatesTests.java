/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class CertificatesTests extends AppServiceTestBase {
    private static final String RG_NAME = "javacsmrg319";
    private static final String CERTIFICATE_NAME = "javagoodcert319";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        //resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    public void canCRDCertificate() throws Exception {
        Vault vault = keyVaultManager.vaults().getByGroup(RG_NAME, "bananagraphwebapp319com");
        AppServiceCertificate certificate = appServiceManager.certificates().define("bananacert")
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(RG_NAME)
                .withExistingCertificateOrder(appServiceManager.certificateOrders().getByGroup(RG_NAME, "graphwebapp319"))
                .create();
        Assert.assertNotNull(certificate);

        // CREATE
        certificate = appServiceManager.certificates().define(CERTIFICATE_NAME)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(RG_NAME)
                .withPfxFile(new File("/Users/jianghlu/Documents/code/certs/myserver.pfx"))
                .withPfxPassword("StrongPass!123")
                .create();
        Assert.assertNotNull(certificate);
    }
}