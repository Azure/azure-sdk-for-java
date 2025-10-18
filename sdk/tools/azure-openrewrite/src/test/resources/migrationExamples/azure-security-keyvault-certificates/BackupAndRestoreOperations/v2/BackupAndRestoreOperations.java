// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.v2.core.http.polling.LongRunningOperationStatus;
import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.Poller;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.certificates.CertificateClient;
import com.azure.v2.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.v2.security.keyvault.certificates.models.CertificateKeyCurveName;
import com.azure.v2.security.keyvault.certificates.models.CertificateKeyType;
import com.azure.v2.security.keyvault.certificates.models.CertificateOperation;
import com.azure.v2.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.v2.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.v2.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.v2.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.v2.security.keyvault.certificates.models.SubjectAlternativeNames;
import io.clientcore.core.http.models.RequestContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrates how to backup and restore certificates in the key vault.
 */
public class BackupAndRestoreOperations {
    /**
     * Authenticates with the key vault and shows how to backup and restore certificates in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     * @throws IOException when writing backup to file is unsuccessful.
     */
    public static void main(String[] args) throws IOException, InterruptedException, IllegalArgumentException {
        /* Instantiate a CertificateClient that will be used to call the service. Notice that the client is using
        default Azure credentials. For more information on this and other types of credentials, see this document:
        https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable.

        To get started, you'll need a URL to an Azure Key Vault. See the README
        (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/README.md)
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

        Poller<CertificateOperation, KeyVaultCertificateWithPolicy> certificatePoller =
            certificateClient.beginCreateCertificate("certificateName", policy, true, tags);
        certificatePoller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);

        KeyVaultCertificate cert = certificatePoller.getFinalResult();

        // Backups are good to have, if in case certificates get accidentally deleted by you.
        // For long term storage, it is ideal to write the backup to a file.
        String backupFilePath = "YOUR_BACKUP_FILE_PATH";
        byte[] certificateBackup = certificateClient.backupCertificate("certificateName");

        System.out.printf("Backed up certificate with back up blob length %d", certificateBackup.length);

        writeBackupToFile(certificateBackup, backupFilePath);

        // The certificate is no longer in use, so you delete it.
        Poller<DeletedCertificate, Void> deletedCertificatePoller =
            certificateClient.beginDeleteCertificate("certificateName");
        // Deleted Certificate is accessible as soon as polling beings.
        PollResponse<DeletedCertificate> pollResponse = deletedCertificatePoller.poll();

        System.out.printf("Deleted certificate with name %s and recovery id %s", pollResponse.getValue().getName(),
            pollResponse.getValue().getRecoveryId());

        deletedCertificatePoller.waitForCompletion();

        // To ensure the certificate is deleted server-side.
        Thread.sleep(30000);

        // If the vault is soft-delete enabled, then you need to purge the certificate as well for permanent deletion.
        certificateClient.purgeDeletedCertificate("certificateName");

        // To ensure the certificate is purged server-side.
        Thread.sleep(15000);

        // After sometime, the certificate is required again. We can use the backup value to restore it in the key
        // vault.
        byte[] backupFromFile = Files.readAllBytes(new File(backupFilePath).toPath());
        KeyVaultCertificate restoredCertificate = certificateClient.restoreCertificateBackup(backupFromFile);

        System.out.printf(" Restored certificate with name %s and id %s", restoredCertificate.getProperties().getName(),
            restoredCertificate.getProperties().getId());
    }

    private static void writeBackupToFile(byte[] bytes, String filePath) {
        try {
            File file = new File(filePath);

            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();

            OutputStream os = new FileOutputStream(file);
            os.write(bytes);

            System.out.println("Successfully wrote backup to file.");

            // Close the file
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
