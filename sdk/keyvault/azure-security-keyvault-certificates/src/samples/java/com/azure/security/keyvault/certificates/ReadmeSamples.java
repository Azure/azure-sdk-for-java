// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;

@SuppressWarnings("unused")
public class ReadmeSamples {
    private final CertificateClient certificateClient = new CertificateClientBuilder()
        .vaultUrl("<your-key-vault-url>")
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

    private final CertificateAsyncClient certificateAsyncClient = new CertificateClientBuilder()
        .vaultUrl("<your-key-vault-url>")
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildAsyncClient();

    public void createCertificateClient() {
        // BEGIN: readme-sample-createCertificateClient
        CertificateClient certificateClient = new CertificateClientBuilder()
            .vaultUrl("<your-key-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: readme-sample-createCertificateClient
    }

    public void createCertificate() {
        // BEGIN: readme-sample-createCertificate
        SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certificatePoller =
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
        SyncPoller<DeletedCertificate, Void> deleteCertificatePoller =
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
                certificateClient.getCertificateVersion(certificateProperties.getName(), certificateProperties.getVersion());
            System.out.printf("Received certificate with name \"%s\" and secret id %s",
                certificateWithAllProperties.getProperties().getName(), certificateWithAllProperties.getSecretId());
        }
        // END: readme-sample-listCertificates
    }

    public void createCertificateAsync() {
        // BEGIN: readme-sample-createCertificateAsync
        // Creates a certificate using the default policy and polls on its progress.
        certificateAsyncClient.beginCreateCertificate("<certificate-name>", CertificatePolicy.getDefault())
            .subscribe(pollResponse -> {
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println(pollResponse.getStatus());
                System.out.println(pollResponse.getValue().getStatus());
                System.out.println(pollResponse.getValue().getStatusDetails());
            });
        // END: readme-sample-createCertificateAsync
    }

    public void retrieveCertificateAsync() {
        // BEGIN: readme-sample-retrieveCertificateAsync
        certificateAsyncClient.getCertificate("<certificate-name>")
            .subscribe(certificateResponse ->
                System.out.printf("Certificate was returned with name \"%s\" and secretId %s%n",
                    certificateResponse.getProperties().getName(), certificateResponse.getSecretId()));
        // END: readme-sample-retrieveCertificateAsync
    }

    public void updateCertificateAsync() {
        // BEGIN: readme-sample-updateCertificateAsync
        certificateAsyncClient.getCertificate("<certificate-name>")
            .flatMap(certificate -> {
                // Update enabled status of the certificate.
                certificate.getProperties().setEnabled(false);
                return certificateAsyncClient.updateCertificateProperties(certificate.getProperties());
            }).subscribe(certificateResponse -> System.out.printf("Certificate's enabled status: %s%n",
                certificateResponse.getProperties().isEnabled()));
        // END: readme-sample-updateCertificateAsync
    }

    public void deleteCertificateAsync() {
        // BEGIN: readme-sample-deleteCertificateAsync
        certificateAsyncClient.beginDeleteCertificate("<certificate-name>")
            .subscribe(pollResponse -> {
                System.out.printf("Deletion status: %s%n", pollResponse.getStatus());
                System.out.printf("Deleted certificate name: %s%n", pollResponse.getValue().getName());
                System.out.printf("Certificate deletion date: %s%n", pollResponse.getValue().getDeletedOn());
            });
        // END: readme-sample-deleteCertificateAsync
    }

    public void listCertificateAsync() {
        // BEGIN: readme-sample-listCertificateAsync
        // The List Certificates operation returns certificates without their full properties, so for each certificate returned
        // we call `getCertificate` to get all its attributes excluding the policy.
        certificateAsyncClient.listPropertiesOfCertificates()
            .flatMap(certificateProperties -> certificateAsyncClient
                .getCertificateVersion(certificateProperties.getName(), certificateProperties.getVersion()))
            .subscribe(certificateResponse ->
                System.out.printf("Received certificate with name \"%s\" and key id %s", certificateResponse.getName(),
                    certificateResponse.getKeyId()));
        // END: readme-sample-listCertificateAsync
    }

    public void troubleshooting() {
        // BEGIN: readme-sample-troubleshooting
        try {
            certificateClient.getCertificate("<deleted-certificate-name>");
        } catch (ResourceNotFoundException e) {
            System.out.println(e.getMessage());
        }
        // END: readme-sample-troubleshooting
    }
}
