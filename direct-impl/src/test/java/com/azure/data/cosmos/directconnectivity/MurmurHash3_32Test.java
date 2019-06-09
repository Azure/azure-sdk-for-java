/*
 *
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos.directconnectivity;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.azure.data.cosmos.internal.routing.MurmurHash3_32;
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
