package com.microsoft.azure.keyvault.webkey.test;

import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.keyvault.webkey.JsonWebKey;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyOperation;

public class AesValidationTests {
    private static final String TRANSFORMATION = "AES";
    
    @Test
    public void aesKeyValidation() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(TRANSFORMATION);
        keyGen.init(256);
        
        SecretKey skey = keyGen.generateKey();        
        JsonWebKey key = serializeDeserialize(skey);
        Assert.assertTrue(key.hasPrivateKey());
        Assert.assertTrue(key.isValid());
        
        SecretKey secretKey = key.toAes();
        encryptDecrypt(secretKey);
        
        // Compare equal JSON web keys
        JsonWebKey sameKey = JsonWebKey.fromAes(skey);
        Assert.assertEquals(key, key);
        Assert.assertEquals(key, sameKey);
        Assert.assertEquals(key.hashCode(), sameKey.hashCode());
    }
    
    @Test
    public void invalidKeyOps() throws Exception {
        JsonWebKey key = getAes();
        key.withKeyOps(Arrays.asList(JsonWebKeyOperation.ENCRYPT, new JsonWebKeyOperation("foo")));
        Assert.assertFalse(key.isValid());
    }
    
    @Test
    public void octHashCode() throws Exception {
        JsonWebKey key = getAes();        

        // Compare hash codes for unequal JWK that would not map to the same hash
        Assert.assertNotEquals(key.hashCode(), new JsonWebKey().withK(key.k()).hashCode());
        Assert.assertNotEquals(key.hashCode(), new JsonWebKey().withKty(key.kty()).hashCode());
        
        // Compare hash codes for unequal JWK that would map to the same hash
        Assert.assertEquals(key.hashCode(), new JsonWebKey().withK(key.k()).withKty(key.kty()).hashCode());
    }
    
    private static JsonWebKey getAes() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(TRANSFORMATION);
        keyGen.init(256);
        
        SecretKey skey = keyGen.generateKey();        
        return JsonWebKey.fromAes(skey);
    }
    
    private static JsonWebKey serializeDeserialize(SecretKey skey) throws Exception {
        JsonWebKey webKey = JsonWebKey.fromAes(skey);
        String serializedKey = webKey.toString();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(serializedKey, JsonWebKey.class);
    }

    private static void encryptDecrypt(SecretKey key) throws Exception {
        byte[] plaintext = new byte[10];
        new Random().nextBytes(plaintext);
        byte[] cipherText = encrypt(key, plaintext);
        Assert.assertArrayEquals(decrypt(key, cipherText), plaintext);
    }

    private static byte[] encrypt(SecretKey key, byte[] plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);   
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(plaintext);
    }

    private static byte[] decrypt(SecretKey key, byte[] ciphertext) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);   
        cipher.init(Cipher.DECRYPT_MODE, key);  
        return cipher.doFinal(ciphertext);
    }
}
