// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.Issuer;
import com.azure.security.keyvault.certificates.models.Contact;
import com.azure.security.keyvault.certificates.models.Certificate;
import com.azure.security.keyvault.certificates.models.CertificateBase;
import com.azure.security.keyvault.certificates.models.IssuerBase;

import java.time.Duration;
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

        try {
            CertificateOperation certificateOperation = certificateClient.createCertificate("certName",
                policy, tags, Duration.ofMillis(60000));
            System.out.printf("Certificate operation status %s \n", certificateOperation.status());
        } catch (IllegalStateException e) {
            // Certificate wasn't created in the specified duration.
            // Log / Handle here
        }


        //Let's create a certificate issuer.
        Issuer issuer = new Issuer("myIssuer", "Test");
        Issuer myIssuer = certificateClient.createCertificateIssuer(issuer);
        System.out.printf("Issuer created with name %s and provider %s", myIssuer.name(), myIssuer.provider());

        //Let's create a certificate signed by our issuer.
        try {
            CertificateOperation certificateOperation = certificateClient.createCertificate("myCertificate",
                new CertificatePolicy("myIssuer", "CN=SignedJavaPkcs12"), Duration.ofMillis(60000));
            System.out.printf("Certificate operation status %s \n", certificateOperation.status());
        } catch (IllegalStateException e) {
            // Certificate wasn't created in the specified duration.
            // Log / Handle here
        }

        // Let's list all the certificates in the key vault.
        for (CertificateBase certificate : certificateClient.listCertificates()) {
            Certificate certificateWithAllProperties = certificateClient.getCertificate(certificate);
            System.out.printf("Received certificate with name %s and secret id %s", certificateWithAllProperties.name(),
                certificateWithAllProperties.secretId());
        }

        // Let's list all certificate versions of the certificate.
        for (CertificateBase certificate : certificateClient.listCertificateVersions("myCertificate")) {
            Certificate certificateWithAllProperties = certificateClient.getCertificate(certificate);
            System.out.printf("Received certificate with name %s and version %s", certificateWithAllProperties.name(),
                certificateWithAllProperties.version());
        }

        //Let's list all certificate issuers in the key vault.
        for (IssuerBase certIssuer : certificateClient.listCertificateIssuers()) {
            Issuer retrievedIssuer = certificateClient.getCertificateIssuer(certIssuer);
            System.out.printf("Received issuer with name %s and provider %s", retrievedIssuer.name(),
                retrievedIssuer.provider());
        }

        // Let's set certificate contacts on the Key vault.
        Contact contactToAdd = new Contact("user", "useremail@exmaple.com");
        for (Contact contact : certificateClient.setCertificateContacts(Arrays.asList(contactToAdd))) {
            System.out.printf("Added contact with name %s and email %s to key vault", contact.name(),
                contact.emailAddress());
        }

        // Let's list all certificate contacts in the key vault.
        for (Contact contact : certificateClient.listCertificateContacts()) {
            System.out.printf("Retrieved contact with name %s and email %s from the key vault", contact.name(),
                contact.emailAddress());
        }

        // Let's delete all certificate contacts in the key vault.
        for (Contact contact : certificateClient.deleteCertificateContacts()) {
            System.out.printf("Deleted contact with name %s and email %s from key vault", contact.name(),
                contact.emailAddress());
        }
    }
}
