/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.*;

import java.io.File;

public class CertificatesTests extends AppServiceTest {
    private static final String CERTIFICATE_NAME = "javagoodcert319";

    @Override
    protected void cleanUpResources() {
        //super.cleanUpResources();
    }

    @Test
    @Ignore("Test is failing fix it, this is based on Existing RG and settings.")
    public void canCRDCertificate() throws Exception {
        Vault vault = keyVaultManager.vaults().getByResourceGroup(RG_NAME, "bananagraphwebapp319com");
        AppServiceCertificate certificate = appServiceManager.certificates().define("bananacert")
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(RG_NAME)
                .withExistingCertificateOrder(appServiceManager.certificateOrders().getByResourceGroup(RG_NAME, "graphwebapp319"))
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