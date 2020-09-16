// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.encryption.EncryptionType;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class AesCryptoServiceProviderTest {
    private byte[] key;

    @BeforeClass(groups = "unit")
    public void beforeClass() throws Exception {
        key = TestUtils.generatePBEKeySpec("myPassword");
    }

    @Test(groups = "unit", dataProvider = "encryptionInput")
    public void aesEncryptThenDecrypt(byte[] input)  {
        AeadAes256CbcHmac256EncryptionKey aeadAesKey = new AeadAes256CbcHmac256EncryptionKey(key, "AES");
        AeadAes256CbcHmac256Algorithm encryptionAlgorithm = new AeadAes256CbcHmac256Algorithm(aeadAesKey, EncryptionType.RANDOMIZED, (byte) 0x01);
        byte[] encrypted = encryptionAlgorithm.encryptData(input);

        assertThat(encrypted).isNotEqualTo(input);
        assertThat(encrypted.length).isGreaterThan(input.length);

        byte[] decrypted = encryptionAlgorithm.decryptData(encrypted);
        assertThat(decrypted).isEqualTo(input);
    }

    @DataProvider(name = "encryptionInput")
    public Object[][] encryptionInput() {
        return new Object[][]{
            { new byte[] {} },
            {"secret".getBytes(StandardCharsets.UTF_8) },
            {"محرمانه".getBytes(StandardCharsets.UTF_8) },
            { RandomStringUtils.randomAlphabetic(100_000).getBytes(StandardCharsets.UTF_8) }
        };
    }
}
