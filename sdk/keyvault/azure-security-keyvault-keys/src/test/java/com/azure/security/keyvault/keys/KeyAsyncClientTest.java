// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.azure.security.keyvault.keys.cryptography.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class KeyAsyncClientTest extends KeyClientTestBase {
    protected KeyAsyncClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    protected void createKeyAsyncClient(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        HttpPipeline httpPipeline = getHttpPipeline(httpClient, serviceVersion);
        client = spy(new KeyClientBuilder()
            .vaultUrl(getEndpoint())
            .pipeline(httpPipeline)
            .serviceVersion(serviceVersion)
            .buildAsyncClient());

        if (interceptorManager.isPlaybackMode()) {
            when(client.getDefaultPollingInterval()).thenReturn(Duration.ofMillis(10));
        }
    }

    /**
     * Tests that a key can be created in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        setKeyRunner((expected) -> StepVerifier.create(client.createKey(expected))
            .assertNext(response -> assertKeyEquals(expected, response))
            .verifyComplete());
    }

    /**
     * Tests that a RSA key created.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createRsaKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        createRsaKeyRunner((expected) -> StepVerifier.create(client.createRsaKey(expected))
            .assertNext(response -> assertKeyEquals(expected, response))
            .verifyComplete());
    }

    /**
     * Tests that we cannot create a key when the key is an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setKeyEmptyName(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        if (isManagedHsmTest && interceptorManager.isPlaybackMode()) {
            // Setting a key with an empty name returns 500 in MHSM, we don't currently produce a recording for that the
            // way things are set.
            return;
        }

        createKeyAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.createKey("", KeyType.RSA))
            .verifyErrorSatisfies(ex -> {
                if (isManagedHsmTest) {
                    assertRestException(ex, HttpResponseException.class, HttpURLConnection.HTTP_SERVER_ERROR);
                } else {
                    assertRestException(ex, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);
                }
            });
    }

    /**
     * Tests that we can create keys when value is not null or an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setKeyNullType(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        setKeyEmptyValueRunner((key) -> StepVerifier.create(client.createKey(key))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST)));
    }

    /**
     * Verifies that an exception is thrown when null key object is passed for creation.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setKeyNull(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.createKey(null))
            .verifyError(NullPointerException.class);
    }

    /**
     * Tests that a key is able to be updated when it exists.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        updateKeyRunner((original, updated) -> {
            StepVerifier.create(client.createKey(original))
                .assertNext(response -> assertKeyEquals(original, response))
                .verifyComplete();

            KeyVaultKey keyToUpdate = client.getKey(original.getName()).block();

            StepVerifier.create(client.updateKeyProperties(keyToUpdate.getProperties().setExpiresOn(updated.getExpiresOn())))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(original.getName(), response.getName());
                }).verifyComplete();

            StepVerifier.create(client.getKey(original.getName()))
                .assertNext(updatedKeyResponse -> assertKeyEquals(updated, updatedKeyResponse))
                .verifyComplete();
        });
    }

    /**
     * Tests that a key is not able to be updated when it is disabled. 403 error is expected.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateDisabledKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        updateDisabledKeyRunner((original, updated) -> {
            StepVerifier.create(client.createKey(original))
                .assertNext(response -> assertKeyEquals(original, response))
                .verifyComplete();
            KeyVaultKey keyToUpdate = client.getKey(original.getName()).block();

            StepVerifier.create(client.updateKeyProperties(keyToUpdate.getProperties().setExpiresOn(updated.getExpiresOn())))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(original.getName(), response.getName());
                }).verifyComplete();

            StepVerifier.create(client.getKey(original.getName()))
                .assertNext(updatedKeyResponse -> assertKeyEquals(updated, updatedKeyResponse))
                .verifyComplete();
        });
    }


    /**
     * Tests that an existing key can be retrieved.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        getKeyRunner((original) -> {
            StepVerifier.create(client.createKey(original))
                .assertNext(response -> assertKeyEquals(original, response))
                .verifyComplete();

            StepVerifier.create(client.getKey(original.getName()))
                .assertNext(response -> assertKeyEquals(original, response))
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
        getKeySpecificVersionRunner((key, keyWithNewVal) -> {
            final KeyVaultKey keyVersionOne = client.createKey(key).block();
            final KeyVaultKey keyVersionTwo = client.createKey(keyWithNewVal).block();

            StepVerifier.create(client.getKey(key.getName(), keyVersionOne.getProperties().getVersion()))
                .assertNext(response -> assertKeyEquals(key, response))
                .verifyComplete();

            StepVerifier.create(client.getKey(keyWithNewVal.getName(), keyVersionTwo.getProperties().getVersion()))
                .assertNext(response -> assertKeyEquals(keyWithNewVal, response))
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
        StepVerifier.create(client.getKey("non-existing"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }


    /**
     * Tests that an existing key can be deleted.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        deleteKeyRunner((keyToDelete) -> {
            StepVerifier.create(client.createKey(keyToDelete))
                .assertNext(keyResponse -> assertKeyEquals(keyToDelete, keyResponse)).verifyComplete();

            PollerFlux<DeletedKey, Void> poller = client.beginDeleteKey(keyToDelete.getName());
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
        StepVerifier.create(client.beginDeleteKey("non-existing"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that an attempt to retrieve a non existing deleted key throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDeletedKey("non-existing"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a deleted key can be recovered on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        recoverDeletedKeyRunner((keyToDeleteAndRecover) -> {
            StepVerifier.create(client.createKey(keyToDeleteAndRecover))
                .assertNext(keyResponse -> assertKeyEquals(keyToDeleteAndRecover, keyResponse)).verifyComplete();

            PollerFlux<DeletedKey, Void> poller = client.beginDeleteKey(keyToDeleteAndRecover.getName());
            AsyncPollResponse<DeletedKey, Void> deleteKeyPollResponse
                = poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                .blockLast();

            assertNotNull(deleteKeyPollResponse.getValue());

            PollerFlux<KeyVaultKey, Void> recoverPoller = client.beginRecoverDeletedKey(keyToDeleteAndRecover.getName());

            AsyncPollResponse<KeyVaultKey, Void> recoverKeyPollResponse
                = recoverPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
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
        StepVerifier.create(client.beginRecoverDeletedKey("non-existing"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        backupKeyRunner((keyToBackup) -> {
            StepVerifier.create(client.createKey(keyToBackup))
                .assertNext(keyResponse -> assertKeyEquals(keyToBackup, keyResponse)).verifyComplete();

            StepVerifier.create(client.backupKey(keyToBackup.getName()))
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
        StepVerifier.create(client.backupKey("non-existing"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        restoreKeyRunner((keyToBackupAndRestore) -> {
            StepVerifier.create(client.createKey(keyToBackupAndRestore))
                .assertNext(keyResponse -> assertKeyEquals(keyToBackupAndRestore, keyResponse)).verifyComplete();
            byte[] backup = client.backupKey(keyToBackupAndRestore.getName()).block();

            PollerFlux<DeletedKey, Void> poller = client.beginDeleteKey(keyToBackupAndRestore.getName());
            AsyncPollResponse<DeletedKey, Void> pollResponse = poller
                .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                .blockLast();
            assertNotNull(pollResponse.getValue());

            StepVerifier.create(client.purgeDeletedKeyWithResponse(keyToBackupAndRestore.getName()))
                .assertNext(voidResponse -> {
                    assertEquals(HttpURLConnection.HTTP_NO_CONTENT, voidResponse.getStatusCode());
                }).verifyComplete();
            pollOnKeyPurge(keyToBackupAndRestore.getName());

            sleepInRecordMode(60000);

            StepVerifier.create(client.restoreKeyBackup(backup))
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
        StepVerifier.create(client.restoreKeyBackup(keyBackupBytes))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST));
    }

    /**
     * Tests that a deleted key can be retrieved on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        getDeletedKeyRunner((keyToDeleteAndGet) -> {

            StepVerifier.create(client.createKey(keyToDeleteAndGet))
                .assertNext(keyResponse -> assertKeyEquals(keyToDeleteAndGet, keyResponse)).verifyComplete();

            PollerFlux<DeletedKey, Void> poller = client.beginDeleteKey(keyToDeleteAndGet.getName());
            AsyncPollResponse<DeletedKey, Void> pollResponse = poller
                .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                .blockLast();
            assertNotNull(pollResponse.getValue());

            StepVerifier.create(client.getDeletedKey(keyToDeleteAndGet.getName()))
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

        if (!interceptorManager.isPlaybackMode()) {
            return;
        }

        listDeletedKeysRunner((keys) -> {
            List<DeletedKey> deletedKeys = new ArrayList<>();

            for (CreateKeyOptions key : keys.values()) {
                StepVerifier.create(client.createKey(key))
                    .assertNext(keyResponse -> assertKeyEquals(key, keyResponse)).verifyComplete();
            }

            sleepInRecordMode(10000);

            for (CreateKeyOptions key : keys.values()) {
                PollerFlux<DeletedKey, Void> poller = client.beginDeleteKey(key.getName());
                AsyncPollResponse<DeletedKey, Void> response = poller.blockLast();

                assertNotNull(response.getValue());
            }

            sleepInRecordMode(90000);

            DeletedKey deletedKey = client.listDeletedKeys().map(actualKey -> {
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
        listKeyVersionsRunner((keys) -> {
            List<KeyProperties> output = new ArrayList<>();
            String keyName = null;
            for (CreateKeyOptions key : keys) {
                keyName = key.getName();
                StepVerifier.create(client.createKey(key))
                    .assertNext(keyResponse -> assertKeyEquals(key, keyResponse)).verifyComplete();
            }
            sleepInRecordMode(30000);
            client.listPropertiesOfKeyVersions(keyName).subscribe(output::add);
            sleepInRecordMode(30000);

            assertEquals(keys.size(), output.size());
        });

    }

    /**
     * Tests that keys can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listKeys(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        listKeysRunner((keys) -> {
            for (CreateKeyOptions key : keys.values()) {
                assertKeyEquals(key, client.createKey(key).block());
            }
            sleepInRecordMode(10000);

            client.listPropertiesOfKeys().map(actualKey -> {
                if (keys.containsKey(actualKey.getName())) {
                    CreateKeyOptions expectedKey = keys.get(actualKey.getName());
                    assertEquals(expectedKey.getExpiresOn(), actualKey.getExpiresOn());
                    assertEquals(expectedKey.getNotBefore(), actualKey.getNotBefore());
                    keys.remove(actualKey.getName());
                }
                return actualKey;
            }).blockLast();
            assertEquals(0, keys.size());
        });
    }

    /**
     * Tests that an existing key can be released.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void releaseKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        // TODO: Remove assumption once Key Vault allows for creating exportable keys.
        Assumptions.assumeTrue(isManagedHsmTest);

        createKeyAsyncClient(httpClient, serviceVersion);
        releaseKeyRunner((keyToRelease, attestationUrl) -> {
            StepVerifier.create(client.createRsaKey(keyToRelease))
                .assertNext(keyResponse -> assertKeyEquals(keyToRelease, keyResponse)).verifyComplete();

            String target = "testAttestationToken";

            if (getTestMode() != TestMode.PLAYBACK) {
                if (!attestationUrl.endsWith("/")) {
                    attestationUrl = attestationUrl + "/";
                }

                try {
                    target = getAttestationToken(attestationUrl + "generate-test-token");
                } catch (IOException e) {
                    fail("Found error when deserializing attestation token.", e);
                }
            }

            StepVerifier.create(client.releaseKey(keyToRelease.getName(), target))
                .assertNext(releaseKeyResult -> assertNotNull(releaseKeyResult.getValue()))
                .expectComplete()
                .verify();
        });
    }

    /**
     * Tests that an RSA key with a public exponent can be created in the key vault.
     */
    @Disabled // Service issue: https://github.com/Azure/azure-sdk-for-java/issues/17382
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createRsaKeyWithPublicExponent(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);
        createRsaKeyWithPublicExponentRunner((createRsaKeyOptions) ->
            StepVerifier.create(client.createRsaKey(createRsaKeyOptions))
                .assertNext(rsaKey -> assertKeyEquals(createRsaKeyOptions, rsaKey))
                .assertNext(rsaKey -> {
                    ByteBuffer wrappedArray = ByteBuffer.wrap(rsaKey.getKey().getE()); // Big-endian by default
                    assertEquals(createRsaKeyOptions.getPublicExponent(), wrappedArray.getInt());
                })
                .verifyComplete());
    }

    private void pollOnKeyDeletion(String keyName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 30) {
            DeletedKey deletedKey = null;
            try {
                deletedKey = client.getDeletedKeyWithResponse(keyName).block().getValue();
            } catch (ResourceNotFoundException e) {
            }
            if (deletedKey == null) {
                sleepInRecordMode(2000);
                pendingPollCount += 1;
            } else {
                return;
            }
        }
        System.err.printf("Deleted Key %s not found \n", keyName);
    }

    private void pollOnKeyPurge(String keyName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 10) {
            DeletedKey deletedKey = null;
            try {
                deletedKey = client.getDeletedKeyWithResponse(keyName).block().getValue();
            } catch (ResourceNotFoundException e) {
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

