// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyRotationPolicy;
import com.azure.security.keyvault.keys.models.KeyRotationPolicyAction;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.ReleaseKeyResult;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static com.azure.security.keyvault.keys.TestUtils.buildSyncAssertingClient;
import static com.azure.security.keyvault.keys.cryptography.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyClientTest extends KeyClientTestBase {
    protected KeyClient keyClient;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    protected void createKeyClient(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion, null);
    }

    protected void createKeyClient(HttpClient httpClient, KeyServiceVersion serviceVersion, String testTenantId) {
        keyClient = getKeyClientBuilder(buildSyncAssertingClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient), testTenantId,
            getEndpoint(), serviceVersion)
            .buildClient();
    }

    /**
     * Tests that a key can be created in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        createKeyRunner((keyToCreate) -> {
            KeyVaultKey createdKey = keyClient.createKey(keyToCreate);

            assertKeyEquals(keyToCreate, createdKey);
            assertEquals("0", createdKey.getProperties().getHsmPlatform());
        });
    }

    /**
     * Tests that a key can be created in the key vault while using a different tenant ID than the one that will be
     * provided in the authentication challenge.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createKeyWithMultipleTenants(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion, testResourceNamer.randomUuid());

        createKeyRunner((keyToCreate) -> assertKeyEquals(keyToCreate, keyClient.createKey(keyToCreate)));

        KeyVaultCredentialPolicy.clearCache(); // Ensure we don't have anything cached and try again.

        createKeyRunner((keyToCreate) -> assertKeyEquals(keyToCreate, keyClient.createKey(keyToCreate)));
    }

    /**
     * Tests that an RSA key is created.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createRsaKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        createRsaKeyRunner((keyToCreate) -> assertKeyEquals(keyToCreate, keyClient.createRsaKey(keyToCreate)));
    }

    /**
     * Tests that an attempt to create a key with empty string name throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createKeyEmptyName(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        KeyType keyType = runManagedHsmTest ? KeyType.RSA_HSM : KeyType.RSA;
        assertRestException(() -> keyClient.createKey("", keyType), ResourceModifiedException.class,
            HttpURLConnection.HTTP_BAD_REQUEST);
    }

    /**
     * Tests that we cannot create keys when key type is null.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createKeyNullType(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        createKeyEmptyValueRunner((keyToCreate) ->
            assertRestException(() -> keyClient.createKey(keyToCreate.getName(), keyToCreate.getKeyType()),
                ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST));
    }

    /**
     * Verifies that an exception is thrown when null key object is passed for creation.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createKeyNull(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        assertThrows(NullPointerException.class, () -> keyClient.createKey(null));
    }

    /**
     * Tests that a key is able to be updated when it exists.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        updateKeyRunner((originalKeyOptions, updatedKeyOptions) -> {
            KeyVaultKey createdKey = keyClient.createKey(originalKeyOptions);

            assertKeyEquals(originalKeyOptions, createdKey);

            KeyVaultKey updatedKey =
                keyClient.updateKeyProperties(createdKey.getProperties().setExpiresOn(updatedKeyOptions.getExpiresOn()));

            assertKeyEquals(updatedKeyOptions, updatedKey);
        });
    }

    /**
     * Tests that a key can be updated when it is disabled.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateDisabledKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        updateDisabledKeyRunner((createKeyOptions, updateKeyOptions) -> {
            KeyVaultKey createdKey = keyClient.createKey(createKeyOptions);

            assertKeyEquals(createKeyOptions, createdKey);

            KeyVaultKey updatedKey =
                keyClient.updateKeyProperties(createdKey.getProperties().setExpiresOn(updateKeyOptions.getExpiresOn()));

            assertKeyEquals(updateKeyOptions, updatedKey);
        });
    }

    /**
     * Tests that an existing key can be retrieved.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        getKeyRunner((keyToSetAndGet) -> {
            keyClient.createKey(keyToSetAndGet);

            KeyVaultKey retrievedKey = keyClient.getKey(keyToSetAndGet.getName());

            assertKeyEquals(keyToSetAndGet, retrievedKey);
            assertEquals("0", retrievedKey.getProperties().getHsmPlatform());
        });
    }

    /**
     * Tests that a specific version of the key can be retrieved.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKeySpecificVersion(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        getKeySpecificVersionRunner((keyWithOriginalValue, keyWithNewValue) -> {
            KeyVaultKey keyVersionOne = keyClient.createKey(keyWithOriginalValue);
            KeyVaultKey keyVersionTwo = keyClient.createKey(keyWithNewValue);

            assertKeyEquals(keyWithOriginalValue,
                keyClient.getKey(keyVersionOne.getName(), keyVersionOne.getProperties().getVersion()));
            assertKeyEquals(keyWithNewValue,
                keyClient.getKey(keyVersionTwo.getName(), keyVersionTwo.getProperties().getVersion()));
        });
    }

    /**
     * Tests that an attempt to get a non-existing key throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        assertRestException(() -> keyClient.getKey("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that an existing key can be deleted.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        deleteKeyRunner((keyToDelete) -> {
            sleepIfRunningAgainstService(30000);

            assertKeyEquals(keyToDelete, keyClient.createKey(keyToDelete));

            SyncPoller<DeletedKey, Void> deletedKeyPoller = setPlaybackSyncPollerPollInterval(
                keyClient.beginDeleteKey(keyToDelete.getName()));

            DeletedKey deletedKey = deletedKeyPoller.waitForCompletion().getValue();

            assertNotNull(deletedKey.getDeletedOn());
            assertNotNull(deletedKey.getRecoveryId());
            assertNotNull(deletedKey.getScheduledPurgeDate());
            assertEquals(keyToDelete.getName(), deletedKey.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        assertRestException(() -> keyClient.beginDeleteKey("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that an attempt to retrieve a non-existing deleted key throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        assertRestException(() -> keyClient.getDeletedKey("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }


    /**
     * Tests that a deleted key can be recovered on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        recoverDeletedKeyRunner((keyToDeleteAndRecover) -> {
            assertKeyEquals(keyToDeleteAndRecover, keyClient.createKey(keyToDeleteAndRecover));

            SyncPoller<DeletedKey, Void> poller = setPlaybackSyncPollerPollInterval(
                keyClient.beginDeleteKey(keyToDeleteAndRecover.getName()));

            assertNotNull(poller.waitForCompletion());

            SyncPoller<KeyVaultKey, Void> recoverPoller = setPlaybackSyncPollerPollInterval(
                keyClient.beginRecoverDeletedKey(keyToDeleteAndRecover.getName()));

            KeyVaultKey recoveredKey = recoverPoller.waitForCompletion().getValue();

            assertEquals(keyToDeleteAndRecover.getName(), recoveredKey.getName());
            assertEquals(keyToDeleteAndRecover.getNotBefore(), recoveredKey.getProperties().getNotBefore());
            assertEquals(keyToDeleteAndRecover.getExpiresOn(), recoveredKey.getProperties().getExpiresOn());
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted key throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        assertRestException(() -> keyClient.beginRecoverDeletedKey("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        backupKeyRunner((keyToBackup) -> {
            assertKeyEquals(keyToBackup, keyClient.createKey(keyToBackup));

            byte[] backupBytes = (keyClient.backupKey(keyToBackup.getName()));

            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
        });
    }

    /**
     * Tests that an attempt to back up a non-existing key throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        assertRestException(() -> keyClient.backupKey("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        restoreKeyRunner((keyToBackupAndRestore) -> {
            assertKeyEquals(keyToBackupAndRestore, keyClient.createKey(keyToBackupAndRestore));

            byte[] backupBytes = (keyClient.backupKey(keyToBackupAndRestore.getName()));

            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);

            SyncPoller<DeletedKey, Void> poller = setPlaybackSyncPollerPollInterval(
                keyClient.beginDeleteKey(keyToBackupAndRestore.getName()));

            poller.waitForCompletion();

            keyClient.purgeDeletedKey(keyToBackupAndRestore.getName());

            pollOnKeyPurge(keyToBackupAndRestore.getName());
            sleepIfRunningAgainstService(60000);

            KeyVaultKey restoredKey = keyClient.restoreKeyBackup(backupBytes);

            assertEquals(keyToBackupAndRestore.getName(), restoredKey.getName());
            assertEquals(keyToBackupAndRestore.getExpiresOn(), restoredKey.getProperties().getExpiresOn());
        });
    }

    /**
     * Tests that an attempt to restore a key from malformed backup bytes throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreKeyFromMalformedBackup(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        byte[] keyBackupBytes = "non-existing".getBytes();

        assertRestException(() -> keyClient.restoreKeyBackup(keyBackupBytes),
            ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);
    }

    /**
     * Tests that keys can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listKeys(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        listKeysRunner((keysToList) -> {

            for (CreateKeyOptions key : keysToList.values()) {
                assertKeyEquals(key, keyClient.createKey(key));
            }

            sleepIfRunningAgainstService(5000);

            for (KeyProperties actualKey : keyClient.listPropertiesOfKeys()) {
                if (keysToList.containsKey(actualKey.getName())) {
                    CreateKeyOptions expectedKey = keysToList.get(actualKey.getName());

                    assertEquals(expectedKey.getExpiresOn(), actualKey.getExpiresOn());
                    assertEquals(expectedKey.getNotBefore(), actualKey.getNotBefore());

                    keysToList.remove(actualKey.getName());
                }
            }
            assertEquals(0, keysToList.size());
        });
    }

    /**
     * Tests that a deleted key can be retrieved on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        getDeletedKeyRunner((keyToDeleteAndGet) -> {
            assertKeyEquals(keyToDeleteAndGet, keyClient.createKey(keyToDeleteAndGet));

            SyncPoller<DeletedKey, Void> poller = setPlaybackSyncPollerPollInterval(
                keyClient.beginDeleteKey(keyToDeleteAndGet.getName()));

            poller.waitForCompletion();

            sleepIfRunningAgainstService(30000);

            DeletedKey deletedKey = keyClient.getDeletedKey(keyToDeleteAndGet.getName());

            assertNotNull(deletedKey.getDeletedOn());
            assertNotNull(deletedKey.getRecoveryId());
            assertNotNull(deletedKey.getScheduledPurgeDate());
            assertEquals(keyToDeleteAndGet.getName(), deletedKey.getName());
        });
    }

    /**
     * Tests that deleted keys can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedKeys(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        // Skip when running against the service to avoid having pipeline runs take longer than they have to.
        if (interceptorManager.isLiveMode()) {
            return;
        }

        listDeletedKeysRunner((keysToList) -> {
            for (CreateKeyOptions key : keysToList.values()) {
                assertKeyEquals(key, keyClient.createKey(key));
            }

            for (CreateKeyOptions key : keysToList.values()) {
                SyncPoller<DeletedKey, Void> poller = setPlaybackSyncPollerPollInterval(
                    keyClient.beginDeleteKey(key.getName()));

                poller.waitForCompletion();
            }

            sleepIfRunningAgainstService(90000);

            Iterable<DeletedKey> deletedKeys = keyClient.listDeletedKeys();

            for (DeletedKey deletedKey : deletedKeys) {
                assertNotNull(deletedKey.getDeletedOn());
                assertNotNull(deletedKey.getRecoveryId());
            }
        });
    }

    /**
     * Tests that key versions can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listKeyVersions(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        listKeyVersionsRunner((keysToList) -> {
            String keyName = null;

            for (CreateKeyOptions key : keysToList) {
                keyName = key.getName();

                sleepIfRunningAgainstService(4000);

                assertKeyEquals(key, keyClient.createKey(key));
            }

            Iterable<KeyProperties> keyVersionsOutput = keyClient.listPropertiesOfKeyVersions(keyName);
            List<KeyProperties> keyVersionsList = new ArrayList<>();

            keyVersionsOutput.forEach(keyVersionsList::add);

            assertEquals(keysToList.size(), keyVersionsList.size());
        });
    }

    /**
     * Tests that an existing key can be released.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void releaseKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        // TODO: Remove assumption once Key Vault allows for creating exportable keys.
        Assumptions.assumeTrue(runManagedHsmTest && runReleaseKeyTest);

        createKeyClient(httpClient, serviceVersion);

        releaseKeyRunner((keyToRelease, attestationUrl) -> {
            assertKeyEquals(keyToRelease, keyClient.createRsaKey(keyToRelease));

            String targetAttestationToken = "testAttestationToken";

            if (getTestMode() != TestMode.PLAYBACK) {
                if (!attestationUrl.endsWith("/")) {
                    attestationUrl = attestationUrl + "/";
                }

                targetAttestationToken = getAttestationToken(attestationUrl + "generate-test-token");
            }

            ReleaseKeyResult releaseKeyResult = keyClient.releaseKey(keyToRelease.getName(), targetAttestationToken);

            assertNotNull(releaseKeyResult.getValue());
        });
    }

    /**
     * Tests that fetching the key rotation policy of a non-existent key throws.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @DisabledIfSystemProperty(named = "IS_SKIP_ROTATION_POLICY_TEST", matches = "true")
    public void getKeyRotationPolicyOfNonExistentKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        // Key Rotation is not yet enabled in Managed HSM.
        Assumptions.assumeTrue(!isHsmEnabled);

        createKeyClient(httpClient, serviceVersion);

        String keyName = testResourceNamer.randomName("nonExistentKey", 20);

        assertThrows(ResourceNotFoundException.class, () -> keyClient.getKeyRotationPolicy(keyName));
    }

    /**
     * Tests that fetching the key rotation policy of a non-existent key throws.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @DisabledIfSystemProperty(named = "IS_SKIP_ROTATION_POLICY_TEST", matches = "true")
    public void getKeyRotationPolicyWithNoPolicySet(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        // Key Rotation is not yet enabled in Managed HSM.
        Assumptions.assumeTrue(!isHsmEnabled);

        createKeyClient(httpClient, serviceVersion);

        String keyName = testResourceNamer.randomName("rotateKey", 20);

        keyClient.createRsaKey(new CreateRsaKeyOptions(keyName));

        KeyRotationPolicy keyRotationPolicy = keyClient.getKeyRotationPolicy(keyName);

        assertNotNull(keyRotationPolicy);
        assertNull(keyRotationPolicy.getId());
        assertNull(keyRotationPolicy.getCreatedOn());
        assertNull(keyRotationPolicy.getUpdatedOn());
        assertNull(keyRotationPolicy.getExpiresIn());
        assertEquals(1, keyRotationPolicy.getLifetimeActions().size());
        assertEquals(KeyRotationPolicyAction.NOTIFY, keyRotationPolicy.getLifetimeActions().get(0).getAction());
        assertEquals("P30D", keyRotationPolicy.getLifetimeActions().get(0).getTimeBeforeExpiry());
        assertNull(keyRotationPolicy.getLifetimeActions().get(0).getTimeAfterCreate());
    }

    /**
     * Tests that fetching the key rotation policy of a non-existent key throws.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @Disabled("Disable after https://github.com/Azure/azure-sdk-for-java/issues/31510 is fixed.")
    //@DisabledIfSystemProperty(named = "IS_SKIP_ROTATION_POLICY_TEST", matches = "true")
    public void updateGetKeyRotationPolicyWithMinimumProperties(HttpClient httpClient,
                                                                KeyServiceVersion serviceVersion) {
        // Key Rotation is not yet enabled in Managed HSM.
        Assumptions.assumeTrue(!isHsmEnabled);

        createKeyClient(httpClient, serviceVersion);

        updateGetKeyRotationPolicyWithMinimumPropertiesRunner((keyName, keyRotationPolicy) -> {
            keyClient.createRsaKey(new CreateRsaKeyOptions(keyName));

            KeyRotationPolicy updatedKeyRotationPolicy =
                keyClient.updateKeyRotationPolicy(keyName, keyRotationPolicy);
            KeyRotationPolicy retrievedKeyRotationPolicy = keyClient.getKeyRotationPolicy(keyName);

            assertKeyVaultRotationPolicyEquals(updatedKeyRotationPolicy, retrievedKeyRotationPolicy);
        });
    }

    /**
     * Tests that an key rotation policy can be updated with all possible properties, then retrieves it.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @DisabledIfSystemProperty(named = "IS_SKIP_ROTATION_POLICY_TEST", matches = "true")
    public void updateGetKeyRotationPolicyWithAllProperties(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        // Key Rotation is not yet enabled in Managed HSM.
        Assumptions.assumeTrue(!isHsmEnabled);

        createKeyClient(httpClient, serviceVersion);

        updateGetKeyRotationPolicyWithAllPropertiesRunner((keyName, keyRotationPolicy) -> {
            keyClient.createRsaKey(new CreateRsaKeyOptions(keyName));

            KeyRotationPolicy updatedKeyRotationPolicy =
                keyClient.updateKeyRotationPolicy(keyName, keyRotationPolicy);
            KeyRotationPolicy retrievedKeyRotationPolicy = keyClient.getKeyRotationPolicy(keyName);

            assertKeyVaultRotationPolicyEquals(updatedKeyRotationPolicy, retrievedKeyRotationPolicy);
        });
    }

    /**
     * Tests that a key can be rotated.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @DisabledIfSystemProperty(named = "IS_SKIP_ROTATION_POLICY_TEST", matches = "true")
    public void rotateKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        // Key Rotation is not yet enabled in Managed HSM.
        Assumptions.assumeTrue(!isHsmEnabled);

        createKeyClient(httpClient, serviceVersion);

        String keyName = testResourceNamer.randomName("rotateKey", 20);
        KeyVaultKey createdKey = keyClient.createRsaKey(new CreateRsaKeyOptions(keyName));
        KeyVaultKey rotatedKey = keyClient.rotateKey(keyName);

        assertEquals(createdKey.getName(), rotatedKey.getName());
        assertEquals(createdKey.getProperties().getTags(), rotatedKey.getProperties().getTags());
    }

    /**
     * Tests that a {@link CryptographyClient} can be created for a given key and version using a {@link KeyClient}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCryptographyClient(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        CryptographyClient cryptographyClient = keyClient.getCryptographyClient("myKey");

        assertNotNull(cryptographyClient);
    }

    /**
     * Tests that a {@link CryptographyClient} can be created for a given key using a {@link KeyClient}. Also tests
     * that cryptographic operations can be performed with said cryptography client.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCryptographyClientAndEncryptDecrypt(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        createKeyRunner((keyToCreate) -> {
            assertKeyEquals(keyToCreate, keyClient.createKey(keyToCreate));

            CryptographyClient cryptographyClient = keyClient.getCryptographyClient(keyToCreate.getName());

            assertNotNull(cryptographyClient);

            byte[] plaintext = "myPlaintext".getBytes();
            byte[] ciphertext = cryptographyClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext).getCipherText();
            byte[] decryptedText = cryptographyClient.decrypt(EncryptionAlgorithm.RSA_OAEP, ciphertext).getPlainText();

            assertArrayEquals(plaintext, decryptedText);
        });
    }

    /**
     * Tests that a {@link CryptographyClient} can be created for a given key and version using a {@link KeyClient}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCryptographyClientWithKeyVersion(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        CryptographyClient cryptographyClient =
            keyClient.getCryptographyClient("myKey", "6A385B124DEF4096AF1361A85B16C204");

        assertNotNull(cryptographyClient);
    }

    /**
     * Tests that a {@link CryptographyClient} can be created for a given key using a {@link KeyClient}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCryptographyClientWithEmptyKeyVersion(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        CryptographyClient cryptographyClient = keyClient.getCryptographyClient("myKey", "");

        assertNotNull(cryptographyClient);
    }

    /**
     * Tests that a {@link CryptographyClient} can be created for a given key using a {@link KeyClient}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCryptographyClientWithNullKeyVersion(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        CryptographyClient cryptographyClient = keyClient.getCryptographyClient("myKey", null);

        assertNotNull(cryptographyClient);
    }

    private void pollOnKeyPurge(String keyName) {
        int pendingPollCount = 0;

        while (pendingPollCount < 10) {
            DeletedKey deletedKey = null;

            try {
                deletedKey = keyClient.getDeletedKey(keyName);
            } catch (ResourceNotFoundException ignored) {
            }

            if (deletedKey != null) {
                sleepIfRunningAgainstService(2000);

                pendingPollCount += 1;
            } else {
                return;
            }
        }

        System.err.printf("Deleted Key %s was not purged \n", keyName);
    }
}
