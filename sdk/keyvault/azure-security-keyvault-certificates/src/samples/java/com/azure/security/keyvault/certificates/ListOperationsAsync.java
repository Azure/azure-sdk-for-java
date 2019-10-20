// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.SubjectAlternativeNames;
import com.azure.security.keyvault.certificates.models.webkey.CertificateKeyCurveName;
import com.azure.security.keyvault.certificates.models.Contact;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrates how to asynchronously list keys and versions of a given key in the key vault.
 */
public class ListOperationsAsync {
    /**
     * Authenticates with the key vault and shows how to asynchronously list keys and list versions of a specific key in the key vault.
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
            .setKeyCurveName(CertificateKeyCurveName.P_256);
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        certificateAsyncClient.beginCreateCertificate("certificatName", policy, true, tags)
            .getObserver().subscribe(pollResponse -> {
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println(pollResponse.getStatus());
                System.out.println(pollResponse.getValue().getStatus());
                System.out.println(pollResponse.getValue().getStatusDetails());
            });

        Thread.sleep(22000);

        //Let's create a certificate issuer.
        certificateAsyncClient.createIssuer("myIssuer", "Test")
            .subscribe(issuer -> {
                System.out.printf("Issuer created with %s and %s\n", issuer.getName(), issuer.getProperties().getProvider());
            });

        Thread.sleep(2000);

        //Let's create a certificate signed by our issuer.
        certificateAsyncClient.beginCreateCertificate("myCert", new CertificatePolicy("myIssuer", "CN=IssuerSignedJavaPkcs12"), true, tags)
            .getObserver().subscribe(pollResponse -> {
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println(pollResponse.getStatus());
                System.out.println(pollResponse.getValue().getStatus());
                System.out.println(pollResponse.getValue().getStatusDetails());
            });

        Thread.sleep(22000);

        // Let's list all the certificates in the key vault.
        certificateAsyncClient.listCertificates()
            .subscribe(certificateBase -> certificateAsyncClient.getCertificate(certificateBase)
                .subscribe(certificateResponse -> System.out.printf("Received certificate with name %s and key id %s \n",
                    certificateResponse.getProperties().getName(), certificateResponse.getKeyId())));

        Thread.sleep(5000);

        // Let's list all certificate versions of the certificate.
        certificateAsyncClient.listCertificateVersions("myCertificate")
            .subscribe(certificateBase -> certificateAsyncClient.getCertificate(certificateBase)
                .subscribe(certificateResponse -> System.out.printf("Received certificate with name %s and key id %s\n",
                    certificateResponse.getProperties().getName(), certificateResponse.getKeyId())));

        Thread.sleep(5000);

        //Let's list all certificate issuers in the key vault.
        certificateAsyncClient.listIssuers()
            .subscribe(issuerBase -> certificateAsyncClient.getIssuer(issuerBase)
                .subscribe(issuerResponse -> System.out.printf("Received issuer with name %s and provider %s\n",
                    issuerResponse.getName(), issuerResponse.getProperties().getProvider())));

        Thread.sleep(5000);

        // Let's set certificate contacts on the Key vault.
        Contact oontactToAdd = new Contact("user", "useremail@exmaple.com");
        certificateAsyncClient.setContacts(Arrays.asList(oontactToAdd)).subscribe(contact ->
            System.out.printf("Contact name %s and email %s\n", contact.getName(), contact.getEmailAddress())
        );

        Thread.sleep(3000);

        // Let's list all certificate contacts in the key vault.
        certificateAsyncClient.listContacts().subscribe(contact ->
            System.out.printf("Contact name %s and email %s\n", contact.getName(), contact.getEmailAddress())
        );

        Thread.sleep(3000);

        // Let's delete all certificate contacts in the key vault.
        certificateAsyncClient.listContacts().subscribe(contact ->
            System.out.printf("Deleted Contact name %s and email %s\n", contact.getName(), contact.getEmailAddress())
        );

        Thread.sleep(2000);
    }
}
