// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.ECKeyOptions;
import com.azure.security.keyvault.certificates.models.SubjectAlternativeNames;
import com.azure.security.keyvault.certificates.models.Certificate;
import com.azure.security.keyvault.certificates.models.webkey.KeyCurveName;
import com.azure.security.keyvault.certificates.models.Issuer;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrates how to set, get, update and delete a certificate.
 */
public class HelloWorld {

    /**
     * Authenticates with the key vault and shows how to set, get, update and delete a certificate in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws InterruptedException, IllegalArgumentException {

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
            .subjectAlternativeNames(SubjectAlternativeNames.fromEmails(Arrays.asList("wow@gmail.com")))
            .keyOptions(new ECKeyOptions()
                .reuseKey(true)
                .curve(KeyCurveName.P_256))
            .validityInMonths(12);
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        try {
            CertificateOperation certificateOperation = certificateClient.createCertificate("certificateName",
                policy, tags, Duration.ofMillis(60000));
            System.out.printf("Certificate operation status %s \n", certificateOperation.status());
        } catch (IllegalStateException e) {
            // Certificate wasn't created in the specified duration.
            // Log / Handle here
        }

        // Let's Get the latest version of the certificate from the key vault.
        Certificate certificate = certificateClient.getCertificateWithPolicy("certificateName");
        System.out.printf("Certificate is returned with name %s and secret id %s \n", certificate.name(),
            certificate.secretId());

        // After some time, we need to disable the certificate temporarily, so we update the enabled status of the certificate.
        // The update method can be used to update the enabled status of the certificate.
        certificate.enabled(false);
        Certificate updatedCertificate = certificateClient.updateCertificate(certificate);
        System.out.printf("Certificate's updated enabled status is %s \n", updatedCertificate.enabled());


        //Let's create a certificate issuer.
        Issuer issuer = new Issuer("myIssuer", "Test");
        Issuer myIssuer = certificateClient.createCertificateIssuer(issuer);
        System.out.printf("Issuer created with name %s and provider %s", myIssuer.name(), myIssuer.provider());

        // Let's fetch the issuer we just created from the key vault.
        myIssuer = certificateClient.getCertificateIssuer("myIssuer");
        System.out.printf("Issuer retrieved with name %s and provider %s", myIssuer.name(), myIssuer.provider());


        //Let's create a certificate signed by our issuer.
        try {
            CertificateOperation certificateOperation = certificateClient.createCertificate("myCertificate",
                new CertificatePolicy("myIssuer", "CN=SelfSignedJavaPkcs12"), Duration.ofMillis(60000));
            System.out.printf("Certificate operation status %s \n", certificateOperation.status());
        } catch (IllegalStateException e) {
            // Certificate wasn't created in the specified duration.
            // Log / Handle here
        }

        // Let's Get the latest version of our certificate from the key vault.
        Certificate myCert = certificateClient.getCertificateWithPolicy("myCertificate");
        System.out.printf("Certificate is returned with name %s and secret id %s \n", myCert.name(),
            myCert.secretId());

        // The certificates and issuers are no longer needed, need to delete it from the key vault.
        DeletedCertificate deletedCertificate = certificateClient.deleteCertificate("certificateName");
        System.out.printf("Certificate is deleted with name %s and its recovery id is %s \n", deletedCertificate.name(), deletedCertificate.recoveryId());

        deletedCertificate = certificateClient.deleteCertificate("myCertificate");
        System.out.printf("Certificate is deleted with name %s and its recovery id is %s \n", deletedCertificate.name(), deletedCertificate.recoveryId());

        Issuer deleteCertificateIssuer = certificateClient.deleteCertificateIssuer("myIssuer");
        System.out.printf("Certificate issuer is permanently deleted with name %s and provider is %s \n", deleteCertificateIssuer.name(), deleteCertificateIssuer.provider());

        // To ensure certificate is deleted on server side.
        Thread.sleep(30000);

        // If the keyvault is soft-delete enabled, then for permanent deletion  deleted certificates need to be purged.
        certificateClient.purgeDeletedCertificateWithResponse("certificateName");
        certificateClient.purgeDeletedCertificateWithResponse("myCertificate");
    }
}
