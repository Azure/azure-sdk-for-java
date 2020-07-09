// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class ByteBufferOutputStreamTest {

    @Test(groups = "unit", dataProvider = "length")
    public void byteBuffer(int len) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteBufferOutputStream byteBufferOutputStream = new ByteBufferOutputStream();
        Random r = new Random();

        for (int i = 0; i < len; i++) {
            byte b = (byte) r.nextInt();
            byteArrayOutputStream.write(b);
            byteBufferOutputStream.write(b);
        }

        byte[] expectedBytes = byteArrayOutputStream.toByteArray();
        ByteBuffer byteBuffer = byteBufferOutputStream.asByteBuffer();

        assertThat(byteBuffer.limit()).isEqualTo(expectedBytes.length);

        for (byte b: expectedBytes) {
            assertThat(byteBuffer.get()).isEqualTo(b);
        }

        for (int i = 0; i < expectedBytes.length; i++) {
            assertThat(byteBuffer.array()[i]).isEqualTo(expectedBytes[i]);
        }
    }

    @DataProvider(name = "length")
    public Object[][] length() {
        return new Object[][]{
            { 0 },
            { 1 },
            { 2 },
            { 3 },
            { 31 },
            { 32 },
            { 33 },
            { 63 },
            { 64 },
            { 65 },
            {1022},
            {1023},
            {1024},
        };
    }
}
