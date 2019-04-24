// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.cryptography.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.security.Provider;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.keyvault.cryptography.ICryptoTransform;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes128Cbc;

public class AesCbcTest {

    private Provider _provider = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        setProvider(null);
    }

    @After
    public void tearDown() throws Exception {
    }
    
    protected void setProvider(Provider provider) {
        _provider = provider;
    }

    @Test
    public void testAes128CbcOneBlock() {
        // Note that AES128CBC as implemented in this library uses PKCS7 padding mode where the test
        // vectors from RFC3602 do not use padding.
        byte[] CEK   = { 0x06, (byte) 0xa9, 0x21, 0x40, 0x36, (byte) 0xb8, (byte) 0xa1, 0x5b, 0x51, 0x2e, 0x03, (byte) 0xd5, 0x34, 0x12, 0x00, 0x06 };
        byte[] PLAIN = "Single block msg".getBytes();
        byte[] IV    = { 0x3d, (byte) 0xaf, (byte) 0xba, 0x42, (byte) 0x9d, (byte) 0x9e, (byte) 0xb4, 0x30, (byte) 0xb4, 0x22, (byte) 0xda, (byte) 0x80, 0x2c, (byte) 0x9f, (byte) 0xac, 0x41 };
        byte[] ED    = { (byte) 0xe3, 0x53, 0x77, (byte) 0x9c, 0x10, 0x79, (byte) 0xae, (byte) 0xb8, 0x27, 0x08, (byte) 0x94, 0x2d, (byte) 0xbe, 0x77, 0x18, 0x1a };

        Aes128Cbc algo      = new Aes128Cbc();
        byte[]    encrypted = null;

        ICryptoTransform encryptor = null;
        try {
            encryptor = algo.CreateEncryptor(CEK, IV, null, _provider);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            encrypted = encryptor.doFinal(PLAIN);
            
            // Assert: we only compare the first 16 bytes as this library uses PKCS7 padding
            assertArrayEquals(Arrays.copyOfRange(encrypted, 0, 16), ED);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ICryptoTransform decryptor = null;
        try {
            decryptor = algo.CreateDecryptor(CEK, IV, null, null, _provider);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        byte[] decrypted = null;

        try {
            decrypted = decryptor.doFinal(encrypted);
            
            // Assert: we only compare the first 16 bytes as this library uses PKCS7 padding
            assertArrayEquals(Arrays.copyOfRange(decrypted, 0, 16), PLAIN);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAes128CbcTwoBlock() {
        // Note that AES128CBC as implemented in this library uses PKCS7 padding mode where the test
        // vectors do not use padding.
        byte[] CEK   = { (byte) 0xc2, (byte) 0x86, 0x69, 0x6d, (byte) 0x88, 0x7c, (byte) 0x9a, (byte) 0xa0, 0x61, 0x1b, (byte) 0xbb, 0x3e, 0x20, 0x25, (byte) 0xa4, 0x5a };
        byte[] PLAIN = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f };
        byte[] IV    = { 0x56, 0x2e, 0x17, (byte) 0x99, 0x6d, 0x09, 0x3d, 0x28, (byte) 0xdd, (byte) 0xb3, (byte) 0xba, 0x69, 0x5a, 0x2e, 0x6f, 0x58 };
        byte[] ED    = { (byte) 0xd2, (byte) 0x96, (byte) 0xcd, (byte) 0x94, (byte) 0xc2, (byte) 0xcc, (byte) 0xcf, (byte) 0x8a, 0x3a, (byte) 0x86, 0x30, 0x28, (byte) 0xb5, (byte) 0xe1, (byte) 0xdc, 0x0a, 0x75, (byte) 0x86, 0x60, 0x2d, 0x25, 0x3c, (byte) 0xff, (byte) 0xf9, 0x1b, (byte) 0x82, 0x66, (byte) 0xbe, (byte) 0xa6, (byte) 0xd6, 0x1a, (byte) 0xb1 };

        Aes128Cbc algo      = new Aes128Cbc();
        byte[]    encrypted = null;

        ICryptoTransform encryptor = null;
        try {
            encryptor = algo.CreateEncryptor(CEK, IV, null, _provider);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            encrypted = encryptor.doFinal(PLAIN);
            
            // Assert: we only compare the first 32 bytes as this library uses PKCS7 padding
            assertArrayEquals(Arrays.copyOfRange(encrypted, 0, 32), ED);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ICryptoTransform decryptor = null;
        try {
            decryptor = algo.CreateDecryptor(CEK, IV, null, null, _provider);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        byte[] decrypted = null;

        try {
            decrypted = decryptor.doFinal(encrypted);
            
            // Assert: we only compare the first 32 bytes as this library uses PKCS7 padding
            assertArrayEquals(Arrays.copyOfRange(decrypted, 0, 32), PLAIN);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAes128CbcOneBlock_ExcessKeyMaterial() {
        // Note that AES128CBC as implemented in this library uses PKCS7 padding mode where the test
        // vectors from RFC3602 do not use padding.
        byte[] CEK   = { 0x06, (byte) 0xa9, 0x21, 0x40, 0x36, (byte) 0xb8, (byte) 0xa1, 0x5b, 0x51, 0x2e, 0x03, (byte) 0xd5, 0x34, 0x12, 0x00, 0x06, (byte) 0xc2, (byte) 0x86, 0x69, 0x6d, (byte) 0x88, 0x7c, (byte) 0x9a, (byte) 0xa0, 0x61, 0x1b, (byte) 0xbb, 0x3e, 0x20, 0x25, (byte) 0xa4, 0x5a };
        byte[] PLAIN = "Single block msg".getBytes();
        byte[] IV    = { 0x3d, (byte) 0xaf, (byte) 0xba, 0x42, (byte) 0x9d, (byte) 0x9e, (byte) 0xb4, 0x30, (byte) 0xb4, 0x22, (byte) 0xda, (byte) 0x80, 0x2c, (byte) 0x9f, (byte) 0xac, 0x41 };
        byte[] ED    = { (byte) 0xe3, 0x53, 0x77, (byte) 0x9c, 0x10, 0x79, (byte) 0xae, (byte) 0xb8, 0x27, 0x08, (byte) 0x94, 0x2d, (byte) 0xbe, 0x77, 0x18, 0x1a };

        Aes128Cbc algo      = new Aes128Cbc();
        byte[]    encrypted = null;

        ICryptoTransform encryptor = null;
        try {
            encryptor = algo.CreateEncryptor(CEK, IV, null, _provider);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            encrypted = encryptor.doFinal(PLAIN);
            
            // Assert: we only compare the first 16 bytes as this library uses PKCS7 padding
            assertArrayEquals(Arrays.copyOfRange(encrypted, 0, 16), ED);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ICryptoTransform decryptor = null;
        try {
            decryptor = algo.CreateDecryptor(CEK, IV, null, null, _provider);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        byte[] decrypted = null;

        try {
            decrypted = decryptor.doFinal(encrypted);
            
            // Assert: we only compare the first 16 bytes as this library uses PKCS7 padding
            assertArrayEquals(Arrays.copyOfRange(decrypted, 0, 16), PLAIN);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAes128CbcTwoBlock_ExcessKeyMaterial() {
        // Note that AES128CBC as implemented in this library uses PKCS7 padding mode where the test
        // vectors do not use padding.
        byte[] CEK   = { (byte) 0xc2, (byte) 0x86, 0x69, 0x6d, (byte) 0x88, 0x7c, (byte) 0x9a, (byte) 0xa0, 0x61, 0x1b, (byte) 0xbb, 0x3e, 0x20, 0x25, (byte) 0xa4, 0x5a, (byte) 0xc2, (byte) 0x86, 0x69, 0x6d, (byte) 0x88, 0x7c, (byte) 0x9a, (byte) 0xa0, 0x61, 0x1b, (byte) 0xbb, 0x3e, 0x20, 0x25, (byte) 0xa4, 0x5a };
        byte[] PLAIN = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f };
        byte[] IV    = { 0x56, 0x2e, 0x17, (byte) 0x99, 0x6d, 0x09, 0x3d, 0x28, (byte) 0xdd, (byte) 0xb3, (byte) 0xba, 0x69, 0x5a, 0x2e, 0x6f, 0x58 };
        byte[] ED    = { (byte) 0xd2, (byte) 0x96, (byte) 0xcd, (byte) 0x94, (byte) 0xc2, (byte) 0xcc, (byte) 0xcf, (byte) 0x8a, 0x3a, (byte) 0x86, 0x30, 0x28, (byte) 0xb5, (byte) 0xe1, (byte) 0xdc, 0x0a, 0x75, (byte) 0x86, 0x60, 0x2d, 0x25, 0x3c, (byte) 0xff, (byte) 0xf9, 0x1b, (byte) 0x82, 0x66, (byte) 0xbe, (byte) 0xa6, (byte) 0xd6, 0x1a, (byte) 0xb1 };

        Aes128Cbc algo      = new Aes128Cbc();
        byte[]    encrypted = null;

        ICryptoTransform encryptor = null;
        try {
            encryptor = algo.CreateEncryptor(CEK, IV, null, _provider);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            encrypted = encryptor.doFinal(PLAIN);
            
            // Assert: we only compare the first 32 bytes as this library uses PKCS7 padding
            assertArrayEquals(Arrays.copyOfRange(encrypted, 0, 32), ED);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ICryptoTransform decryptor = null;
        try {
            decryptor = algo.CreateDecryptor(CEK, IV, null, null, _provider);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        byte[] decrypted = null;

        try {
            decrypted = decryptor.doFinal(encrypted);
            
            // Assert: we only compare the first 32 bytes as this library uses PKCS7 padding
            assertArrayEquals(Arrays.copyOfRange(decrypted, 0, 32), PLAIN);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
