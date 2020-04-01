/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.core.http.rest.PagedIterable;
import com.azure.management.keyvault.Vault;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

public class CertificatesTests extends AppServiceTest {
    private final String CERTIFICATE_NAME = "javagoodcert319";

    @Override
    protected void cleanUpResources() {
        //super.cleanUpResources();
    }

    @Test
    @Disabled("Test is failing fix it, this is based on Existing RG and settings.")
    public void canCRDCertificate() throws Exception {
        Vault vault = keyVaultManager.vaults().getByResourceGroup(RG_NAME, "bananagraphwebapp319com");
        AppServiceCertificate certificate = appServiceManager.certificates().define("bananacert")
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(RG_NAME)
                .withExistingCertificateOrder(appServiceManager.certificateOrders().getByResourceGroup(RG_NAME, "graphwebapp319"))
                .create();
        Assertions.assertNotNull(certificate);

        // CREATE
        certificate = appServiceManager.certificates().define(CERTIFICATE_NAME)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(RG_NAME)
                .withPfxFile(new File("/Users/jianghlu/Documents/code/certs/myserver.pfx"))
                .withPfxPassword("StrongPass!123")
                .create();
        Assertions.assertNotNull(certificate);
    }

    @Test
    public void canListCertificate() {
        PagedIterable<AppServiceCertificate> certificates = appServiceManager.certificates().list();
    }
}