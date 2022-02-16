package com.azure.aot.graalvm.samples.keyvault.certificates;

import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.security.keyvault.certificates.models.CertificateKeyCurveName;
import com.azure.security.keyvault.certificates.models.CertificateKeyType;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.security.keyvault.certificates.models.SubjectAlternativeNames;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KeyVaultCertificatesSample {
    private static final String AZURE_KEY_VAULT_URL = System.getenv("AZURE_KEY_VAULT_URL");

    public static void runSample() {
        System.out.println("\n================================================================");
        System.out.println(" Starting Key Vault Certificates Sample");
        System.out.println("================================================================");

        if (AZURE_KEY_VAULT_URL == null || AZURE_KEY_VAULT_URL.isEmpty()) {
            System.err.println("azure_key_vault_url environment variable is not set - exiting");
            System.exit(-1);
        }

        // Instantiate a certificate client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        CertificateClient certificateClient = new CertificateClientBuilder()
                .vaultUrl(AZURE_KEY_VAULT_URL)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
        System.out.println("Created Certificate client");

        // Let's create a self signed certificate valid for 1 year. if the certificate
        // already exists in the key vault, then a new version of the certificate is created.
        CertificatePolicy policy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12")
                .setSubjectAlternativeNames(new SubjectAlternativeNames().setEmails(Arrays.asList("wow@gmail.com")))
                .setKeyReusable(true)
                .setKeyType(CertificateKeyType.EC)
                .setKeyCurveName(CertificateKeyCurveName.P_256)
                .setValidityInMonths(12);
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        System.out.println("Creating new certificate");
        SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certificatePoller =
                certificateClient.beginCreateCertificate("certificateName", policy, true, tags);
        certificatePoller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);

        KeyVaultCertificate cert = certificatePoller.getFinalResult();

        // Let's Get the latest version of the certificate from the key vault.
        System.out.println("Retrieving the new certificate from the key vault");
        KeyVaultCertificate certificate = certificateClient.getCertificate("certificateName");
        System.out.printf("Certificate is returned with name %s and secret id %s \n", certificate.getProperties().getName(),
                certificate.getSecretId());

        // After some time, we need to disable the certificate temporarily, so we update the enabled status of the certificate.
        // The update method can be used to update the enabled status of the certificate.
        certificate.getProperties().setEnabled(false);
        KeyVaultCertificate updatedCertificate = certificateClient.updateCertificateProperties(certificate.getProperties());
        System.out.printf("Certificate's updated enabled status is %s \n", updatedCertificate.getProperties().isEnabled());


        //Let's create a certificate issuer.
        CertificateIssuer issuer = new CertificateIssuer("myIssuer", "Test");
        CertificateIssuer myIssuer = certificateClient.createIssuer(issuer);
        System.out.printf("Issuer created with name %s and provider %s", myIssuer.getName(), myIssuer.getProvider());

        // Let's fetch the issuer we just created from the key vault.
        myIssuer = certificateClient.getIssuer("myIssuer");
        System.out.printf("Issuer retrieved with name %s and provider %s", myIssuer.getName(), myIssuer.getProvider());

        //Let's create a certificate signed by our issuer.
        certificateClient.beginCreateCertificate("myCertificate",
                        new CertificatePolicy("myIssuer", "CN=SelfSignedJavaPkcs12"), true, tags)
                .waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);

        // Let's Get the latest version of our certificate from the key vault.
        KeyVaultCertificate myCert = certificateClient.getCertificate("myCertificate");
        System.out.printf("Certificate is returned with name %s and secret id %s \n", myCert.getProperties().getName(),
                myCert.getSecretId());

        // The certificates and issuers are no longer needed, need to delete it from the key vault.
        SyncPoller<DeletedCertificate, Void> deletedCertificatePoller =
                certificateClient.beginDeleteCertificate("certificateName");
        // Deleted Certificate is accessible as soon as polling beings.
        PollResponse<DeletedCertificate> pollResponse = deletedCertificatePoller.poll();
        System.out.printf("Deleted certificate with name %s and recovery id %s", pollResponse.getValue().getName(),
                pollResponse.getValue().getRecoveryId());
        deletedCertificatePoller.waitForCompletion();

        SyncPoller<DeletedCertificate, Void> deletedCertPoller =
                certificateClient.beginDeleteCertificate("myCertificate");
        // Deleted Certificate is accessible as soon as polling beings.
        PollResponse<DeletedCertificate> deletePollResponse = deletedCertPoller.poll();
        System.out.printf("Deleted certificate with name %s and recovery id %s", deletePollResponse.getValue().getName(),
                deletePollResponse.getValue().getRecoveryId());
        deletedCertificatePoller.waitForCompletion();

        CertificateIssuer deleteCertificateIssuer = certificateClient.deleteIssuer("myIssuer");
        System.out.printf("Certificate issuer is permanently deleted with name %s and provider is %s \n", deleteCertificateIssuer.getName(), deleteCertificateIssuer.getProvider());

        // If the keyvault is soft-delete enabled, then for permanent deletion  deleted certificates need to be purged.
        certificateClient.purgeDeletedCertificate("certificateName");
        certificateClient.purgeDeletedCertificate("myCertificate");

        System.out.println("\n================================================================");
        System.out.println(" Key Vault Keys Certificates Complete");
        System.out.println("================================================================");
        System.exit(0);
    }
}
