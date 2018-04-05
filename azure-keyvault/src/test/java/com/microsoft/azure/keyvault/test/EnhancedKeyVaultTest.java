/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.test;

import com.microsoft.azure.keyvault.messagesecurity.JWEObject;
import com.microsoft.azure.keyvault.messagesecurity.JWEHeader;
import com.microsoft.azure.keyvault.messagesecurity.JWSHeader;
import com.microsoft.azure.keyvault.messagesecurity.JWSObject;
import org.junit.Assert;
import org.junit.Test;


public class EnhancedKeyVaultTest  {
    
    @Test
    public void equalityTest() throws Exception {
        JWEObject jweObject1 = new JWEObject(new JWEHeader("test alg","test kid","test enc"),"test enc key","test iv","test cipher","test tag");
        JWEObject jweObject2 = new JWEObject(new JWEHeader("test alg","test kid","test enc"),"test enc key","test iv","test cipher","test tag");
        JWEObject jweObject3 = new JWEObject(new JWEHeader("different alg","different kid","test enc"),"different enc key","different iv","different cipher","different tag");

        Assert.assertTrue(jweObject1.equals(jweObject2));
        Assert.assertFalse(jweObject1.equals(jweObject3));

        JWSObject jwsObject1 = new JWSObject(new JWSHeader("test alg","test kid","test at",1,"test p","test typ"),"test payload","test signature");
        JWSObject jwsObject2 = new JWSObject(new JWSHeader("test alg","test kid","test at",1,"test p","test typ"),"test payload","test signature");
        JWSObject jwsObject3 = new JWSObject(new JWSHeader("different alg","different kid","different at",2,"different p","different typ"),"different payload","different signature");

        Assert.assertTrue(jwsObject1.equals(jwsObject2));
        Assert.assertFalse(jwsObject1.equals(jwsObject3));

        jweObject1 = new JWEObject(new JWEHeader("test alg","test kid","test enc"),"test enc key","test iv","test cipher","test tag");
        jweObject2 = new JWEObject(new JWEHeader("different alg","test kid","test enc"),"test enc key","test iv","test cipher","test tag");

        Assert.assertFalse(jweObject1.equals(jweObject2));

        jwsObject1 = new JWSObject(new JWSHeader("test alg","test kid","test at",1,"test p","test typ"),"test payload","test signature");
        jwsObject2 = new JWSObject(new JWSHeader("different alg","test kid","test at",1,"test p","test typ"),"test payload","test signature");

        Assert.assertFalse(jwsObject1.equals(jwsObject2));
    }

    @Test
    public void serializationTest() throws Exception {
        JWEObject jweObject1 = new JWEObject(new JWEHeader("test alg","test kid","test enc"),"test enc key","test iv","test cipher","test tag");
        String serialized = jweObject1.serialize();
        JWEObject jweObject2 = JWEObject.deserialize(serialized);
        Assert.assertTrue(jweObject1.equals(jweObject2));

        JWSObject jwsObject1 = new JWSObject(new JWSHeader("test alg","test kid","test at",1,"test p","test typ"),"test payload","test signature");
        serialized = jwsObject1.serialize();
        JWSObject jwsObject2 = JWSObject.deserialize(serialized);
        Assert.assertTrue(jwsObject1.equals(jwsObject2));
    }

    @Test
    public void payloadTests() throws Exception {

    }
}
