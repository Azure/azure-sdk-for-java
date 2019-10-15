// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.Poller;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.Certificate;
import com.azure.security.keyvault.certificates.models.Issuer;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.IssuerProperties;
import com.azure.security.keyvault.certificates.models.Contact;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrates how to perform list operation on certificates, certificate issuers and certificate contacts  in the key vault.
 */
public class ListOperations {
    /**
     * Authenticates with the key vault and shows how to list certificates, certificate issuers and contacts in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     */
    public static void main(String[] args) throws IllegalArgumentException {

        // Instantiate a certificate client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        CertificateClient certificateClient = new CertificateClientBuilder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Let's create a self signed certificate valid for 1 year. if the certificate
        //   already exists in the key vault, then a new version of the certificate is created.
        CertificatePolicy policy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        Poller<CertificateOperation, Certificate> certificatePoller = certificateClient.beginCreateCertificate("certName", policy, tags);
        certificatePoller.blockUntil(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED);

        Certificate cert = certificatePoller.result().block();

        //Let's create a certificate issuer.
        Issuer issuer = new Issuer("myIssuer", "Test");
        Issuer myIssuer = certificateClient.createIssuer(issuer);
        System.out.printf("Issuer created with name %s and provider %s", myIssuer.getName(), myIssuer.getProperties().getProvider());

        //Let's create a certificate signed by our issuer.
        certificateClient.beginCreateCertificate("myCertificate",
            new CertificatePolicy("myIssuer", "CN=SignedJavaPkcs12"), tags)
            .blockUntil(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED);


        // Let's list all the certificates in the key vault.
        for (CertificateProperties certificate : certificateClient.listCertificates()) {
            Certificate certificateWithAllProperties = certificateClient.getCertificate(certificate);
            System.out.printf("Received certificate with name %s and secret id %s", certificateWithAllProperties.getProperties().getName(),
                certificateWithAllProperties.getSecretId());
        }

        // Let's list all certificate versions of the certificate.
        for (CertificateProperties certificate : certificateClient.listCertificateVersions("myCertificate")) {
            Certificate certificateWithAllProperties = certificateClient.getCertificate(certificate);
            System.out.printf("Received certificate with name %s and version %s", certificateWithAllProperties.getProperties().getName(),
                certificateWithAllProperties.getProperties().getVersion());
        }

        //Let's list all certificate issuers in the key vault.
        for (IssuerProperties certIssuer : certificateClient.listIssuers()) {
            Issuer retrievedIssuer = certificateClient.getIssuer(certIssuer);
            System.out.printf("Received issuer with name %s and provider %s", retrievedIssuer.getName(),
                retrievedIssuer.getProperties().getProvider());
        }

        // Let's set certificate contacts on the Key vault.
        Contact contactToAdd = new Contact("user", "useremail@exmaple.com");
        for (Contact contact : certificateClient.setContacts(Arrays.asList(contactToAdd))) {
            System.out.printf("Added contact with name %s and email %s to key vault", contact.getName(),
                contact.getEmailAddress());
        }

        // Let's list all certificate contacts in the key vault.
        for (Contact contact : certificateClient.listContacts()) {
            System.out.printf("Retrieved contact with name %s and email %s from the key vault", contact.getName(),
                contact.getEmailAddress());
        }

        // Let's delete all certificate contacts in the key vault.
        for (Contact contact : certificateClient.deleteContacts()) {
            System.out.printf("Deleted contact with name %s and email %s from key vault", contact.getName(),
                contact.getEmailAddress());
        }
    }
}
