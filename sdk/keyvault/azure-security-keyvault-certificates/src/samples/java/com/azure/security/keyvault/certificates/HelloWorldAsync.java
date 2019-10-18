// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.SubjectAlternativeNames;
import com.azure.security.keyvault.certificates.models.Certificate;
import com.azure.security.keyvault.certificates.models.webkey.CertificateKeyCurveName;
import com.azure.security.keyvault.certificates.models.webkey.CertificateKeyType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrates how to asynchronously set, get, update and delete a key.
 */
public class HelloWorldAsync {
    /**
     * Authenticates with the key vault and shows how to asynchronously set, get, update and delete a key in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws InterruptedException {

        // Instantiate an async key client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        CertificateAsyncClient certificateAsyncClient = new CertificateClientBuilder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // Let's create a self signed certificate valid for 1 year. if the certificate
        //   already exists in the key vault, then a new version of the certificate is created.
        CertificatePolicy policy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12")
            .setSubjectAlternativeNames(SubjectAlternativeNames.fromEmails(Arrays.asList("wow@gmail.com")))
            .setReuseKey(true)
            .setKeyType(CertificateKeyType.EC)
            .setKeyCurveName(CertificateKeyCurveName.P_256);
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        certificateAsyncClient.beginCreateCertificate("certificateName", policy, true, tags)
            .getObserver().subscribe(pollResponse -> {
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println(pollResponse.getStatus());
                System.out.println(pollResponse.getValue().getStatus());
                System.out.println(pollResponse.getValue().getStatusDetails());
            });

        Thread.sleep(22000);

        // Let's Get the latest version of the certificate from the key vault.
        certificateAsyncClient.getCertificateWithPolicy("certificateName")
            .subscribe(certificateResponse ->
                System.out.printf("Certificate is returned with name %s and secretId %s %n", certificateResponse.getProperties().getName(),
                    certificateResponse.getSecretId()));

        // After some time, we need to disable the certificate temporarily, so we update the enabled status of the certificate.
        // The update method can be used to update the enabled status of the certificate.
        certificateAsyncClient.getCertificateWithPolicy("certificateName")
            .subscribe(certificateResponseValue -> {
                Certificate certificate = certificateResponseValue;
                //Update enabled status of the certificate
                certificate.getProperties().setEnabled(false);
                certificateAsyncClient.updateCertificateProperties(certificate.getProperties())
                    .subscribe(certificateResponse ->
                        System.out.printf("Certificate's enabled status %s %n",
                            certificateResponse.getProperties().isEnabled().toString()));
            });

        Thread.sleep(3000);


        //Let's create a certificate issuer.
        certificateAsyncClient.createIssuer("myIssuer", "Test")
            .subscribe(issuer -> {
                System.out.printf("Issuer created with %s and %s", issuer.getName(), issuer.getProperties().getProvider());
            });

        Thread.sleep(2000);


        // Let's fetch the issuer we just created from the key vault.
        certificateAsyncClient.getIssuer("myIssuer")
            .subscribe(issuer -> {
                System.out.printf("Issuer returned with %s and %s", issuer.getName(), issuer.getProperties().getProvider());
            });

        Thread.sleep(2000);

        //Let's create a certificate signed by our issuer.
        certificateAsyncClient.beginCreateCertificate("myCertificate", new CertificatePolicy("myIssuer", "CN=IssuerSignedJavaPkcs12"), true, tags)
            .getObserver().subscribe(pollResponse -> {
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println(pollResponse.getStatus());
                System.out.println(pollResponse.getValue().getStatus());
                System.out.println(pollResponse.getValue().getStatusDetails());
            });

        Thread.sleep(22000);

        // Let's Get the latest version of our certificate from the key vault.
        certificateAsyncClient.getCertificateWithPolicy("myCertificate")
            .subscribe(certificateResponse ->
                System.out.printf("Certificate is returned with name %s and secretId %s %n", certificateResponse.getProperties().getName(),
                    certificateResponse.getSecretId()));

        Thread.sleep(2000);

        // The certificates and issuers are no longer needed, need to delete it from the key vault.
        certificateAsyncClient.deleteCertificate("certificateName")
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Certificate's Recovery Id %s %n", deletedSecretResponse.getRecoveryId()));

        certificateAsyncClient.deleteCertificate("myCertificate")
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Certificate's Recovery Id %s %n", deletedSecretResponse.getRecoveryId()));


        certificateAsyncClient.deleteIssuerWithResponse("myIssuer")
            .subscribe(deletedIssuerResponse ->
                System.out.printf("Deleted issuer with name %s %n", deletedIssuerResponse.getValue().getName()));

        // To ensure certificate is deleted on server side.
        Thread.sleep(50000);

        // If the keyvault is soft-delete enabled, then for permanent deletion  deleted certificates need to be purged.
        certificateAsyncClient.purgeDeletedCertificateWithResponse("certificateName")
            .subscribe(purgeResponse ->
                System.out.printf("Purge Status response %d %n", purgeResponse.getStatusCode()));

        certificateAsyncClient.purgeDeletedCertificateWithResponse("myCertificate")
            .subscribe(purgeResponse ->
                System.out.printf("Purge Status response %d %n", purgeResponse.getStatusCode()));

        Thread.sleep(4000);
    }
}
