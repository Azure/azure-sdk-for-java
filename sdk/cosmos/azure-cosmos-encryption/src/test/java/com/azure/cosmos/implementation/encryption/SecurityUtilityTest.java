// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.google.common.io.BaseEncoding;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class SecurityUtilityTest {
    byte[] secretSha256Key;

    @BeforeClass(groups = "unit")
    public void beforeClass() throws Exception {
        secretSha256Key = generateKey();
    }

    @Test(groups = "unit", dataProvider = "sha256KeyHashInputProvider")
    public void getHMACWithSHA256(byte[] input, String expectedHashAsHex) throws Exception {
        byte[] expectedHash = hexToByteArray(expectedHashAsHex);
        byte[] output = new byte[expectedHashAsHex.length() / 2];

        SecurityUtility.getHMACWithSHA256(input, secretSha256Key, output);
        assertThat(output).isEqualTo(expectedHash);
    }

    @Test(groups = "unit", dataProvider = "sha256InputProvider")
    public void getSHA256Hash(byte[] input, String expectedHash) throws Exception {
        String hash = SecurityUtility.getSHA256Hash(input);
        assertThat(hash).isEqualTo(expectedHash);
    }

    @Test(groups = "unit")
    public void generateRandomBytes() {
        ShanonEntropyGauge entropy = new ShanonEntropyGauge();
        int numberOfRandomBytes = 2;
        byte[] output = new byte[numberOfRandomBytes];

        for (int i = 0; i < 1_000_000; i++) {
            SecurityUtility.generateRandomBytes(output);
            entropy.add(output);
        }

        double entropyValue = entropy.calculate();

        // a smoke test validating shannon entropy
        assertThat(entropyValue).isGreaterThan(numberOfRandomBytes * 8 - 1);
        assertThat(entropyValue).isLessThan(numberOfRandomBytes * 8);
    }

    @DataProvider(name = "sha256KeyHashInputProvider")
    public Object[][] sha256KeyHashInputProvider() {
        return new Object[][]{
            // byte array plain text, hex of hmac byte array
            {"".getBytes(StandardCharsets.UTF_8), "BE360B09127711ED4E"},
            {"test".getBytes(StandardCharsets.UTF_8), "9BC3AF5A"},
            {"تست".getBytes(StandardCharsets.UTF_8), "1A66670142D26FAE72"},
        };
    }

    @DataProvider(name = "sha256InputProvider")
    public Object[][] sha256InputProvider() {
        return new Object[][]{
            {"".getBytes(StandardCharsets.UTF_8), "E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855"},
            {"good morning".getBytes(StandardCharsets.UTF_8), "CDF71DEA7D7741A2B6F021F3DD344F75C8333988F547866A8FBF28F064CF7C78"},
            {"صبح بخیر".getBytes(StandardCharsets.UTF_8), "5D504A5D6F1946209EDD3B99FB52AB77A60310675C8FDE03726112796893A650"},
            {"शुभ प्रभात".getBytes(StandardCharsets.UTF_8), "C033E4FD4691F0AD6422FB2B91BB64644C6F56D962DF206D60A0A1FEF23297F8"},
        };
    }

    private static byte[] generateKey() throws Exception {
        final SecretKeySpec keyspec = new javax.crypto.spec.SecretKeySpec("کلید".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return keyspec.getEncoded();
    }

    private static byte[] hexToByteArray(String hex) {
        return BaseEncoding.base16().decode(hex);
    }
}
