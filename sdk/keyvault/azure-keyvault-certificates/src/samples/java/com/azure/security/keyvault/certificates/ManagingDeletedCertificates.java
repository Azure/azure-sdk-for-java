// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.util.polling.PollResponse;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.SubjectAlternativeNames;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.Certificate;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.webkey.CertificateKeyCurveName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrates how to list, recover and purge deleted certificates in a soft-delete isEnabled key vault.
 */
public class ManagingDeletedCertificates {
    /**
     * Authenticates with the key vault and shows how to list, recover and purge deleted certificates in a soft-delete isEnabled key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws IllegalArgumentException, InterruptedException {

        // NOTE: To manage deleted certificates, your key vault needs to have soft-delete isEnabled. Soft-delete allows deleted keys
        // to be retained for a given retention period (90 days). During this period deleted keys can be recovered and if
        // a key needs to be permanently deleted then it needs to be purged.

        // Instantiate a certificate client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        CertificateClient certificateClient = new CertificateClientBuilder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Let's create a self signed certificate valid for 1 year. if the certificate
        //   already exists in the key vault, then a new version of the certificate is created.
        CertificatePolicy policy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12")
            .setSubjectAlternativeNames(SubjectAlternativeNames.fromEmails(Arrays.asList("wow@gmail.com")))
            .setReuseKey(true)
            .setKeyCurveName(CertificateKeyCurveName.P_256);
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        Poller<CertificateOperation, Certificate> certificatePoller = certificateClient.beginCreateCertificate("certificateName", policy, tags);
        certificatePoller.blockUntil(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED);

        Certificate cert = certificatePoller.result().block();

        // The certificate is no longer needed, need to delete it from the key vault.
        DeletedCertificate deletedCertificate = certificateClient.deleteCertificate("certificateName");
        System.out.printf("Certificate is deleted with name %s and its recovery id is %s %n", deletedCertificate.getName(), deletedCertificate.getRecoveryId());

        //To ensure certificate is deleted on server side.
        Thread.sleep(30000);

        // We accidentally deleted the certificate. Let's recover it.
        // A deleted certificate can only be recovered if the key vault is soft-delete isEnabled.
        Certificate certificate = certificateClient.recoverDeletedCertificate("certificateName");
        System.out.printf(" Recovered Deleted certificate with name %s and id %s", certificate.getProperties().getName(),
            certificate.getProperties().getId());

        //To ensure certificate is recovered on server side.
        Thread.sleep(30000);

        // The certificates are no longer needed, need to delete them from the key vault.
        deletedCertificate = certificateClient.deleteCertificate("certificateName");
        System.out.printf("Certificate is deleted with name %s and its recovery id is %s %n", deletedCertificate.getName(), deletedCertificate.getRecoveryId());

        //To ensure certificate is deleted on server side.
        Thread.sleep(30000);

        // You can list all the deleted and non-purged certificates, assuming key vault is soft-delete isEnabled.
        for (DeletedCertificate deletedCert : certificateClient.listDeletedCertificates()) {
            System.out.printf("Deleted certificate's recovery Id %s", deletedCert.getRecoveryId());
        }

        // If the key vault is soft-delete isEnabled, then for permanent deletion deleted certificate need to be purged.
        certificateClient.purgeDeletedCertificate("certificateName");

        //To ensure certificate is purged on server side.
        Thread.sleep(15000);
    }
}
