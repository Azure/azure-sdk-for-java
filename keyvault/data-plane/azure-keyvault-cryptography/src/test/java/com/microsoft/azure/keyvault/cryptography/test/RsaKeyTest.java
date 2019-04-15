// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.cryptography.test;

import static org.junit.Assert.*;

import java.security.MessageDigest;
import java.security.Provider;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.keyvault.cryptography.RsaKey;
import com.microsoft.azure.keyvault.cryptography.algorithms.Rs256;
import com.microsoft.azure.keyvault.cryptography.algorithms.Rsa15;
import com.microsoft.azure.keyvault.cryptography.algorithms.RsaOaep;
import com.microsoft.azure.keyvault.webkey.JsonWebKey;

public class RsaKeyTest {

    // A Content Encryption Key, or Message. This value is kept consistent with the .NET
    // unit test cases to enable cross platform testing.
    static final byte[] CEK                    = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
    static final String CrossPlatformHash      = "qPrtarvzXBKksm5A9v6xnXNtkARcg7n5ox9jjTI+aBE=";
    static final String CrossPlatformSignature = "RaNc+8WcWxplS8I7ynJLSoLJKz+dgBvrZhIGH3VFlTTyzu7b9d+lpaV9IKhzCNBsgSysKhgL7EZwVCOTBZ4m6xvKSXqVFXYaBPyBTD7VoKPMYMW6ai5x6xV5XAMaZPfMkff3Deg/RXcc8xQ28FhYuUa8yly01GySY4Hk55anEvb2wBxSy1UGun/0LE1lYH3C3XEgSry4cEkJHDJl1hp+wB4J/noXOqn5ECGU+/4ehBJOyW1gtUH0/gRe8yXnDH0AXepHRyH8iBHLWlKX1r+1/OrMulqOoi82RZzJlTyEz9X+bsQhllqGF6n3hdLS6toH9o7wUtwYNqSx82JuQT6iMg==";

    private Provider _provider = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    protected void setProvider(Provider provider) {
        _provider = provider;
    }

    @Test
    public void testRsa15() throws Exception {

        RsaKey key = getTestRsaKey();

        // Wrap and Unwrap
        Pair<byte[], String> wrapped   = key.wrapKeyAsync(CEK, Rsa15.ALGORITHM_NAME).get();
        byte[]               unwrapped = key.unwrapKeyAsync(wrapped.getLeft(), wrapped.getRight()).get();

        // Assert
        assertEquals(Rsa15.ALGORITHM_NAME, wrapped.getRight());
        assertArrayEquals(CEK, unwrapped);

        // Encrypt and Decrypt
        Triple<byte[], byte[], String> encrypted = key.encryptAsync(CEK, null, null, Rsa15.ALGORITHM_NAME).get();
        byte[]                         decrypted = key.decryptAsync(encrypted.getLeft(), null, null, null, encrypted.getRight()).get();

        // Assert
        assertEquals(Rsa15.ALGORITHM_NAME, encrypted.getRight());
        assertArrayEquals(CEK, decrypted);

        key.close();
    }

    @Test
    public void testRsaOaep() throws Exception {

        RsaKey key = getTestRsaKey();

        // Wrap and Unwrap
        Pair<byte[], String> wrapped   = key.wrapKeyAsync(CEK, RsaOaep.ALGORITHM_NAME).get();
        byte[]               unwrapped = key.unwrapKeyAsync(wrapped.getLeft(), wrapped.getRight()).get();

        // Assert
        assertEquals(RsaOaep.ALGORITHM_NAME, wrapped.getRight());
        assertArrayEquals(CEK, unwrapped);

        // Encrypt and Decrypt
        Triple<byte[], byte[], String> encrypted = key.encryptAsync(CEK, null, null, RsaOaep.ALGORITHM_NAME).get();
        byte[]                         decrypted = key.decryptAsync(encrypted.getLeft(), null, null, null, encrypted.getRight()).get();

        // Assert
        assertEquals(RsaOaep.ALGORITHM_NAME, encrypted.getRight());
        assertArrayEquals(CEK, decrypted);

        key.close();
    }

    @Test
    public void testDefaultAlgorithm() throws Exception {

        RsaKey key = getTestRsaKey();

        assertEquals(RsaOaep.ALGORITHM_NAME, key.getDefaultEncryptionAlgorithm());
        assertEquals(RsaOaep.ALGORITHM_NAME, key.getDefaultKeyWrapAlgorithm());
        assertEquals(Rs256.ALGORITHM_NAME, key.getDefaultSignatureAlgorithm());

        // Wrap and Unwrap
        Pair<byte[], String> wrapped   = key.wrapKeyAsync(CEK, key.getDefaultKeyWrapAlgorithm()).get();
        byte[]               unwrapped = key.unwrapKeyAsync(wrapped.getLeft(), wrapped.getRight()).get();

        // Assert
        assertEquals(RsaOaep.ALGORITHM_NAME, wrapped.getRight());
        assertArrayEquals(CEK, unwrapped);

        // Encrypt and Decrypt
        Triple<byte[], byte[], String> encrypted = key.encryptAsync(CEK, null, null, key.getDefaultEncryptionAlgorithm()).get();
        byte[]                         decrypted = key.decryptAsync(encrypted.getLeft(), null, null, null, encrypted.getRight()).get();

        // Assert
        assertEquals(RsaOaep.ALGORITHM_NAME, encrypted.getRight());
        assertArrayEquals(CEK, decrypted);

        key.close();
    }

    @Test
    public void testSignVerify() throws Exception {

        RsaKey key = getTestRsaKey();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(CEK);

        byte[] crossPlatformHash      = Base64.decodeBase64(CrossPlatformHash);
        byte[] crossPlatformSignature = Base64.decodeBase64(CrossPlatformSignature);

        // Check the hash
        assertNotNull(hash);
        assertEquals(32, hash.length);
        assertArrayEquals(hash, crossPlatformHash);

        Pair<byte[], String> signature = key.signAsync(hash, "RS256").get();
        boolean              result    = key.verifyAsync(hash, signature.getLeft(), "RS256").get();

        // Check the signature
        assertTrue(result);
        assertArrayEquals(crossPlatformSignature, signature.getLeft());

        // Now prove we can verify the cross platform signature
        result = key.verifyAsync(hash, Base64.decodeBase64(CrossPlatformSignature), "RS256").get();

        assertTrue(result);

        key.close();
    }

    @Test
    public void testToFromJsonWebKey() throws Exception {
        RsaKey key = getTestRsaKey();
        JsonWebKey jwk = key.toJsonWebKey();
        jwk.withKid("new kid");
        //setting kid
        RsaKey sameKey = RsaKey.fromJsonWebKey(jwk, true, _provider);
        JsonWebKey jwkSame = sameKey.toJsonWebKey();
        jwkSame.withKid("new kid");
        assertEquals(jwk, jwkSame);
    }

    private RsaKey getTestRsaKey() throws Exception {
        String       jwkString = "{\"kty\":\"RSA\",\"n\":\"rZ8pnmXkhfmmgNWVVdtNcYy2q0OAcCGIpeFzsN9URqJsiBEiWQfxlUxFTbM4kVWPqjauKt6byvApBGEeMA7Qs8kxwRVP-BD4orXRe9VPgliM92rH0UxQWHmCHUe7G7uUAFPwbiDVhWuFzELxNa6Kljg6Z9DuUKoddmQvlYWj8uSunofCtDi_zzlZKGYTOYJma5IYScHNww1yjLp8-b-Be2UdHbrPkCv6Nuwi6MVIKjPpEeRQgfefRmxDBJQKY3OfydMXZmEwukYXVkUcdIP8XwG2OxnfdRK0oAo0NDebNNVuT89k_3AyZLTr1KbDmx1nnjwa8uB8k-uLtcOC9igbTw\",\"e\":\"AQAB\",\"d\":\"H-z7hy_vVJ9yeZBMtIvt8qpQUK_J51STPwV085otcgud72tPKJXoW2658664ASl9kGwbnLBwb2G3-SEunuGqiNS_PGUB3niob6sFSUMRKsPDsB9HfPoOcCZvwZiWFGRqs6C7vlR1TuJVqRjKJ_ffbf4K51oo6FZPspx7j4AShLAwLUSQ60Ld5QPuxYMYZIMpdVbMVIVHJ26pR4Y18e_0GYmEGnbF5N0HkwqQmfmTiIK5aoGnD3GGgqHeHmWBwh6_WAq90ITLcX_zBeqQUgBSj-Z5v61SroO9Eang36T9mMoYrcPpYwemtAOb4HhQYDj8dCCfbeOcVmvZ9UJKWCX2oQ\",\"dp\":\"HW87UpwPoj3lPI9B9K1hJFeuGgarpakvtHuk1HpZ5hXWFGAJiXoWRV-jvYyjoM2k7RpSxPyuuFFmYHcIxiGFp2ES4HnP0BIhKVa2DyugUxIEcMK53C43Ub4mboJPZTSC3sapKgAmA2ue624sapWmshTPpx9qnUP2Oj3cSMkgMGE\",\"dq\":\"RhwEwb5FYio0GS2tmul8FAYsNH7JDehwI1yUApnTiakhSenFetml4PYyVkKR4csgLZEi3RY6J3R8Tg-36zrZuF7hxhVJn80L5_KETSpfEI3jcrXMVg4SRaMsWLY9Ahxflt2FJgUnHOmWRLmP6_hmaTcxxSACjbyUd_HhwNavD5E\",\"qi\":\"wYPZ4lKIslA1w3FaAzQifnNLABYXXUZ_KAA3a8T8fuxkdE4OP3xIFX7WHhnmBd6uOFiEcGoeq2jNQqDg91rV5661-5muQKcvp4uUsNId5rQw9EZw-kdDcwMtVFTEBfvVuyp83X974xYAHn1Jd8wWohSwrpi1QuH5cQMR5Fm6I1A\",\"p\":\"74Ot7MgxRu4euB31UWnGtrqYPjJmvbjYESS43jfDfo-s62ggV5a39P_YPg6oosgtGHNw0QDxunUOXNu9iriaYPf_imptRk69bKN8Nrl727Y-AaBYdLf1UZuwz8X07FqHAH5ghYpk79djld8QvkUUJLpx6rzcW8BJLTOi46DtzZE\",\"q\":\"uZJu-qenARIt28oj_Jlsk-p_KLnqdczczZfbRDd7XNp6csGLa8R0EyYqUB4xLWELQZsX4tAu9SaAO62tuuEy5wbOAmOVrq2ntoia1mGQSJdoeVq6OqtN300xVnaBc3us0rm8C6-824fEQ1PWXoulXLKcSqBhFT-hQahsYi-kat8\"}";
        ObjectMapper mapper    = new ObjectMapper();
        JsonWebKey   jwk       = null;

        jwk = mapper.readValue(jwkString, JsonWebKey.class);

        return new RsaKey("foo", jwk.toRSA(true, _provider));
    }

}
