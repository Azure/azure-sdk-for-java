// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.deviceprovisioningservices;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.CertificateListDescriptionInner;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.CertificateResponseInner;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.ProvisioningServiceDescriptionInner;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.VerificationCodeResponseInner;
import com.azure.resourcemanager.deviceprovisioningservices.models.CertificateBodyDescription;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CertificatesTests extends DeviceProvisioningTestBase
{
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void CertificateCRUD() {
        ResourceManager resourceManager = createResourceManager();
        IotDpsManager iotDpsManager = createIotDpsManager();
        ResourceGroup resourceGroup = createResourceGroup(resourceManager);

        try {
            ProvisioningServiceDescriptionInner provisioningServiceDescription =
                createProvisioningService(iotDpsManager, resourceGroup);

            CertificateBodyDescription certificateBodyDescription =
                new CertificateBodyDescription().withCertificate(Constants.Certificate.CONTENT);

            // create a new certificate
            iotDpsManager
                .serviceClient()
                .getDpsCertificates()
                .createOrUpdate(
                    resourceGroup.name(),
                    provisioningServiceDescription.name(),
                    Constants.Certificate.NAME,
                    certificateBodyDescription);

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
            resourceManager.resourceGroups().beginDeleteByName(resourceGroup.name());
        }
    }
}
