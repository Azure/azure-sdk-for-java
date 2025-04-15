// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.SubjectAlternativeNames;
import com.azure.security.keyvault.certificates.models.CertificateKeyCurveName;
import com.azure.security.keyvault.certificates.models.CertificateKeyType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrates how to asynchronously list, recover and purge deleted certificates in a soft-delete enabled key vault.
 */
public class ManagingDeletedCertificatesAsync {
    /**
     * Authenticates with the key vault and shows how to asynchronously list, recover and purge deleted certificates in a soft-delete enabled key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws InterruptedException {
        /* NOTE: To manage deleted certificates, your key vault needs to have soft-delete enabled. Soft-delete allows
        deleted certificates to be retained for a given retention period (90 days). During this period deleted
        certificates can be recovered and if a certificates needs to be permanently deleted then it needs to be purged.
        */

        /* Instantiate a CertificateAsyncClient that will be used to call the service. Notice that the client is using
        default Azure credentials. For more information on this and other types of credentials, see this document:
        https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable.

        To get started, you'll need a URL to an Azure Key Vault. See the README
        (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/README.md)
        for links and instructions. */
        CertificateAsyncClient certificateAsyncClient = new CertificateClientBuilder()
            .vaultUrl("<your-key-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // Let's create a self-signed certificate valid for 1 year. If the certificate already exists in the key vault,
        // then a new version of the certificate is created.
        CertificatePolicy policy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12")
            .setSubjectAlternativeNames(new SubjectAlternativeNames().setEmails(Arrays.asList("wow@gmail.com")))
            .setKeyReusable(true)
            .setKeyCurveName(CertificateKeyCurveName.P_256)
            .setKeyType(CertificateKeyType.EC);
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        certificateAsyncClient.beginCreateCertificate("certificateName", policy, true, tags)
            .subscribe(pollResponse -> {
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println(pollResponse.getStatus());
                System.out.println(pollResponse.getValue().getStatus());
                System.out.println(pollResponse.getValue().getStatusDetails());
            });

        Thread.sleep(22000);

        // The certificate is no longer needed, need to delete it from the key vault.
        certificateAsyncClient.beginDeleteCertificate("certificateName")
            .subscribe(pollResponse -> {
                System.out.println("Delete Status: " + pollResponse.getStatus().toString());
                System.out.println("Delete Certificate Name: " + pollResponse.getValue().getName());
                System.out.println("Certificate Delete Date: " + pollResponse.getValue().getDeletedOn().toString());
            });

        // To ensure the certificate is deleted server-side.
        Thread.sleep(30000);

        // We accidentally deleted the certificate. Let's recover it.
        // A deleted certificate can only be recovered if the key vault is soft-delete enabled.
        certificateAsyncClient.beginRecoverDeletedCertificate("certificateName")
            .subscribe(pollResponse -> {
                System.out.println("Recovery Status: " + pollResponse.getStatus().toString());
                System.out.println("Recover Certificate Name: " + pollResponse.getValue().getName());
                System.out.println("Recover Certificate Id: " + pollResponse.getValue().getId());
            });

        // To ensure the certificate is recovered server-side.
        Thread.sleep(10000);

        // The certificate is no longer needed, need to delete it from the key vault.
        certificateAsyncClient.beginDeleteCertificate("certificateName")
            .subscribe(pollResponse -> {
                System.out.println("Delete Status: " + pollResponse.getStatus().toString());
                System.out.println("Delete Certificate Name: " + pollResponse.getValue().getName());
                System.out.println("Certificate Delete Date: " + pollResponse.getValue().getDeletedOn().toString());
            });

        // To ensure the certificate is deleted server-side.
        Thread.sleep(30000);

        // You can list all the deleted and non-purged certificates, assuming key vault is soft-delete enabled.
        certificateAsyncClient.listDeletedCertificates()
            .subscribe(deletedCertificateResponse ->  System.out.printf("Deleted Certificate's Recovery Id %s %n",
                deletedCertificateResponse.getRecoveryId()));

        Thread.sleep(15000);

        // If the keyvault is soft-delete enabled, then deleted certificates need to be purged for permanent deletion.
        certificateAsyncClient.purgeDeletedCertificateWithResponse("certificateName")
            .subscribe(purgeResponse ->
                System.out.printf("Purge Status response %d %n", purgeResponse.getStatusCode()));

        // To ensure the certificate is purged server-side.
        Thread.sleep(15000);
    }
}
