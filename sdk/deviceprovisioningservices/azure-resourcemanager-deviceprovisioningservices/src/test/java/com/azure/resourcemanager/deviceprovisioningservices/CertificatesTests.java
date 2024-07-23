// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.deviceprovisioningservices;

import com.azure.core.test.annotation.LiveOnly;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.CertificateListDescriptionInner;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.CertificateResponseInner;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.ProvisioningServiceDescriptionInner;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.VerificationCodeResponseInner;
import com.azure.resourcemanager.deviceprovisioningservices.models.CertificateProperties;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CertificatesTests extends DeviceProvisioningTestBase {
    @Test
    @LiveOnly
    public void certificateCRUD() {
        ResourceManager resourceManager = createResourceManager();
        IotDpsManager iotDpsManager = createIotDpsManager();
        ResourceGroup resourceGroup = createResourceGroup(resourceManager);

        try {
            ProvisioningServiceDescriptionInner provisioningServiceDescription =
                createProvisioningService(iotDpsManager, resourceGroup);

            CertificateResponseInner certificateInner = new CertificateResponseInner()
                .withProperties(new CertificateProperties()
                    .withCertificate(Constants.Certificate.CONTENT.getBytes(StandardCharsets.UTF_8)));

            // create a new certificate
            iotDpsManager
                .serviceClient()
                .getDpsCertificates()
                .createOrUpdate(
                    resourceGroup.name(),
                    provisioningServiceDescription.name(),
                    Constants.Certificate.NAME,
                    certificateInner);

            CertificateListDescriptionInner certificateListDescription =
                iotDpsManager
                    .serviceClient()
                    .getDpsCertificates()
                    .list(
                        resourceGroup.name(),
                        provisioningServiceDescription.name());

            assertEquals(1, certificateListDescription.value().size());

            CertificateResponseInner certificate = certificateListDescription.value().get(0);
            assertFalse(certificate.properties().isVerified());
            assertEquals(Constants.Certificate.SUBJECT, certificate.properties().subject());
            assertEquals(Constants.Certificate.THUMBPRINT, certificate.properties().thumbprint());

            // verify that you can generate certificate verification codes
            VerificationCodeResponseInner verificationCodeResponse =
                iotDpsManager
                    .serviceClient()
                    .getDpsCertificates()
                    .generateVerificationCode(
                        certificate.name(),
                        certificate.etag(),
                        resourceGroup.name(),
                        provisioningServiceDescription.name());

            assertNotNull(verificationCodeResponse.properties().verificationCode());

            // delete the certificate
            iotDpsManager
                .serviceClient()
                .getDpsCertificates()
                .delete(
                    resourceGroup.name(),
                    verificationCodeResponse.etag(),
                    provisioningServiceDescription.name(),
                    certificate.name());

            // verify that the certificate isn't listed anymore
            certificateListDescription =
                iotDpsManager
                    .serviceClient()
                    .getDpsCertificates()
                    .list(
                        resourceGroup.name(),
                        provisioningServiceDescription.name());

            assertEquals(0, certificateListDescription.value().size());

        } finally {
            // No matter if the test fails or not, delete the resource group that contains these test resources
            deleteResourceGroup(resourceManager, resourceGroup);
        }
    }
}
