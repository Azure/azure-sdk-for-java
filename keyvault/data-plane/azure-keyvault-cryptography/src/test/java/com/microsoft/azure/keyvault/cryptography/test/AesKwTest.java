/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.cryptography.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import javax.crypto.Cipher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.keyvault.cryptography.ICryptoTransform;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw128;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw192;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw256;

public class AesKwTest {

    // Always null for the default provider
    private Provider _provider = null;
    
    private static boolean hasUnlimitedCrypto() {
        try {
            return Cipher.getMaxAllowedKeyLength("RC5") >= 256;
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

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
    public void KeyVault_AesKw128() {
        // Arrange
        byte[] KEK = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F };
        byte[] CEK = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
        byte[] EK  = { 0x1F, (byte) 0xA6, (byte) 0x8B, 0x0A, (byte) 0x81, 0x12, (byte) 0xB4, 0x47, (byte) 0xAE, (byte) 0xF3, 0x4B, (byte) 0xD8, (byte) 0xFB, 0x5A, 0x7B, (byte) 0x82, (byte) 0x9D, 0x3E, (byte) 0x86, 0x23, 0x71, (byte) 0xD2, (byte) 0xCF, (byte) 0xE5 };

        AesKw kw = new AesKw128();

        ICryptoTransform encryptor = null;

        try {
            encryptor = kw.CreateEncryptor(KEK, _provider);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        byte[] encrypted = null;

        try {
            encrypted = encryptor.doFinal(CEK);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // Assert
        assertArrayEquals(EK, encrypted);

        ICryptoTransform decryptor = null;

        try {
            decryptor = kw.CreateDecryptor(KEK, _provider);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        byte[] decrypted = null;

        try {
            decrypted = decryptor.doFinal(EK);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // Assert
        assertArrayEquals(CEK, decrypted);
    }

    @Test
    public void KeyVault_AesKw192() {
        // Arrange
        byte[] KEK = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };
        byte[] CEK = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
        byte[] EK  = { (byte) 0x96, 0x77, (byte) 0x8B, 0x25, (byte) 0xAE, 0x6C, (byte) 0xA4, 0x35, (byte) 0xF9, 0x2B, 0x5B, (byte) 0x97, (byte) 0xC0, 0x50, (byte) 0xAE, (byte) 0xD2, 0x46, (byte) 0x8A, (byte) 0xB8, (byte) 0xA1, 0x7A, (byte) 0xD8, 0x4E, 0x5D };

        /*
         * This test using the default JCE provider depends on whether unlimited security
         * is installed or not. In the unlimited case, the full test should pass but in
         * the limited case, it should fail with InvalidKeyException.
         */
        boolean unlimited = hasUnlimitedCrypto();
        AesKw   kw        = new AesKw192();

        ICryptoTransform encryptor = null;

        try {
            encryptor = kw.CreateEncryptor(KEK, _provider);
            
            if (!unlimited) fail("Expected InvalidKeyException");
        } catch (InvalidKeyException e) {
            if (unlimited) fail("InvalidKeyException");
        } catch (Exception e) {
            fail(e.getMessage());
        }

        if (unlimited) {
            byte[] encrypted = null;
    
            try {
                encrypted = encryptor.doFinal(CEK);
            } catch (Exception e) {
                fail(e.getMessage());
            }
    
            // Assert
            assertArrayEquals(EK, encrypted);
        }

        ICryptoTransform decryptor = null;

        try {
            decryptor = kw.CreateDecryptor(KEK, _provider);
            if (!unlimited) fail("Expected InvalidKeyException");
        } catch (InvalidKeyException e) {
            if (unlimited) fail("InvalidKeyException");
        } catch (Exception e) {
            fail(e.getMessage());
        }

        if (unlimited) {
            byte[] decrypted = null;
    
            try {
                decrypted = decryptor.doFinal(EK);
            } catch (Exception e) {
                fail(e.getMessage());
            }
    
            // Assert
            assertArrayEquals(CEK, decrypted);
        }
    }

    @Test
    public void KeyVault_AesKw256() {
        // Arrange
        byte[] KEK = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F };
        byte[] CEK = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
        byte[] EK  = { 0x64, (byte) 0xE8, (byte) 0xC3, (byte) 0xF9, (byte) 0xCE, 0x0F, 0x5B, (byte) 0xA2, 0x63, (byte) 0xE9, 0x77, 0x79, 0x05, (byte) 0x81, (byte) 0x8A, 0x2A, (byte) 0x93, (byte) 0xC8, 0x19, 0x1E, 0x7D, 0x6E, (byte) 0x8A, (byte) 0xE7 };

        /*
         * This test using the default JCE provider depends on whether unlimited security
         * is installed or not. In the unlimited case, the full test should pass but in
         * the limited case, it should fail with InvalidKeyException.
         */
        boolean unlimited = hasUnlimitedCrypto();
        AesKw   kw        = new AesKw256();

        ICryptoTransform encryptor = null;

        try {
            encryptor = kw.CreateEncryptor(KEK, _provider);
            if (!unlimited) fail("Expected InvalidKeyException");
        } catch (InvalidKeyException e) {
            if (unlimited) fail("InvalidKeyException");
        } catch (Exception e) {
            fail(e.getMessage());
        }

        if (unlimited) {
            byte[] encrypted = null;
    
            try {
                encrypted = encryptor.doFinal(CEK);
            } catch (Exception e) {
                fail(e.getMessage());
            }
    
            // Assert
            assertArrayEquals(EK, encrypted);
        }

        ICryptoTransform decryptor = null;

        try {
            decryptor = kw.CreateDecryptor(KEK, _provider);
            
            if (!unlimited) fail("Expected InvalidKeyException");
        } catch (InvalidKeyException e) {
            if (unlimited) fail("InvalidKeyException");
        } catch (Exception e) {
            fail(e.getMessage());
        }

        if (unlimited) {
            byte[] decrypted = null;
    
            try {
                decrypted = decryptor.doFinal(EK);
            } catch (Exception e) {
                fail(e.getMessage());
            }
    
            // Assert
            assertArrayEquals(CEK, decrypted);
        }
    }

    @Test
    public void KeyVault_AesKw128_ExcessKeyMaterial() {
        // Arrange
        byte[] KEK = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F };
        byte[] CEK = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
        byte[] EK  = { 0x1F, (byte) 0xA6, (byte) 0x8B, 0x0A, (byte) 0x81, 0x12, (byte) 0xB4, 0x47, (byte) 0xAE, (byte) 0xF3, 0x4B, (byte) 0xD8, (byte) 0xFB, 0x5A, 0x7B, (byte) 0x82, (byte) 0x9D, 0x3E, (byte) 0x86, 0x23, 0x71, (byte) 0xD2, (byte) 0xCF, (byte) 0xE5 };

        AesKw kw = new AesKw128();

        ICryptoTransform encryptor = null;

        try {
            encryptor = kw.CreateEncryptor(KEK, _provider);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        byte[] encrypted = null;

        try {
            encrypted = encryptor.doFinal(CEK);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // Assert
        assertArrayEquals(EK, encrypted);

        ICryptoTransform decryptor = null;

        try {
            decryptor = kw.CreateDecryptor(KEK, _provider);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        byte[] decrypted = null;

        try {
            decrypted = decryptor.doFinal(EK);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // Assert
        assertArrayEquals(CEK, decrypted);
    }

    @Test
    public void KeyVault_AesKw192_ExcessKeyMaterial() {
        // Arrange
        byte[] KEK = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F };
        byte[] CEK = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
        byte[] EK  = { (byte) 0x96, 0x77, (byte) 0x8B, 0x25, (byte) 0xAE, 0x6C, (byte) 0xA4, 0x35, (byte) 0xF9, 0x2B, 0x5B, (byte) 0x97, (byte) 0xC0, 0x50, (byte) 0xAE, (byte) 0xD2, 0x46, (byte) 0x8A, (byte) 0xB8, (byte) 0xA1, 0x7A, (byte) 0xD8, 0x4E, 0x5D };

        /*
         * This test using the default JCE provider depends on whether unlimited security
         * is installed or not. In the unlimited case, the full test should pass but in
         * the limited case, it should fail with InvalidKeyException.
         */
        boolean unlimited = hasUnlimitedCrypto();
        AesKw   kw        = new AesKw192();

        ICryptoTransform encryptor = null;

        try {
            encryptor = kw.CreateEncryptor(KEK, _provider);
            
            if (!unlimited) fail("Expected InvalidKeyException");
        } catch (InvalidKeyException e) {
            if (unlimited) fail("InvalidKeyException");
        } catch (Exception e) {
            fail(e.getMessage());
        }

        if (unlimited) {
            byte[] encrypted = null;
    
            try {
                encrypted = encryptor.doFinal(CEK);
            } catch (Exception e) {
                fail(e.getMessage());
            }
    
            // Assert
            assertArrayEquals(EK, encrypted);
        }

        ICryptoTransform decryptor = null;

        try {
            decryptor = kw.CreateDecryptor(KEK, _provider);
            if (!unlimited) fail("Expected InvalidKeyException");
        } catch (InvalidKeyException e) {
            if (unlimited) fail("InvalidKeyException");
        } catch (Exception e) {
            fail(e.getMessage());
        }

        if (unlimited) {
            byte[] decrypted = null;
    
            try {
                decrypted = decryptor.doFinal(EK);
            } catch (Exception e) {
                fail(e.getMessage());
            }
    
            // Assert
            assertArrayEquals(CEK, decrypted);
        }
    }

    @Test
    public void KeyVault_AesKw256_ExcessKeyMaterial() {
        // Arrange
        byte[] KEK = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F };
        byte[] CEK = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
        byte[] EK  = { 0x64, (byte) 0xE8, (byte) 0xC3, (byte) 0xF9, (byte) 0xCE, 0x0F, 0x5B, (byte) 0xA2, 0x63, (byte) 0xE9, 0x77, 0x79, 0x05, (byte) 0x81, (byte) 0x8A, 0x2A, (byte) 0x93, (byte) 0xC8, 0x19, 0x1E, 0x7D, 0x6E, (byte) 0x8A, (byte) 0xE7 };

        /*
         * This test using the default JCE provider depends on whether unlimited security
         * is installed or not. In the unlimited case, the full test should pass but in
         * the limited case, it should fail with InvalidKeyException.
         */
        boolean unlimited = hasUnlimitedCrypto();
        AesKw   kw        = new AesKw256();

        ICryptoTransform encryptor = null;

        try {
            encryptor = kw.CreateEncryptor(KEK, _provider);
            if (!unlimited) fail("Expected InvalidKeyException");
        } catch (InvalidKeyException e) {
            if (unlimited) fail("InvalidKeyException");
        } catch (Exception e) {
            fail(e.getMessage());
        }

        if (unlimited) {
            byte[] encrypted = null;
    
            try {
                encrypted = encryptor.doFinal(CEK);
            } catch (Exception e) {
                fail(e.getMessage());
            }
    
            // Assert
            assertArrayEquals(EK, encrypted);
        }

        ICryptoTransform decryptor = null;

        try {
            decryptor = kw.CreateDecryptor(KEK, _provider);
            
            if (!unlimited) fail("Expected InvalidKeyException");
        } catch (InvalidKeyException e) {
            if (unlimited) fail("InvalidKeyException");
        } catch (Exception e) {
            fail(e.getMessage());
        }

        if (unlimited) {
            byte[] decrypted = null;
    
            try {
                decrypted = decryptor.doFinal(EK);
            } catch (Exception e) {
                fail(e.getMessage());
            }
    
            // Assert
            assertArrayEquals(CEK, decrypted);
        }
    }

}
