// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.security.keyvault.keys.models.KeyAttestation;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;

import static com.azure.security.keyvault.keys.cryptography.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("shouldRunHsmTest")
public class KeyClientManagedHsmTest extends KeyClientTest implements KeyClientManagedHsmTestBase {
    public KeyClientManagedHsmTest() {
        this.runManagedHsmTest = shouldRunHsmTest();
        this.isHsmEnabled = this.runManagedHsmTest;
    }

    public static boolean shouldRunHsmTest() {
        return Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT") != null
            || TEST_MODE == TestMode.PLAYBACK;
    }

    /**
     * Tests that an RSA key is created.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createRsaKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.createRsaKey(httpClient, serviceVersion);
    }

    /**
     * Tests that an RSA key with a public exponent can be created in the key vault.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createRsaKeyWithPublicExponent(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        createRsaKeyWithPublicExponentRunner((keyToCreate) -> {
            KeyVaultKey rsaKey = keyClient.createRsaKey(keyToCreate);

            assertKeyEquals(keyToCreate, rsaKey);
            assertEquals(BigInteger.valueOf(keyToCreate.getPublicExponent()), toBigInteger(rsaKey.getKey().getE()));
            assertEquals(keyToCreate.getKeySize(), rsaKey.getKey().getN().length * 8);
        });
    }

    /**
     * Tests that a key can be created in the key vault.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.createKey(httpClient, serviceVersion);
    }

    /**
     * Tests that a key can be created in the key vault while using a different tenant ID than the one that will be
     * provided in the authentication challenge.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createKeyWithMultipleTenants(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.createKeyWithMultipleTenants(httpClient, serviceVersion);
    }

    /**
     * Tests that a key is able to be updated when it exists.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.updateKey(httpClient, serviceVersion);
    }

    /**
     * Tests that a key is not able to be updated when it is disabled. 403 error is expected.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateDisabledKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.updateDisabledKey(httpClient, serviceVersion);
    }

    /**
     * Tests that an existing key can be retrieved.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.getKey(httpClient, serviceVersion);
    }

    /**
     * Tests that a specific version of the key can be retrieved.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKeySpecificVersion(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.getKeySpecificVersion(httpClient, serviceVersion);
    }

    /**
     * Tests that an existing key can be deleted.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.deleteKey(httpClient, serviceVersion);
    }

    /**
     * Tests that a deleted key can be retrieved on a soft-delete enabled vault.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.getDeletedKey(httpClient, serviceVersion);
    }

    /**
     * Tests that a deleted key can be recovered on a soft-delete enabled vault.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.recoverDeletedKey(httpClient, serviceVersion);
    }

    /**
     * Tests that keys can be listed in the key vault.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listKeys(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.listKeys(httpClient, serviceVersion);
    }

    /**
     * Tests that key versions can be listed in the key vault.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listKeyVersions(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.listKeyVersions(httpClient, serviceVersion);
    }

    /**
     * Tests that deleted keys can be listed in the key vault.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedKeys(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.listDeletedKeys(httpClient, serviceVersion);
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.backupKey(httpClient, serviceVersion);
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.restoreKey(httpClient, serviceVersion);
    }

    /**
     * Tests that a {@link com.azure.security.keyvault.keys.cryptography.CryptographyClient} can be created for a given
     * key using a {@link KeyClient}. Also tests that cryptographic operations can be performed with said cryptography
     * client.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCryptographyClientAndEncryptDecrypt(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.getCryptographyClientAndEncryptDecrypt(httpClient, serviceVersion);
    }

    /**
     * Tests that fetching the key rotation policy of a non-existent key throws.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKeyRotationPolicyWithNoPolicySet(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.getKeyRotationPolicyWithNoPolicySet(httpClient, serviceVersion);
    }

    /**
     * Tests that a symmetric key of a default is created.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createOctKeyWithDefaultSize(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        createOctKeyRunner(null, (keyToCreate) -> {
            KeyVaultKey octKey = keyClient.createOctKey(keyToCreate);

            assertEquals(keyToCreate.getName(), octKey.getName());
            assertEquals(KeyType.OCT_HSM, octKey.getKey().getKeyType());
            assertEquals(keyToCreate.getExpiresOn(), octKey.getProperties().getExpiresOn());
            assertEquals(keyToCreate.getNotBefore(), octKey.getProperties().getNotBefore());
            assertEquals(keyToCreate.getTags(), octKey.getProperties().getTags());
        });
    }

    /**
     * Tests that a symmetric key of a valid size is created.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createOctKeyWithValidSize(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        createOctKeyRunner(256, (keyToCreate) -> {
            KeyVaultKey octKey = keyClient.createOctKey(keyToCreate);

            assertEquals(keyToCreate.getName(), octKey.getName());
            assertEquals(KeyType.OCT_HSM, octKey.getKey().getKeyType());
            assertEquals(keyToCreate.getExpiresOn(), octKey.getProperties().getExpiresOn());
            assertEquals(keyToCreate.getNotBefore(), octKey.getProperties().getNotBefore());
            assertEquals(keyToCreate.getTags(), octKey.getProperties().getTags());
        });
    }

    /**
     * Tests that a symmetric key of an invalid size cannot be created.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createOctKeyWithInvalidSize(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        createOctKeyRunner(64,
            (keyToCreate) -> assertThrows(ResourceModifiedException.class, () -> keyClient.createOctKey(keyToCreate)));
    }

    /**
     * Tests that random bytes can be retrieved from a Managed HSM.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getRandomBytes(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        getRandomBytesRunner((count) -> {
            byte[] randomBytes = keyClient.getRandomBytes(count);

            assertEquals(count, randomBytes.length);
        });
    }

    /**
     * Tests that an existing key can be released.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void releaseKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        super.releaseKey(httpClient, serviceVersion);
    }

    /**
     * Tests that a key's attestation material can be retrieved.
     */
    @Override
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKeyAttestation(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        getKeyAttestationRunner((keyToCreate) -> {
            assertKeyEquals(keyToCreate, keyClient.createKey(keyToCreate));

            KeyVaultKey keyWithAttestation = keyClient.getKeyAttestation(keyToCreate.getName());

            assertNotNull(keyWithAttestation);

            KeyAttestation keyAttestation = keyWithAttestation.getProperties().getKeyAttestation();

            assertNotNull(keyAttestation);
            assertNotNull(keyAttestation.getCertificatePemFile());
            assertTrue(keyAttestation.getCertificatePemFile().length > 0);
            assertNotNull(keyAttestation.getPrivateKeyAttestation());
            assertTrue(keyAttestation.getPrivateKeyAttestation().length > 0);
            assertNotNull(keyAttestation.getPublicKeyAttestation());
            assertTrue(keyAttestation.getPublicKeyAttestation().length > 0);
            assertNotNull(keyAttestation.getVersion());
        });
    }
}
