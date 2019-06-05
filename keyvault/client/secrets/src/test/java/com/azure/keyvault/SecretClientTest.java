package com.azure.keyvault;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.keyvault.models.DeletedSecret;
import com.azure.keyvault.models.Secret;
import com.azure.keyvault.models.SecretBase;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class SecretClientTest extends SecretClientTestBase {

    private SecretClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();

        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(credentials -> SecretClient.builder()
                .credential(credentials)
                .endpoint(getEndpoint())
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .build());
        } else {
            client = clientSetup(credentials -> SecretClient.builder()
                .credential(credentials)
                .endpoint(getEndpoint())
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .build());
        }
    }

    @Override
    protected void afterTest() {

        for (SecretBase secret : client.listSecrets()) {
         //   client.deleteSecret(secret.name());
        }
    }

    /**
     * Tests that a secret can be created in the key vault.
     */
    public void setSecret() {
        setSecretRunner((expected) -> assertSecretEquals(expected, client.setSecret(expected)));
    }

    /**
     * Tests that we cannot add a configuration setting when the key is an empty string.
     */
    public void setSecretEmptyName() {
        assertRestException(() -> client.setSecret("", "A value"), HttpResponseStatus.METHOD_NOT_ALLOWED.code());
    }

    /**
     * Tests that we can create secrets when value is not null or an empty string.
     */
    public void setSecretEmptyValue() {
        setSecretEmptyValueRunner((secret) -> {
            assertSecretEquals(secret, client.setSecret(secret.name(), secret.value()));
            assertSecretEquals(secret, client.getSecret(secret.name()));
        });
    }

    /**
     * Verifies that an exception is thrown when null secret object is passed for creation.
     */
    public void setSecretNull() {
        assertRunnableThrowsException(() -> client.setSecret(null), NullPointerException.class);
    }

    /**
     * Tests that a secret is able to be updated when it exists.
     */
    public void updateSecret() {
        updateSecretRunner((original, updated) -> {
            assertSecretEquals(original, client.setSecret(original));
            Secret secretToUpdate = client.getSecret(original.name()).value();
            client.updateSecret(secretToUpdate.expires(updated.expires()));
            assertSecretEquals(updated, client.getSecret(original.name()));
        });
    }

    /**
     * Tests that a secret is not able to be updated when it is disabled. 403 error is expected.
     */
    public void updateDisabledSecret() {
        updateDisabledSecretRunner((original, updated) -> {
            assertSecretEquals(original, client.setSecret(original));
            assertRestException(() -> client.getSecret(original.name()), ResourceModifiedException.class, HttpResponseStatus.FORBIDDEN.code());
        });
    }

    /**
     * Tests that an existing secret can be retrieved.
     */
    public void getSecret() {
        getSecretRunner((original) -> {
            client.setSecret(original);
            assertSecretEquals(original, client.getSecret(original.name()));
        });
    }

    /**
     * Tests that a specific version of the secret can be retrieved.
     */
    public void getSecretSpecificVersion() {
        getSecretSpecificVersionRunner((secret, secretWithNewVal) -> {
            Secret secretVersionOne = client.setSecret(secret).value();
            Secret secretVersionTwo = client.setSecret(secretWithNewVal).value();
            assertSecretEquals(secret, client.getSecret(secretVersionOne.name(), secretVersionOne.version()));
            assertSecretEquals(secretWithNewVal, client.getSecret(secretVersionTwo.name(), secretVersionTwo.version()));
        });
    }

    /**
     * Tests that an attempt to get a non-existing secret throws an error.
     */
    public void getSecretNotFound() {
        assertRestException(() -> client.getSecret("non-existing"),  ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that an existing secret can be deleted.
     */
    public void deleteSecret() {
        deleteSecretRunner((secretToDelete) -> {
            assertSecretEquals(secretToDelete,  client.setSecret(secretToDelete));
            DeletedSecret deletedSecret = client.deleteSecret(secretToDelete.name()).value();
            sleep(30000);
            assertNotNull(deletedSecret.deletedDate());
            assertNotNull(deletedSecret.recoveryId());
            assertNotNull(deletedSecret.scheduledPurgeDate());
            assertEquals(secretToDelete.name(), deletedSecret.name());
            client.purgeDeletedSecret(secretToDelete.name());
            sleep(15000);
        });
    }

    public void deleteSecretNotFound() {
        assertRestException(() -> client.deleteSecret("non-existing"), ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that a deleted secret can be retrieved on a soft-delete enabled vault.
     */
    public void getDeletedSecret() {
        getDeletedSecretRunner((secretToDeleteAndGet) -> {
//            client.purgeDeletedSecret(secretToDeleteAndGet.name());
//            sleep(15000);
            assertSecretEquals(secretToDeleteAndGet, client.setSecret(secretToDeleteAndGet));
            assertNotNull(client.deleteSecret(secretToDeleteAndGet.name()).value());
            sleep(30000);
            DeletedSecret deletedSecret = client.getDeletedSecret(secretToDeleteAndGet.name()).value();
            assertNotNull(deletedSecret.deletedDate());
            assertNotNull(deletedSecret.recoveryId());
            assertNotNull(deletedSecret.scheduledPurgeDate());
            assertEquals(secretToDeleteAndGet.name(), deletedSecret.name());
            client.purgeDeletedSecret(secretToDeleteAndGet.name());
            sleep(15000);
        });
    }

    /**
     * Tests that an attempt to retrieve a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    public void getDeletedSecretNotFound() {
        assertRestException(() -> client.getDeletedSecret("non-existing"),  ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }


    /**
     * Tests that a deleted secret can be recovered on a soft-delete enabled vault.
     */
    public void recoverDeletedSecret() {
        recoverDeletedSecretRunner((secretToDeleteAndRecover) -> {
            assertSecretEquals(secretToDeleteAndRecover, client.setSecret(secretToDeleteAndRecover));
            assertNotNull(client.deleteSecret(secretToDeleteAndRecover.name()).value());
            sleep(30000);
            Secret recoveredSecret = client.recoverDeletedSecret(secretToDeleteAndRecover.name()).value();
            assertEquals(secretToDeleteAndRecover.name(), recoveredSecret.name());
            assertEquals(secretToDeleteAndRecover.notBefore(), recoveredSecret.notBefore());
            assertEquals(secretToDeleteAndRecover.expires(), recoveredSecret.expires());
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    public void recoverDeletedSecretNotFound() {
        assertRestException(() -> client.recoverDeletedSecret("non-existing"),  ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    public void backupSecret() {
        backupSecretRunner((secretToBackup) -> {
            assertSecretEquals(secretToBackup, client.setSecret(secretToBackup));
            byte[] backupBytes = (client.backupSecret(secretToBackup.name()).value());
            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
        });
    }

    /**
     * Tests that an attempt to backup a non existing secret throws an error.
     */
    public void backupSecretNotFound() {
        assertRestException(() -> client.backupSecret("non-existing"),  ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    public void restoreSecret() {
        restoreSecretRunner((secretToBackupAndRestore) -> {
            assertSecretEquals(secretToBackupAndRestore, client.setSecret(secretToBackupAndRestore));
            byte[] backupBytes = (client.backupSecret(secretToBackupAndRestore.name()).value());
            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
            client.deleteSecret(secretToBackupAndRestore.name());
            sleep(32000);
            client.purgeDeletedSecret(secretToBackupAndRestore.name());
            sleep(15000);
            Secret restoredSecret = client.restoreSecret(backupBytes).value();
            assertEquals(secretToBackupAndRestore.name(), restoredSecret.name());
            //assertEquals(secretToBackupAndRestore.notBefore(), restoredSecret.notBefore());
            assertEquals(secretToBackupAndRestore.expires(), restoredSecret.expires());
        });
    }

    /**
     * Tests that an attempt to restore a secret from malformed backup bytes throws an error.
     */
    public void restoreSecretFromMalformedBackup() {
        byte[] secretBackupBytes = "non-existing".getBytes();
        assertRestException(() -> client.restoreSecret(secretBackupBytes), ResourceModifiedException.class, HttpResponseStatus.BAD_REQUEST.code());
    }

    /**
     * Tests that secrets can be listed in the key vault.
     */
    public void listSecrets() {
        listSecretsRunner((secrets) -> {
            HashMap<String, Secret> secretsToList = secrets;
            for(Secret secret :  secretsToList.values()){
                assertSecretEquals(secret, client.setSecret(secret));
            }
            List<SecretBase> secretsListed = client.listSecrets();
            for(SecretBase actualSecret : secretsListed){
                if(secretsToList.containsKey(actualSecret.name())){
                    Secret expectedSecret = secretsToList.get(actualSecret.name());
                    assertEquals(expectedSecret.expires(), actualSecret.expires());
                    assertEquals(expectedSecret.notBefore(), actualSecret.notBefore());
                    secretsToList.remove(actualSecret.name());
                }
            }
            assertEquals(0,secretsToList.size());
        });
    }


}
