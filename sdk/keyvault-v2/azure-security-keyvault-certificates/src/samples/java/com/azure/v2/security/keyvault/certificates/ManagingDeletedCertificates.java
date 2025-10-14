// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates;

import com.azure.v2.core.http.polling.LongRunningOperationStatus;
import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.Poller;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.certificates.models.CertificateKeyCurveName;
import com.azure.v2.security.keyvault.certificates.models.CertificateKeyType;
import com.azure.v2.security.keyvault.certificates.models.CertificateOperation;
import com.azure.v2.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.v2.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.v2.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.v2.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.v2.security.keyvault.certificates.models.SubjectAlternativeNames;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrates how to list, recover and purge deleted certificates in a soft-delete enabled key vault.
 */
public class ManagingDeletedCertificates {
    /**
     * Authenticates with the key vault and shows how to list, recover and purge deleted certificates in a soft-delete enabled key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws IllegalArgumentException, InterruptedException {
        /* NOTE: To manage deleted certificates, your key vault needs to have soft-delete enabled. Soft-delete allows
        deleted certificates to be retained for a given retention period (90 days). During this period deleted
        certificates can be recovered and if a certificates needs to be permanently deleted then it needs to be purged.
        */

        /* Instantiate a CertificateClient that will be used to call the service. Notice that the client is using
        default Azure credentials. For more information on this and other types of credentials, see this document:
        https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable.

        To get started, you'll need a URL to an Azure Key Vault. See the README
        (https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-certificates/README.md)
        for links and instructions. */
        CertificateClient certificateClient = new CertificateClientBuilder()
            .endpoint("<your-key-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Let's create a self-signed certificate valid for 1 year. If the certificate already exists in the key vault,
        // then a new version of the certificate is created.
        CertificatePolicy policy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12")
            .setSubjectAlternativeNames(new SubjectAlternativeNames().setEmails(Arrays.asList("wow@gmail.com")))
            .setKeyReusable(true)
            .setKeyCurveName(CertificateKeyCurveName.P_256)
            .setKeyType(CertificateKeyType.EC);
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        Poller<CertificateOperation, KeyVaultCertificateWithPolicy> certificatePoller = null;
            //certificateClient.beginCreateCertificate("certificateName", policy, true,  tags);
        certificatePoller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);

        KeyVaultCertificate cert = certificatePoller.getFinalResult();

        // The certificate is no longer needed, need to delete it from the key vault.
        Poller<DeletedCertificate, Void> deletedCertificatePoller = null;
            //certificateClient.beginDeleteCertificate("certificateName");
        // The deleted certificate is accessible as soon as polling beings.
        PollResponse<DeletedCertificate> pollResponse = deletedCertificatePoller.poll();

        System.out.printf("Deleted certificate with name %s and recovery id %s", pollResponse.getValue().getName(),
            pollResponse.getValue().getRecoveryId());

        deletedCertificatePoller.waitForCompletion();

        // To ensure the certificate is deleted server-side.
        Thread.sleep(30000);

        // We accidentally deleted the certificate. Let's recover it.
        // A deleted certificate can only be recovered if the key vault is soft-delete enabled.
        Poller<KeyVaultCertificateWithPolicy, Void> recoverCertPoller = null;
            //certificateClient.beginRecoverDeletedCertificate("certificateName");
        // A recovered certificate is accessible as soon as polling beings
        PollResponse<KeyVaultCertificateWithPolicy> recoverPollResponse = recoverCertPoller.poll();

        System.out.printf(" Recovered Deleted certificate with name %s and id %s", recoverPollResponse.getValue()
            .getProperties().getName(), recoverPollResponse.getValue().getProperties().getId());
        recoverCertPoller.waitForCompletion();

        // To ensure the certificate is recovered server-side.
        Thread.sleep(30000);

        // The certificate is no longer needed, need to delete them from the key vault.
        deletedCertificatePoller = null;
            //certificateClient.beginDeleteCertificate("certificateName");
        // The deleted certificate is accessible as soon as polling beings.
        PollResponse<DeletedCertificate> deletePollResponse = deletedCertificatePoller.poll();

        System.out.printf("Deleted certificate with name %s and recovery id %s", deletePollResponse.getValue().getName(),
            deletePollResponse.getValue().getRecoveryId());

        deletedCertificatePoller.waitForCompletion();

        // To ensure the certificate is deleted server-side.
        Thread.sleep(30000);

        // You can list all the deleted and non-purged certificates, assuming the key vault is soft-delete enabled.
        for (DeletedCertificate deletedCert : certificateClient.listDeletedCertificates()) {
            System.out.printf("Deleted certificate's recovery Id %s", deletedCert.getRecoveryId());
        }

        // If the key vault is soft-delete enabled, then deleted certificates need to be purged for permanent deletion.
        certificateClient.purgeDeletedCertificate("certificateName");

        // To ensure the certificate is purged server-side.
        Thread.sleep(15000);
    }
}
