// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.CertificateContact;
import com.azure.security.keyvault.certificates.models.IssuerProperties;
import com.azure.security.keyvault.certificates.models.CertificateOperation;

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
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Let's create a self signed certificate valid for 1 year. if the certificate
        //   already exists in the key vault, then a new version of the certificate is created.
        CertificatePolicy policy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certificatePoller = certificateClient.beginCreateCertificate("certName", policy, true, tags);
        certificatePoller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);

        KeyVaultCertificate cert = certificatePoller.getFinalResult();

        //Let's create a certificate issuer.
        CertificateIssuer issuer = new CertificateIssuer("myIssuer", "Test");
        CertificateIssuer myIssuer = certificateClient.createIssuer(issuer);
        System.out.printf("Issuer created with name %s and provider %s", myIssuer.getName(),
            myIssuer.getProvider());

        //Let's create a certificate signed by our issuer.
        certificateClient.beginCreateCertificate("myCertificate",
            new CertificatePolicy("myIssuer", "CN=SignedJavaPkcs12"), true, tags)
            .waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);


        // Let's list all the certificates in the key vault.
        for (CertificateProperties certificate : certificateClient.listPropertiesOfCertificates()) {
            KeyVaultCertificate certificateWithAllProperties = certificateClient
                .getCertificateVersion(certificate.getName(), certificate.getVersion());
            System.out.printf("Received certificate with name %s and secret id %s", certificateWithAllProperties
                    .getProperties().getName(),
                certificateWithAllProperties.getSecretId());
        }

        // Let's list all certificate versions of the certificate.
        for (CertificateProperties certificate : certificateClient.listPropertiesOfCertificateVersions("myCertificate")) {
            KeyVaultCertificate certificateWithAllProperties = certificateClient
                .getCertificateVersion(certificate.getName(), certificate.getVersion());
            System.out.printf("Received certificate with name %s and version %s", certificateWithAllProperties
                    .getProperties().getName(),
                certificateWithAllProperties.getProperties().getVersion());
        }

        //Let's list all certificate issuers in the key vault.
        for (IssuerProperties certIssuer : certificateClient.listPropertiesOfIssuers()) {
            CertificateIssuer retrievedIssuer = certificateClient.getIssuer(certIssuer.getName());
            System.out.printf("Received issuer with name %s and provider %s", retrievedIssuer.getName(),
                retrievedIssuer.getProvider());
        }

        // Let's set certificate contacts on the Key vault.
        CertificateContact contactToAdd = new CertificateContact("user", "useremail@exmaple.com");
        for (CertificateContact contact : certificateClient.setContacts(Arrays.asList(contactToAdd))) {
            System.out.printf("Added contact with name %s and email %s to key vault", contact.getName(),
                contact.getEmail());
        }

        // Let's list all certificate contacts in the key vault.
        for (CertificateContact contact : certificateClient.listContacts()) {
            System.out.printf("Retrieved contact with name %s and email %s from the key vault", contact.getName(),
                contact.getEmail());
        }

        // Let's delete all certificate contacts in the key vault.
        for (CertificateContact contact : certificateClient.deleteContacts()) {
            System.out.printf("Deleted contact with name %s and email %s from key vault", contact.getName(),
                contact.getEmail());
        }
    }
}
