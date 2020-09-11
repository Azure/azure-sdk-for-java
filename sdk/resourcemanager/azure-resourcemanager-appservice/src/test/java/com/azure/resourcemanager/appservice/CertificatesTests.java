// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appservice.models.AppServiceCertificate;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class CertificatesTests extends AppServiceTest {
    private final String certificateName = "javagoodcert319";

    @Override
    protected void cleanUpResources() {
        // super.cleanUpResources();
    }

    @Test
    @Disabled("Test is failing fix it, this is based on Existing RG and settings.")
    public void canCRDCertificate() throws Exception {
        Vault vault = keyVaultManager.vaults().getByResourceGroup(rgName, "bananagraphwebapp319com");
        AppServiceCertificate certificate =
            appServiceManager
                .certificates()
                .define("bananacert")
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(rgName)
                .withExistingCertificateOrder(
                    appServiceManager.certificateOrders().getByResourceGroup(rgName, "graphwebapp319"))
                .create();
        Assertions.assertNotNull(certificate);

        // CREATE
        certificate =
            appServiceManager
                .certificates()
                .define(certificateName)
                .withRegion(Region.US_EAST)
                .withExistingResourceGroup(rgName)
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
