// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyRotationPolicyAction;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.azure.security.keyvault.keys.cryptography.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class KeyAsyncClientTest extends KeyClientTestBase {
    protected KeyAsyncClient keyAsyncClient;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    protected void createKeyAsyncClient(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion, null);
    }

    protected void createKeyAsyncClient(HttpClient httpClient, KeyServiceVersion serviceVersion, String testTenantId) {
        HttpPipeline httpPipeline = getHttpPipeline(httpClient, testTenantId);
        keyAsyncClient = spy(new KeyClientBuilder()
            .vaultUrl(getEndpoint())
            .pipeline(httpPipeline)
            .serviceVersion(serviceVersion)
            .buildAsyncClient());

        if (interceptorManager.isPlaybackMode()) {
            when(keyAsyncClient.getDefaultPollingInterval()).thenReturn(Duration.ofMillis(10));
        }
    }

    /**
     * Tests that a key can be created in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        createKeyRunner((keyToCreate) ->
            StepVerifier.create(keyAsyncClient.createKey(keyToCreate))
                .assertNext(response -> assertKeyEquals(keyToCreate, response))
                .verifyComplete());
    }

    /**
     * Tests that a key can be created in the key vault while using a different tenant ID than the one that will be
     * provided in the authentication challenge.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createKeyWithMultipleTenants(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion, testResourceNamer.randomUuid());

        createKeyRunner((keyToCreate) ->
            StepVerifier.create(keyAsyncClient.createKey(keyToCreate))
                .assertNext(response -> assertKeyEquals(keyToCreate, response))
                .verifyComplete());

        KeyVaultCredentialPolicy.clearCache(); // Ensure we don't have anything cached and try again.

        createKeyRunner((keyToCreate) ->
            StepVerifier.create(keyAsyncClient.createKey(keyToCreate))
                .assertNext(response -> assertKeyEquals(keyToCreate, response))
                .verifyComplete());
    }

    /**
     * Tests that a RSA key created.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createRsaKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        createRsaKeyRunner((keyToCreate) ->
            StepVerifier.create(keyAsyncClient.createRsaKey(keyToCreate))
                .assertNext(response -> assertKeyEquals(keyToCreate, response))
                .verifyComplete());
    }

    /**
     * Tests that we cannot create a key when the key is an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createKeyEmptyName(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        final KeyType keyType;

        if (runManagedHsmTest) {
            keyType = KeyType.RSA_HSM;
        } else {
            keyType = KeyType.RSA;
        }

        StepVerifier.create(keyAsyncClient.createKey("", keyType))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST));
    }

    /**
     * Tests that we can create keys when value is not null or an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createKeyNullType(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        createKeyEmptyValueRunner((keyToCreate) ->
            StepVerifier.create(keyAsyncClient.createKey(keyToCreate))
                .verifyErrorSatisfies(e ->
                    assertRestException(e, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST)));
    }

    /**
     * Verifies that an exception is thrown when null key object is passed for creation.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createKeyNull(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(keyAsyncClient.createKey(null))
            .verifyError(NullPointerException.class);
    }

    /**
     * Tests that a key is able to be updated when it exists.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        updateKeyRunner((originalKey, updatedKey) -> StepVerifier.create(keyAsyncClient.createKey(originalKey)
                .flatMap(response -> {
                    assertKeyEquals(originalKey, response);

                    return keyAsyncClient.updateKeyProperties(response.getProperties()
                        .setExpiresOn(updatedKey.getExpiresOn()));
                }))
            .assertNext(response -> assertKeyEquals(updatedKey, response))
            .verifyComplete());
    }

    /**
     * Tests that a key is not able to be updated when it is disabled. 403 error is expected.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateDisabledKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        updateDisabledKeyRunner((originalKey, updatedKey) -> StepVerifier.create(keyAsyncClient.createKey(originalKey)
                .flatMap(response -> {
                    assertKeyEquals(originalKey, response);

                    return keyAsyncClient.updateKeyProperties(response.getProperties()
                        .setExpiresOn(updatedKey.getExpiresOn()));
                }))
            .assertNext(response -> assertKeyEquals(updatedKey, response))
            .verifyComplete());
    }


    /**
     * Tests that an existing key can be retrieved.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        getKeyRunner((keyToSetAndGet) -> {
            StepVerifier.create(keyAsyncClient.createKey(keyToSetAndGet))
                .assertNext(response -> assertKeyEquals(keyToSetAndGet, response))
                .verifyComplete();

            StepVerifier.create(keyAsyncClient.getKey(keyToSetAndGet.getName()))
                .assertNext(response -> assertKeyEquals(keyToSetAndGet, response))
                .verifyComplete();
        });
    }

    /**
     * Tests that a specific version of the key can be retrieved.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKeySpecificVersion(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        getKeySpecificVersionRunner((keyWithOriginalValue, keyWithNewValue) -> {
            final KeyVaultKey keyVersionOne = keyAsyncClient.createKey(keyWithOriginalValue).block();
            final KeyVaultKey keyVersionTwo = keyAsyncClient.createKey(keyWithNewValue).block();

            StepVerifier.create(keyAsyncClient.getKey(keyWithOriginalValue.getName(),
                    keyVersionOne.getProperties().getVersion()))
                .assertNext(response -> assertKeyEquals(keyWithOriginalValue, response))
                .verifyComplete();

            StepVerifier.create(keyAsyncClient.getKey(keyWithNewValue.getName(),
                    keyVersionTwo.getProperties().getVersion()))
                .assertNext(response -> assertKeyEquals(keyWithNewValue, response))
                .verifyComplete();
        });
    }

    /**
     * Tests that an attempt to get a non-existing key throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(keyAsyncClient.getKey("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }


    /**
     * Tests that an existing key can be deleted.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        deleteKeyRunner((keyToDelete) -> {
            StepVerifier.create(keyAsyncClient.createKey(keyToDelete))
                .assertNext(keyResponse -> assertKeyEquals(keyToDelete, keyResponse)).verifyComplete();

            PollerFlux<DeletedKey, Void> poller = keyAsyncClient.beginDeleteKey(keyToDelete.getName());
            AsyncPollResponse<DeletedKey, Void> deletedKeyPollResponse = poller
                .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                .blockLast();
            DeletedKey deletedKeyResponse = deletedKeyPollResponse.getValue();

            assertNotNull(deletedKeyResponse.getDeletedOn());
            assertNotNull(deletedKeyResponse.getRecoveryId());
            assertNotNull(deletedKeyResponse.getScheduledPurgeDate());
            assertEquals(keyToDelete.getName(), deletedKeyResponse.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(keyAsyncClient.beginDeleteKey("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that an attempt to retrieve a non existing deleted key throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(keyAsyncClient.getDeletedKey("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a deleted key can be recovered on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        recoverDeletedKeyRunner((keyToDeleteAndRecover) -> {
            StepVerifier.create(keyAsyncClient.createKey(keyToDeleteAndRecover))
                .assertNext(keyResponse -> assertKeyEquals(keyToDeleteAndRecover, keyResponse)).verifyComplete();

            PollerFlux<DeletedKey, Void> poller = keyAsyncClient.beginDeleteKey(keyToDeleteAndRecover.getName());
            AsyncPollResponse<DeletedKey, Void> deleteKeyPollResponse =
                poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            assertNotNull(deleteKeyPollResponse.getValue());

            PollerFlux<KeyVaultKey, Void> recoverPoller =
                keyAsyncClient.beginRecoverDeletedKey(keyToDeleteAndRecover.getName());
            AsyncPollResponse<KeyVaultKey, Void> recoverKeyPollResponse =
                recoverPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();
            KeyVaultKey keyResponse = recoverKeyPollResponse.getValue();

            assertEquals(keyToDeleteAndRecover.getName(), keyResponse.getName());
            assertEquals(keyToDeleteAndRecover.getNotBefore(), keyResponse.getProperties().getNotBefore());
            assertEquals(keyToDeleteAndRecover.getExpiresOn(), keyResponse.getProperties().getExpiresOn());
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted key throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(keyAsyncClient.beginRecoverDeletedKey("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        backupKeyRunner((keyToBackup) -> {
            StepVerifier.create(keyAsyncClient.createKey(keyToBackup))
                .assertNext(keyResponse -> assertKeyEquals(keyToBackup, keyResponse)).verifyComplete();

            StepVerifier.create(keyAsyncClient.backupKey(keyToBackup.getName()))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertTrue(response.length > 0);
                }).verifyComplete();
        });
    }

    /**
     * Tests that an attempt to backup a non existing key throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(keyAsyncClient.backupKey("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        restoreKeyRunner((keyToBackupAndRestore) -> {
            StepVerifier.create(keyAsyncClient.createKey(keyToBackupAndRestore))
                .assertNext(keyResponse -> assertKeyEquals(keyToBackupAndRestore, keyResponse))
                .verifyComplete();

            byte[] backup = keyAsyncClient.backupKey(keyToBackupAndRestore.getName()).block();

            PollerFlux<DeletedKey, Void> poller = keyAsyncClient.beginDeleteKey(keyToBackupAndRestore.getName());
            AsyncPollResponse<DeletedKey, Void> pollResponse =
                poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            assertNotNull(pollResponse.getValue());

            StepVerifier.create(keyAsyncClient.purgeDeletedKeyWithResponse(keyToBackupAndRestore.getName()))
                .assertNext(voidResponse ->
                    assertEquals(HttpURLConnection.HTTP_NO_CONTENT, voidResponse.getStatusCode()))
                .verifyComplete();

            pollOnKeyPurge(keyToBackupAndRestore.getName());

            sleepInRecordMode(60000);

            StepVerifier.create(keyAsyncClient.restoreKeyBackup(backup))
                .assertNext(response -> {
                    assertEquals(keyToBackupAndRestore.getName(), response.getName());
                    assertEquals(keyToBackupAndRestore.getNotBefore(), response.getProperties().getNotBefore());
                    assertEquals(keyToBackupAndRestore.getExpiresOn(), response.getProperties().getExpiresOn());
                }).verifyComplete();
        });
    }

    /**
     * Tests that an attempt to restore a key from malformed backup bytes throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreKeyFromMalformedBackup(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        byte[] keyBackupBytes = "non-existing".getBytes();

        StepVerifier.create(keyAsyncClient.restoreKeyBackup(keyBackupBytes))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST));
    }

    /**
     * Tests that a deleted key can be retrieved on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        getDeletedKeyRunner((keyToDeleteAndGet) -> {
            StepVerifier.create(keyAsyncClient.createKey(keyToDeleteAndGet))
                .assertNext(keyResponse -> assertKeyEquals(keyToDeleteAndGet, keyResponse))
                .verifyComplete();

            PollerFlux<DeletedKey, Void> poller = keyAsyncClient.beginDeleteKey(keyToDeleteAndGet.getName());
            AsyncPollResponse<DeletedKey, Void> pollResponse =
                poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            assertNotNull(pollResponse.getValue());

            StepVerifier.create(keyAsyncClient.getDeletedKey(keyToDeleteAndGet.getName()))
                .assertNext(deletedKeyResponse -> {
                    assertNotNull(deletedKeyResponse.getDeletedOn());
                    assertNotNull(deletedKeyResponse.getRecoveryId());
                    assertNotNull(deletedKeyResponse.getScheduledPurgeDate());
                    assertEquals(keyToDeleteAndGet.getName(), deletedKeyResponse.getName());
                }).verifyComplete();
        });
    }

    /**
     * Tests that deleted keys can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedKeys(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        // Skip when running against the service to avoid having pipeline runs take longer than they have to.
        if (interceptorManager.isLiveMode()) {
            return;
        }

        listDeletedKeysRunner((keysToList) -> {
            List<DeletedKey> deletedKeys = new ArrayList<>();

            for (CreateKeyOptions key : keysToList.values()) {
                StepVerifier.create(keyAsyncClient.createKey(key))
                    .assertNext(keyResponse -> assertKeyEquals(key, keyResponse)).verifyComplete();
            }

            sleepInRecordMode(10000);

            for (CreateKeyOptions key : keysToList.values()) {
                PollerFlux<DeletedKey, Void> poller = keyAsyncClient.beginDeleteKey(key.getName());
                AsyncPollResponse<DeletedKey, Void> response = poller.blockLast();

                assertNotNull(response.getValue());
            }

            sleepInRecordMode(90000);

            DeletedKey deletedKey = keyAsyncClient.listDeletedKeys()
                .map(actualKey -> {
                    deletedKeys.add(actualKey);
                    assertNotNull(actualKey.getDeletedOn());
                    assertNotNull(actualKey.getRecoveryId());

                    return actualKey;
                }).blockLast();

            assertNotNull(deletedKey);
        });
    }

    /**
     * Tests that key versions can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listKeyVersions(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        listKeyVersionsRunner((keysToList) -> {
            List<KeyProperties> output = new ArrayList<>();
            String keyName = null;

            for (CreateKeyOptions key : keysToList) {
                keyName = key.getName();

                StepVerifier.create(keyAsyncClient.createKey(key))
                    .assertNext(keyResponse -> assertKeyEquals(key, keyResponse))
                    .verifyComplete();
            }

            sleepInRecordMode(30000);

            keyAsyncClient.listPropertiesOfKeyVersions(keyName).subscribe(output::add);

            sleepInRecordMode(30000);

            assertEquals(keysToList.size(), output.size());
        });

    }

    /**
     * Tests that keys can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listKeys(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        listKeysRunner((keysToList) -> {
            for (CreateKeyOptions key : keysToList.values()) {
                assertKeyEquals(key, keyAsyncClient.createKey(key).block());
            }

            sleepInRecordMode(10000);

            keyAsyncClient.listPropertiesOfKeys().map(actualKey -> {
                if (keysToList.containsKey(actualKey.getName())) {
                    CreateKeyOptions expectedKey = keysToList.get(actualKey.getName());
                    assertEquals(expectedKey.getExpiresOn(), actualKey.getExpiresOn());
                    assertEquals(expectedKey.getNotBefore(), actualKey.getNotBefore());
                    keysToList.remove(actualKey.getName());
                }

                return actualKey;
            }).blockLast();

            assertEquals(0, keysToList.size());
        });
    }

    /**
     * Tests that an existing key can be released.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void releaseKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        // TODO: Remove assumption once Key Vault allows for creating exportable keys.
        Assumptions.assumeTrue(runManagedHsmTest);

        createKeyAsyncClient(httpClient, serviceVersion);

        releaseKeyRunner((keyToRelease, attestationUrl) -> {
            StepVerifier.create(keyAsyncClient.createRsaKey(keyToRelease))
                .assertNext(keyResponse -> assertKeyEquals(keyToRelease, keyResponse))
                .verifyComplete();

            String targetAttestationToken = "testAttestationToken";

            if (getTestMode() != TestMode.PLAYBACK) {
                if (!attestationUrl.endsWith("/")) {
                    attestationUrl = attestationUrl + "/";
                }

                try {
                    targetAttestationToken = getAttestationToken(attestationUrl + "generate-test-token");
                } catch (IOException e) {
                    fail("Found error when deserializing attestation token.", e);
                }
            }

            StepVerifier.create(keyAsyncClient.releaseKey(keyToRelease.getName(), targetAttestationToken))
                .assertNext(releaseKeyResult -> assertNotNull(releaseKeyResult.getValue()))
                .expectComplete()
                .verify();
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

        createKeyAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(keyAsyncClient.getKeyRotationPolicy(testResourceNamer.randomName("nonExistentKey", 20)))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
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

        createKeyAsyncClient(httpClient, serviceVersion);

        String keyName = testResourceNamer.randomName("rotateKey", 20);

        StepVerifier.create(keyAsyncClient.createRsaKey(new CreateRsaKeyOptions(keyName)))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        StepVerifier.create(keyAsyncClient.getKeyRotationPolicy(keyName))
            .assertNext(keyRotationPolicy -> {
                assertNotNull(keyRotationPolicy);
                assertNull(keyRotationPolicy.getId());
                assertNull(keyRotationPolicy.getCreatedOn());
                assertNull(keyRotationPolicy.getUpdatedOn());
                assertNull(keyRotationPolicy.getExpiresIn());
                assertEquals(1, keyRotationPolicy.getLifetimeActions().size());
                assertEquals(KeyRotationPolicyAction.NOTIFY, keyRotationPolicy.getLifetimeActions().get(0).getAction());
                assertEquals("P30D", keyRotationPolicy.getLifetimeActions().get(0).getTimeBeforeExpiry());
                assertNull(keyRotationPolicy.getLifetimeActions().get(0).getTimeAfterCreate());
            }).verifyComplete();
    }

    /**
     * Tests that fetching the key rotation policy of a non-existent key throws.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @DisabledIfSystemProperty(named = "IS_SKIP_ROTATION_POLICY_TEST", matches = "true")
    public void updateGetKeyRotationPolicyWithMinimumProperties(HttpClient httpClient,
                                                                KeyServiceVersion serviceVersion) {
        // Key Rotation is not yet enabled in Managed HSM.
        Assumptions.assumeTrue(!isHsmEnabled);

        createKeyAsyncClient(httpClient, serviceVersion);

        updateGetKeyRotationPolicyWithMinimumPropertiesRunner((keyName, keyRotationPolicy) -> {
            StepVerifier.create(keyAsyncClient.createRsaKey(new CreateRsaKeyOptions(keyName)))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();

            StepVerifier.create(keyAsyncClient.updateKeyRotationPolicy(keyName, keyRotationPolicy)
                    .flatMap(updatedKeyRotationPolicy -> Mono.zip(Mono.just(updatedKeyRotationPolicy),
                        keyAsyncClient.getKeyRotationPolicy(keyName))))
                .assertNext(tuple -> assertKeyVaultRotationPolicyEquals(tuple.getT1(), tuple.getT2()))
                .verifyComplete();
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

        createKeyAsyncClient(httpClient, serviceVersion);

        updateGetKeyRotationPolicyWithAllPropertiesRunner((keyName, keyRotationPolicy) -> {
            StepVerifier.create(keyAsyncClient.createRsaKey(new CreateRsaKeyOptions(keyName)))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();

            StepVerifier.create(keyAsyncClient.updateKeyRotationPolicy(keyName, keyRotationPolicy)
                    .flatMap(updatedKeyRotationPolicy -> Mono.zip(Mono.just(updatedKeyRotationPolicy),
                        keyAsyncClient.getKeyRotationPolicy(keyName))))
                .assertNext(tuple -> assertKeyVaultRotationPolicyEquals(tuple.getT1(), tuple.getT2()))
                .verifyComplete();
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

        createKeyAsyncClient(httpClient, serviceVersion);

        String keyName = testResourceNamer.randomName("rotateKey", 20);

        StepVerifier.create(keyAsyncClient.createRsaKey(new CreateRsaKeyOptions(keyName))
                .flatMap(createdKey -> Mono.zip(Mono.just(createdKey),
                    keyAsyncClient.rotateKey(keyName))))
            .assertNext(tuple -> {
                KeyVaultKey createdKey = tuple.getT1();
                KeyVaultKey rotatedKey = tuple.getT2();

                assertEquals(createdKey.getName(), rotatedKey.getName());
                assertEquals(createdKey.getProperties().getTags(), rotatedKey.getProperties().getTags());
            }).verifyComplete();
    }

    /**
     * Tests that a {@link CryptographyAsyncClient} can be created for a given key using a {@link KeyAsyncClient}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCryptographyAsyncClient(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        CryptographyAsyncClient cryptographyAsyncClient = keyAsyncClient.getCryptographyAsyncClient("myKey");

        assertNotNull(cryptographyAsyncClient);
    }

    /**
     * Tests that a {@link CryptographyClient} can be created for a given key using a {@link KeyClient}. Also tests
     * that cryptographic operations can be performed with said cryptography client.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCryptographyAsyncClientAndEncryptDecrypt(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        createKeyRunner((keyToCreate) -> {
            StepVerifier.create(keyAsyncClient.createKey(keyToCreate))
                .assertNext(response -> assertKeyEquals(keyToCreate, response))
                .verifyComplete();

            CryptographyAsyncClient cryptographyAsyncClient =
                keyAsyncClient.getCryptographyAsyncClient(keyToCreate.getName());

            assertNotNull(cryptographyAsyncClient);

            byte[] plaintext = "myPlaintext".getBytes();

            StepVerifier.create(cryptographyAsyncClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext)
                    .map(EncryptResult::getCipherText)
                    .flatMap(ciphertext -> cryptographyAsyncClient.decrypt(EncryptionAlgorithm.RSA_OAEP, ciphertext)
                        .map(DecryptResult::getPlainText)))
                .assertNext(decryptedText -> assertArrayEquals(plaintext, decryptedText))
                .verifyComplete();
        });
    }

    /**
     * Tests that a {@link CryptographyAsyncClient} can be created for a given key and version using a
     * {@link KeyAsyncClient}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCryptographyAsyncClientWithKeyVersion(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        CryptographyAsyncClient cryptographyAsyncClient =
            keyAsyncClient.getCryptographyAsyncClient("myKey", "6A385B124DEF4096AF1361A85B16C204");

        assertNotNull(cryptographyAsyncClient);
    }

    /**
     * Tests that a {@link CryptographyAsyncClient} can be created for a given key using a {@link KeyAsyncClient}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCryptographyAsyncClientWithEmptyKeyVersion(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        CryptographyAsyncClient cryptographyAsyncClient = keyAsyncClient.getCryptographyAsyncClient("myKey", "");

        assertNotNull(cryptographyAsyncClient);
    }

    /**
     * Tests that a {@link CryptographyAsyncClient} can be created for a given key using a {@link KeyAsyncClient}.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCryptographyAsyncClientWithNullKeyVersion(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        CryptographyAsyncClient cryptographyAsyncClient = keyAsyncClient.getCryptographyAsyncClient("myKey", null);

        assertNotNull(cryptographyAsyncClient);
    }

    private void pollOnKeyPurge(String keyName) {
        int pendingPollCount = 0;

        while (pendingPollCount < 10) {
            DeletedKey deletedKey = null;

            try {
                deletedKey = keyAsyncClient.getDeletedKey(keyName).block();
            } catch (ResourceNotFoundException ignored) {
            }

            if (deletedKey != null) {
                sleepInRecordMode(2000);

                pendingPollCount += 1;
            } else {
                return;
            }
        }

        System.err.printf("Deleted Key %s was not purged \n", keyName);
    }
}

