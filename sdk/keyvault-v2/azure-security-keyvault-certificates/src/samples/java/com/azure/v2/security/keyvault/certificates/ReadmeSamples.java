// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates;

import com.azure.v2.core.http.polling.LongRunningOperationStatus;
import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.Poller;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.certificates.models.CertificateOperation;
import com.azure.v2.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.v2.security.keyvault.certificates.models.CertificateProperties;
import com.azure.v2.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.v2.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.v2.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import io.clientcore.core.http.models.HttpResponseException;

@SuppressWarnings("unused")
public class ReadmeSamples {
    private final CertificateClient certificateClient = new CertificateClientBuilder()
        .endpoint("<your-key-vault-url>")
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

    public void createCertificateClient() {
        // BEGIN: readme-sample-createCertificateClient
        CertificateClient certificateClient = new CertificateClientBuilder()
            .endpoint("<your-key-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: readme-sample-createCertificateClient
    }

    public void createCertificate() {
        // BEGIN: readme-sample-createCertificate
        Poller<CertificateOperation, KeyVaultCertificateWithPolicy> certificatePoller =
            certificateClient.beginCreateCertificate("certificateName", CertificatePolicy.getDefault());
        certificatePoller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
        KeyVaultCertificate certificate = certificatePoller.getFinalResult();
        System.out.printf("Certificate created with name \"%s\"%n", certificate.getName());
        // END: readme-sample-createCertificate
    }

    public void retrieveCertificate() {
        // BEGIN: readme-sample-retrieveCertificate
        KeyVaultCertificateWithPolicy certificate = certificateClient.getCertificate("<certificate-name>");
        System.out.printf("Received certificate with name \"%s\", version %s and secret id %s%n",
            certificate.getProperties().getName(), certificate.getProperties().getVersion(), certificate.getSecretId());
        // END: readme-sample-retrieveCertificate
    }

    public void updateCertificate() {
        // BEGIN: readme-sample-updateCertificate
        // Get the certificate to update.
        KeyVaultCertificate certificate = certificateClient.getCertificate("<certificate-name>");
        // Update certificate enabled status.
        certificate.getProperties().setEnabled(false);
        KeyVaultCertificate updatedCertificate = certificateClient.updateCertificateProperties(certificate.getProperties());
        System.out.printf("Updated certificate with name \"%s\" and enabled status \"%s\"%n",
            updatedCertificate.getProperties().getName(), updatedCertificate.getProperties().isEnabled());
        // END: readme-sample-updateCertificate
    }

    public void deleteCertificate() {
        // BEGIN: readme-sample-deleteCertificate
        Poller<DeletedCertificate, Void> deleteCertificatePoller =
            certificateClient.beginDeleteCertificate("<certificate-name>");

        // Deleted certificate is accessible as soon as polling beings.
        PollResponse<DeletedCertificate> pollResponse = deleteCertificatePoller.poll();

        // Deletion date only works for a SoftDelete-enabled Key Vault.
        System.out.printf("Deleted certificate with name \"%s\" and recovery id %s", pollResponse.getValue().getName(),
            pollResponse.getValue().getRecoveryId());

        // Certificate is being deleted on server.
        deleteCertificatePoller.waitForCompletion();
        // END: readme-sample-deleteCertificate
    }

    public void listCertificates() {
        // BEGIN: readme-sample-listCertificates
        // List operations don't return the certificates with their full information. So, for each returned certificate we call
        // getCertificate to get the certificate with all its properties excluding the policy.
        for (CertificateProperties certificateProperties : certificateClient.listPropertiesOfCertificates()) {
            KeyVaultCertificate certificateWithAllProperties =
                certificateClient.getCertificate(certificateProperties.getName(), certificateProperties.getVersion());
            System.out.printf("Received certificate with name \"%s\" and secret id %s",
                certificateWithAllProperties.getProperties().getName(), certificateWithAllProperties.getSecretId());
        }
        // END: readme-sample-listCertificates
    }

    public void troubleshooting() {
        // BEGIN: readme-sample-troubleshooting
        try {
            certificateClient.getCertificate("<deleted-certificate-name>");
        } catch (HttpResponseException e) {
            System.out.println(e.getMessage());
        }
        // END: readme-sample-troubleshooting
    }
}
