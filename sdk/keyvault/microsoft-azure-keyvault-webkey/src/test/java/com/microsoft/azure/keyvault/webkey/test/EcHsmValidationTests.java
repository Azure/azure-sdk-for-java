// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.webkey.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.keyvault.webkey.JsonWebKey;
import org.junit.Assert;
import org.junit.Test;


public class EcHsmValidationTests {

    String keyWithoutT = "{\"kid\":\"key_id\",\"kty\":\"EC-HSM\",\"key_ops\":null,\"n\":null,\"e\":null,\"d\":null,\"dp\":null,\"dq\":null,\"qi\":null,\"p\":null,\"q\":null,\"k\":null,\"key_hsm\":null,\"crv\":\"P-256\",\"x\":\"KyjF795jLyVIgswKSQInEGYHNBKSKyPgNojEgYlldMI\",\"y\":\"AIl_ca1ZIKbJ5YGdgGr_7HySldI2aWeBaOImZEYIMpVe\"}";
    String keyWithT = "{\"kid\":\"key_id\",\"kty\":\"EC-HSM\",\"key_ops\":null,\"key_hsm\":\"VC1UT0tFTg\"}";

    @Test
    public void ecHsmValidation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonWebKey keyNoT = mapper.readValue(keyWithoutT, JsonWebKey.class);
        JsonWebKey keyT = mapper.readValue(keyWithT, JsonWebKey.class);

        Assert.assertTrue(keyNoT.isValid());
        Assert.assertFalse(keyNoT.hasPrivateKey());

        Assert.assertTrue(keyT.isValid());
        Assert.assertFalse(keyT.hasPrivateKey());
    }

    @Test
    public void ecHsmHashCode() throws Exception {
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
