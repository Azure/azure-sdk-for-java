// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.webkey.test;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.keyvault.webkey.JsonWebKey;


public class RsaHsmValidationTests {

    String keyWithoutT = "{\"kid\":\"key_id\",\"kty\":\"RSA-HSM\",\"key_ops\":[\"encrypt\",\"decrypt\"],\"n\":\"1_6ZtP288hEkKML-L6nFyZh1PD1rmAgwbbwjEvTSDK_008BYWhjp_6ULy9BhWtRIytNkPkm9gzaBTrCpp-vyDXPGa836Htp-w8u5JmxoUZchJh576m3m-8ZYWTmZSAp5SpruyKAmLSxPJHEWPXQntnmuTMjb9HBT9Ltrwc0ZDk-jsMLYunDJrNmrRUxQgb0zQ_Tl5fJjj8j-0KVx2RXtbfWFvf5fRdBYyP3m0aUpoopQPwtXszD2LcSKMJ_TnmnvMWr8MOA5aRlBaGdBk7zBgRafvDPam3Q2AvFA9mfcAVncpfZ3JFm73VARw6MofXtRqOHtZ7y4oNbY95xXwU2r6w\",\"e\":\"AQAB\"}";
    String keyWithT    = "{\"kid\":\"key_id\",\"kty\":\"RSA-HSM\",\"key_ops\":[\"encrypt\",\"decrypt\"],\"key_hsm\":\"VC1UT0tFTg\"}";

    @Test
    public void rsaHsmValidation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonWebKey keyNoT = mapper.readValue(keyWithoutT, JsonWebKey.class);
        JsonWebKey keyT = mapper.readValue(keyWithT, JsonWebKey.class);

        Assert.assertTrue(keyNoT.isValid());
        Assert.assertFalse(keyNoT.hasPrivateKey());

        Assert.assertTrue(keyT.isValid());
        Assert.assertFalse(keyT.hasPrivateKey());
    }

    @Test
    public void rsaHsmHashCode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonWebKey keyNoT = mapper.readValue(keyWithoutT, JsonWebKey.class);
        JsonWebKey keyT = mapper.readValue(keyWithT, JsonWebKey.class);

        Assert.assertNotEquals(keyT.hashCode(), keyNoT.hashCode());

        // Compare hash codes for unequal JWK that would not map to the same hash
        Assert.assertNotEquals(keyT.hashCode(), new JsonWebKey().withKid(keyT.kid()).withT(keyT.t()).hashCode());
        Assert.assertNotEquals(keyT.hashCode(), new JsonWebKey().withKid(keyT.kid()).withKty(keyT.kty()).hashCode());
        Assert.assertNotEquals(keyNoT.hashCode(), new JsonWebKey().hashCode());

        // Compare hash codes for unequal JWK that would map to the same hash
        Assert.assertEquals(keyT.hashCode(),
                new JsonWebKey().withKid(keyT.kid()).withKty(keyT.kty()).withT(keyT.t()).hashCode());
        Assert.assertEquals(keyNoT.hashCode(), new JsonWebKey().withKid(keyT.kid()).hashCode());
    }
}
