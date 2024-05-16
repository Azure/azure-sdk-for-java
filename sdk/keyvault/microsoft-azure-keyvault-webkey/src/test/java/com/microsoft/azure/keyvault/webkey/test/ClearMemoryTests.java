// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.webkey.test;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.keyvault.webkey.JsonWebKey;

public class ClearMemoryTests {

    @Test
    public void clearMemory() {
        JsonWebKey key = new JsonWebKey()
                .withD(getRandomByte())
                .withDp(getRandomByte())
                .withDq(getRandomByte())
                .withE(getRandomByte())
                .withK(getRandomByte())
                .withN(getRandomByte())
                .withP(getRandomByte())
                .withQ(getRandomByte())
                .withQi(getRandomByte())
                .withT(getRandomByte());
        key.clearMemory();
        Assert.assertNull(key.d());
        Assert.assertNull(key.dp());
        Assert.assertNull(key.dq());
        Assert.assertNull(key.e());
        Assert.assertNull(key.k());
        Assert.assertNull(key.n());
        Assert.assertNull(key.p());
        Assert.assertNull(key.q());
        Assert.assertNull(key.qi());
        Assert.assertNull(key.t());

        // Compare it with a newly created JsonWebKey with no properties set.
        JsonWebKey key2 = new JsonWebKey();
        Assert.assertTrue(key2.equals(key));
    }

    @Test
    public void clearNullMemory() {
        JsonWebKey key = new JsonWebKey();
        key.clearMemory();
    }

    private static byte[] getRandomByte() {
        byte[] bytes = new byte[10];
        new Random().nextBytes(bytes);
        return bytes;
    }

}
