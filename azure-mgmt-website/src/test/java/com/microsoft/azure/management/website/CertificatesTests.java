/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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
    public void canCRDCertificateOrder() throws Exception {
        Vault vault = keyVaultManager.vaults().getByGroup(RG_NAME, "bananagraphwebapp319com");
        AppServiceCertificate certificate = appServiceManager.certificates().define("bananacert")
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(RG_NAME)
                .withKeyVaultSecretCertificateStore(vault.id(), "bananagraphwebapp319com")
                .create();

        // CREATE
//        AppServiceCertificate certificate = appServiceManager.certificates().define(CERTIFICATE_NAME)
//                .withRegion(Region.US_EAST)
//                .withExistingResourceGroup(RG_NAME)
//                .withPfxFile(new File("/Users/jianghlu/Documents/code/certs/myserver.pfx"))
//              //.withPfxBytes(byte[])
//              //.withPfxUrl(String url)
//              //.withPfxFromKeyVault(Vault vault, String secretName)
//                .withPfxFilePassword("StrongPass!123") // withPfxPassword(String), withPfxPasswordFromKeyVault(Vault vault, String secretName)
//                .create();
        Assert.assertNotNull(certificate);
    }
}