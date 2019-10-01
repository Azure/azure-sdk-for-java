// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.routing.MurmurHash3_32;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.RandomUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * validates {@link MurmurHash3_32} against Google's murmur3_32 implementation.
 */
public class MurmurHash3_32Test {

    private MurmurHash3_32 murmurHash3_32;

    @BeforeClass(groups = "unit")
    public void setup() {
        murmurHash3_32 = new MurmurHash3_32();
    }

    @Test(groups = "unit")
    public void murmurHash3_32_EmptyByteArray() {
        byte[] byteArray = new byte[0];
        int actualHash = murmurHash3_32.hash(byteArray, byteArray.length, 0);

        HashFunction googleMurmur3_32 = Hashing.murmur3_32(0);
        int expectedHash = googleMurmur3_32.hashBytes(byteArray).asInt();

        assertThat(actualHash).isEqualTo(expectedHash);
    }

    @Test(groups = "unit")
    public void murmurHash3_32_String() {
        byte[] byteArray = new String("test").getBytes(Charset.forName("UTF-8"));
        int actualHash = murmurHash3_32.hash(byteArray, byteArray.length, 0);

        HashFunction googleMurmur3_32 = Hashing.murmur3_32(0);
        int expectedHash = googleMurmur3_32.hashBytes(byteArray).asInt();

        assertThat(actualHash).isEqualTo(expectedHash);
    }

    @Test(groups = "unit")
    public void murmurHash3_32_NonLatin() throws UnsupportedEncodingException {
        String nonLatin = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяабвгдеёжзийклмнопрстуфхцчшщъыьэюяабвгдеёжзийклмнопрстуфхцчшщъыьэюяабвгдеёжзийклмнопрстуфхцчшщъыьэюя";
        for(int i = 0; i < nonLatin.length() + 1; i++) {
            byte[] byteArray = nonLatin.substring(0, i).getBytes("UTF-8");
            int actualHash = murmurHash3_32.hash(byteArray, byteArray.length, 0);

            HashFunction googleMurmur3_32 = Hashing.murmur3_32(0);
            int expectedHash = googleMurmur3_32.hashBytes(byteArray).asInt();

            assertThat(actualHash).isEqualTo(expectedHash);
        }
    }

    @Test(groups = "unit")
    public void murmurHash3_32_ZeroByteArray() {
        byte[] byteArray = new byte[3];
        int actualHash = murmurHash3_32.hash(byteArray, byteArray.length, 0);

        HashFunction googleMurmur3_32 = Hashing.murmur3_32(0);
        int expectedHash = googleMurmur3_32.hashBytes(byteArray).asInt();

        assertThat(actualHash).isEqualTo(expectedHash);
    }

    @Test(groups = "unit")
    public void murmurHash3_32_RandomBytesOfAllSizes() {
        for(int i = 0; i < 1000; i++) {
            byte[] byteArray = randomBytes(i);

            int actualHash = murmurHash3_32.hash(byteArray, byteArray.length, 0);

            HashFunction googleMurmur3_32 = Hashing.murmur3_32(0);
            int expectedHash = googleMurmur3_32.hashBytes(byteArray).asInt();

            assertThat(actualHash).isEqualTo(expectedHash);
        }
    }

    private byte[] randomBytes(int count) {
        return RandomUtils.nextBytes(count);
    }
}
