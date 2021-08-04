// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation.models;

import com.azure.security.attestation.AttestationClientTestBase;
import com.azure.security.attestation.implementation.models.AttestationResult;
import com.azure.security.attestation.implementation.models.AttestationTokenImpl;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.security.auth.x500.X500Principal;

final class TestObject {
    @JsonProperty(value = "alg")
    private String alg;

    public TestObject setAlg(String v) {
        alg = v;
        return this;
    }

    public String getAlg() {
        return alg;
    }

    @JsonProperty(value = "int")
    private int integer;
    public TestObject setInteger(int v) {
        integer = v;
        return this;
    }
    public int getInteger() {
        return integer;
    }


    @JsonProperty(value = "intArray")
    private int[] integerArray;
    public TestObject setIntegerArray(int[] v) {
        integerArray = v.clone();
        return this;
    }
    public int[] getIntegerArray() {
        return integerArray;
    }

    TestObject() {

    }
}


/**
 * Test for Attestation Signing Certificates APIs.
 */
public class AttestationTokenTests extends AttestationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

//    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @Test
    void testCreateSigningKeyRSA() {
        KeyPair rsaKey = assertDoesNotThrow(() -> createKeyPair("RSA"));
        X509Certificate cert = assertDoesNotThrow(() -> createSelfSignedCertificate("Test Certificate", rsaKey));

        AttestationSigningKey signingKey = new AttestationSigningKey()
            .setPrivateKey(rsaKey.getPrivate())
            .setCertificate(cert);

        assertDoesNotThrow(() -> signingKey.verify());

    }

    @Test
    void testCreateSigningKeyECDS() {
        KeyPair rsaKey = assertDoesNotThrow(() -> createKeyPair("EC"));
        X509Certificate cert = assertDoesNotThrow(() -> createSelfSignedCertificate("Test Certificate", rsaKey));

        AttestationSigningKey signingKey = new AttestationSigningKey()
            .setPrivateKey(rsaKey.getPrivate())
            .setCertificate(cert);

        assertDoesNotThrow(() -> signingKey.verify());
    }

    @Test
    void testCreateSigningKeyWrongKey() {
        KeyPair rsaKey = assertDoesNotThrow(() -> createKeyPair("EC"));
        X509Certificate cert = assertDoesNotThrow(() -> createSelfSignedCertificate("Test Certificate", rsaKey));

        // Wrong key family
        KeyPair rsaKeyWrongFamily = assertDoesNotThrow(() -> createKeyPair("RSA"));

        // Wrong key family
        KeyPair rsaKeyWrongKey = assertDoesNotThrow(() -> createKeyPair("EC"));

        // Create a signing key with a private key that does not match the certificate.
        AttestationSigningKey signingKey = new AttestationSigningKey()
            .setPrivateKey(rsaKeyWrongFamily.getPrivate())
            .setCertificate(cert);

        // The wrong key family should result in an InvalidKey exception.
        assertThrows(InvalidKeyException.class, () -> signingKey.verify());

        // And make sure that the wrong key also throws a reasonable exception.
        signingKey.setPrivateKey(rsaKeyWrongKey.getPrivate());
        assertThrows(IllegalArgumentException.class, () -> signingKey.verify());
    }

    @Test
    void testCreateUnsecuredAttestationToken() {
        String sourceObject = "{\"foo\": \"foo\", \"bar\": 10 }";
        AttestationToken newToken = AttestationTokenImpl.createUnsecuredToken(sourceObject);

        // Verify that this is an unsecured attestation token.
        assertEquals("none", newToken.getAlgorithm());
        assertNull(newToken.getKeyId());

        Object jsonValue = newToken.getBody(Object.class);
        assertTrue(jsonValue instanceof LinkedHashMap);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> jsonMap = (LinkedHashMap<String, Object>) jsonValue;
        assertNotNull(jsonMap);
        assertEquals(2, jsonMap.size());
        assertEquals("foo", jsonMap.get("foo"));
        assertEquals(10, jsonMap.get("bar"));
    }

    @Test
    void testCreateUnsecuredAttestationTokenFromObject() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        TestObject testObject = new TestObject()
            .setAlg("Test Algorithm")
            .setInteger(31415926)
            .setIntegerArray(new int[]{123, 456, 789});
        String objectString = assertDoesNotThrow(() -> mapper.writeValueAsString(testObject));

        AttestationToken newToken = AttestationTokenImpl.createUnsecuredToken(objectString);

        assertEquals("none", newToken.getAlgorithm());
        TestObject object = newToken.getBody(TestObject.class);

        assertEquals("Test Algorithm", object.getAlg());
        assertEquals(31415926, object.getInteger());
        assertArrayEquals(new int[]{123, 456, 789}, object.getIntegerArray());
    }

    @Test
    void testCreateUnsecuredEmptyAttestationToken() {
        AttestationToken newToken = AttestationTokenImpl.createUnsecuredToken();

        // Verify that this is an unsecured attestation token.
        assertEquals("none", newToken.getAlgorithm());

        Object jsonValue = newToken.getBody(Object.class);
        assertNull(jsonValue);
    }

    @Test
    void testCreateSecuredAttestationToken() {
        KeyPair rsaKey = assertDoesNotThrow(() -> createKeyPair("RSA"));
        X509Certificate cert = assertDoesNotThrow(() -> createSelfSignedCertificate("Test Certificate Secured", rsaKey));

        AttestationSigningKey signingKey = new AttestationSigningKey()
            .setPrivateKey(rsaKey.getPrivate())
            .setCertificate(cert)
            .setAllowWeakKey(true);

        String sourceObject = "{\"foo\": \"foo\", \"bar\": 10 }";

        AttestationToken newToken = AttestationTokenImpl.createSecuredToken(sourceObject, signingKey);

        // Verify that this is a secured attestation token.
        assertNotEquals("none", newToken.getAlgorithm());
        assertNull(newToken.getKeyId());
        assertNotNull(newToken.getCertificateChain());
        assertArrayEquals(assertDoesNotThrow(() -> cert.getEncoded()), assertDoesNotThrow(() -> newToken.getCertificateChain().getCertificates()[0].getEncoded()));

        Object jsonValue = newToken.getBody(Object.class);
        assertTrue(jsonValue instanceof  LinkedHashMap);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> jsonMap = assertDoesNotThrow(() -> (LinkedHashMap<String, Object>) jsonValue);
        assertNotNull(jsonMap);
        assertEquals(2, jsonMap.size());
        assertEquals("foo", jsonMap.get("foo"));
        assertEquals(10, jsonMap.get("bar"));
    }

    @Test
    void testCreateSecuredAttestationTokenFromObject() {
        KeyPair rsaKey = assertDoesNotThrow(() -> createKeyPair("RSA"));
        X509Certificate cert = assertDoesNotThrow(() -> createSelfSignedCertificate("Test Certificate Secured 2", rsaKey));
        AttestationSigningKey signingKey = new AttestationSigningKey()
            .setPrivateKey(rsaKey.getPrivate())
            .setCertificate(cert)
            .setAllowWeakKey(true);


        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        TestObject testObject = new TestObject()
            .setAlg("Test Algorithm")
            .setInteger(31415926)
            .setIntegerArray(new int[]{123, 456, 789});

        String objectString = assertDoesNotThrow(() -> mapper.writeValueAsString(testObject));

        AttestationToken newToken = AttestationTokenImpl.createSecuredToken(objectString, signingKey);

        assertNotEquals("none", newToken.getAlgorithm());
        TestObject object = newToken.getBody(TestObject.class);

        assertEquals("Test Algorithm", object.getAlg());
        assertEquals(31415926, object.getInteger());
        assertArrayEquals(new int[]{123, 456, 789}, object.getIntegerArray());
    }

    @Test
    void testCreateSecuredEmptyAttestationToken() {
        KeyPair rsaKey = assertDoesNotThrow(() -> createKeyPair("RSA"));
        X509Certificate cert = assertDoesNotThrow(() -> createSelfSignedCertificate("Test Certificate Secured 2", rsaKey));
        AttestationSigningKey signingKey = new AttestationSigningKey()
            .setPrivateKey(rsaKey.getPrivate())
            .setCertificate(cert)
            .setAllowWeakKey(true);

        AttestationToken newToken = AttestationTokenImpl.createSecuredToken(signingKey);

        // This had better be a signed token.
        assertNotEquals("none", newToken.getAlgorithm());
        assertNull(newToken.getKeyId());
        assertNotNull(newToken.getCertificateChain());
        assertArrayEquals(assertDoesNotThrow(() -> cert.getEncoded()), assertDoesNotThrow(() -> newToken.getCertificateChain().getCertificates()[0].getEncoded()));

        Object jsonValue = newToken.getBody(Object.class);
        assertNull(jsonValue);
    }

    @Test
    void testCreateAttestationToken() {
        String tokenToTest = "eyJhbGciOiJSUzI1NiIsImprdSI6Imh0dHBzOi8vamFsYXJyeW9hdHRlc3"
            + "RhdGlvbmFhZC53dXMuYXR0ZXN0LmF6dXJlLm5ldC9jZXJ0cyIsImtpZCI6InJQdEJHUldUbFBtenM1d"
            + "TM1TDBRUkQ5V2R5bWVLUFJ4dGVUZVRsd04wRWM9IiwidHlwIjoiSldUIn0.eyJhYXMtZWhkIjoiQ2lB"
            + "Z0lDQWdJQ0FnZXdvZ0lDQWdJQ0FnSUNBZ0lDQWlhbmRySWlBNklIc0tJQ0FnSUNBZ0lDQWdJQ0FnSUN"
            + "BZ0lDSnJkSGtpT2lKRlF5SXNDaUFnSUNBZ0lDQWdJQ0FnSUNBZ0lDQWlkWE5sSWpvaWMybG5JaXdLSU"
            + "NBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0pqY25ZaU9pSlFMVEkxTmlJc0NpQWdJQ0FnSUNBZ0lDQWdJQ0FnS"
            + "UNBaWVDSTZJakU0ZDBoTVpVbG5WemwzVms0MlZrUXhWSGhuY0hGNU1reHplbGxyVFdZMlNqaHVhbFpC"
            + "YVdKMmFFMGlMQW9nSUNBZ0lDQWdJQ0FnSUNBZ0lDQWdJbmtpT2lKalZqUmtVelJWWVV4TloxQmZOR1p"
            + "aTkdvNGFYSTNZMnd4VkZoc1JtUkJaMk40TlRWdk4xUnJZMU5CSWdvZ0lDQWdJQ0FnSUNBZ0lDQjlDaU"
            + "FnSUNBZ0lDQWdmUW9nSUNBZ0lDQWdJQSIsImV4cCI6MTYyNjkzMDQ5NiwiaWF0IjoxNjI2OTAxNjk2L"
            + "CJpcy1kZWJ1Z2dhYmxlIjpmYWxzZSwiaXNzIjoiaHR0cHM6Ly9qYWxhcnJ5b2F0dGVzdGF0aW9uYWFk"
            + "Lnd1cy5hdHRlc3QuYXp1cmUubmV0IiwianRpIjoiMDU0ODdiMzM1MzE3MmRhOTAxMzMxNGYxZTllYjF"
            + "jYzdkYzJiM2JjY2MwNTQyOTFkZjRjMzQ0ZWYzOWY5YmNkNCIsIm1hYS1hdHRlc3RhdGlvbmNvbGxhdG"
            + "VyYWwiOnsicWVpZGNlcnRzaGFzaCI6ImE2NGQ2NDkxOTg1MDdkOGI1N2UzM2Y2M2FiMjY2ODM4ZjQzZ"
            + "jMyN2JkNGFhY2M3ODUxMGI2OTc2ZWQwNDZlMTAiLCJxZWlkY3JsaGFzaCI6IjA1OTc2MmU3MjZlODAz"
            + "N2ExZGY1YmE1MTM2YTQzNDU3ZmEyMjgzYTM4ZjVmYzI3MTRlMjlhYWZiNjQ1NTQxMzgiLCJxZWlkaGF"
            + "zaCI6Ijc3MDFmNjQ3MDBiN2Y1MDVkN2I0YjdhOTNlNDVkNWNkZThjZmM4NjViNjBmMWRkNDllY2JlZT"
            + "k3OTBjMzM3MmUiLCJxdW90ZWhhc2giOiJhZjBlMDU2MzFjMTQ1ZDdkYzFhMzNiMjE0ZDQzYzZhYTM0Y"
            + "Tc4NTk2MjJhMGFjN2Q4ODBhYjdiMjRlMmYxOGQyIiwidGNiaW5mb2NlcnRzaGFzaCI6ImE2NGQ2NDkx"
            + "OTg1MDdkOGI1N2UzM2Y2M2FiMjY2ODM4ZjQzZjMyN2JkNGFhY2M3ODUxMGI2OTc2ZWQwNDZlMTAiLCJ"
            + "0Y2JpbmZvY3JsaGFzaCI6IjA1OTc2MmU3MjZlODAzN2ExZGY1YmE1MTM2YTQzNDU3ZmEyMjgzYTM4Zj"
            + "VmYzI3MTRlMjlhYWZiNjQ1NTQxMzgiLCJ0Y2JpbmZvaGFzaCI6IjgyZDEwOWZiMzA4ZjI0YTkwZTQzO"
            + "TM2ZWE5ZTEyYjU1YjA1MjUwMjIxZmRhMjI5NGY3NGFiNTgxN2U3MWJlYTQifSwibWFhLWVoZCI6IkNp"
            + "QWdJQ0FnSUNBZ2V3b2dJQ0FnSUNBZ0lDQWdJQ0FpYW5kcklpQTZJSHNLSUNBZ0lDQWdJQ0FnSUNBZ0l"
            + "DQWdJQ0pyZEhraU9pSkZReUlzQ2lBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FpZFhObElqb2ljMmxuSWl3S0"
            + "lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNKamNuWWlPaUpRTFRJMU5pSXNDaUFnSUNBZ0lDQWdJQ0FnSUNBZ"
            + "0lDQWllQ0k2SWpFNGQwaE1aVWxuVnpsM1ZrNDJWa1F4VkhobmNIRjVNa3h6ZWxsclRXWTJTamh1YWxa"
            + "QmFXSjJhRTBpTEFvZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSW5raU9pSmpWalJrVXpSVllVeE5aMUJmTkd"
            + "aWk5HbzRhWEkzWTJ3eFZGaHNSbVJCWjJONE5UVnZOMVJyWTFOQklnb2dJQ0FnSUNBZ0lDQWdJQ0I5Q2"
            + "lBZ0lDQWdJQ0FnZlFvZ0lDQWdJQ0FnSUEiLCJuYmYiOjE2MjY5MDE2OTYsInByb2R1Y3QtaWQiOjEsI"
            + "nNneC1tcmVuY2xhdmUiOiJiNzc5MjAyNjE4YmVjYjYzNjc2OGU1ZDkwMmYyOGQ1MjQyMjI3MzVmYWZi"
            + "NTM5ZWE5YmEwN2MzZmMxNzFhMWMyIiwic2d4LW1yc2lnbmVyIjoiMmMxYTQ0OTUyYWU4MjA3MTM1YzZ"
            + "jMjliNzViOGMwMjkzNzJlZTk0YjY3N2UxNWMyMGJkNDIzNDBmMTBkNDFhYSIsInN2biI6MSwidGVlIj"
            + "oic2d4IiwieC1tcy1hdHRlc3RhdGlvbi10eXBlIjoic2d4IiwieC1tcy1wb2xpY3ktaGFzaCI6ImJST"
            + "3JOODk3WmZXbVdZMlYwRkxVbHUzdXdrNXhhdEduS1lIbjN6QmJTbHciLCJ4LW1zLXNneC1jb2xsYXRl"
            + "cmFsIjp7InFlaWRjZXJ0c2hhc2giOiJhNjRkNjQ5MTk4NTA3ZDhiNTdlMzNmNjNhYjI2NjgzOGY0M2Y"
            + "zMjdiZDRhYWNjNzg1MTBiNjk3NmVkMDQ2ZTEwIiwicWVpZGNybGhhc2giOiIwNTk3NjJlNzI2ZTgwMz"
            + "dhMWRmNWJhNTEzNmE0MzQ1N2ZhMjI4M2EzOGY1ZmMyNzE0ZTI5YWFmYjY0NTU0MTM4IiwicWVpZGhhc"
            + "2giOiI3NzAxZjY0NzAwYjdmNTA1ZDdiNGI3YTkzZTQ1ZDVjZGU4Y2ZjODY1YjYwZjFkZDQ5ZWNiZWU5"
            + "NzkwYzMzNzJlIiwicXVvdGVoYXNoIjoiYWYwZTA1NjMxYzE0NWQ3ZGMxYTMzYjIxNGQ0M2M2YWEzNGE"
            + "3ODU5NjIyYTBhYzdkODgwYWI3YjI0ZTJmMThkMiIsInRjYmluZm9jZXJ0c2hhc2giOiJhNjRkNjQ5MT"
            + "k4NTA3ZDhiNTdlMzNmNjNhYjI2NjgzOGY0M2YzMjdiZDRhYWNjNzg1MTBiNjk3NmVkMDQ2ZTEwIiwid"
            + "GNiaW5mb2NybGhhc2giOiIwNTk3NjJlNzI2ZTgwMzdhMWRmNWJhNTEzNmE0MzQ1N2ZhMjI4M2EzOGY1"
            + "ZmMyNzE0ZTI5YWFmYjY0NTU0MTM4IiwidGNiaW5mb2hhc2giOiI4MmQxMDlmYjMwOGYyNGE5MGU0Mzk"
            + "zNmVhOWUxMmI1NWIwNTI1MDIyMWZkYTIyOTRmNzRhYjU4MTdlNzFiZWE0In0sIngtbXMtc2d4LWVoZC"
            + "I6IkNpQWdJQ0FnSUNBZ2V3b2dJQ0FnSUNBZ0lDQWdJQ0FpYW5kcklpQTZJSHNLSUNBZ0lDQWdJQ0FnS"
            + "UNBZ0lDQWdJQ0pyZEhraU9pSkZReUlzQ2lBZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FpZFhObElqb2ljMmxu"
            + "SWl3S0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSUNKamNuWWlPaUpRTFRJMU5pSXNDaUFnSUNBZ0lDQWdJQ0F"
            + "nSUNBZ0lDQWllQ0k2SWpFNGQwaE1aVWxuVnpsM1ZrNDJWa1F4VkhobmNIRjVNa3h6ZWxsclRXWTJTam"
            + "h1YWxaQmFXSjJhRTBpTEFvZ0lDQWdJQ0FnSUNBZ0lDQWdJQ0FnSW5raU9pSmpWalJrVXpSVllVeE5aM"
            + "UJmTkdaWk5HbzRhWEkzWTJ3eFZGaHNSbVJCWjJONE5UVnZOMVJyWTFOQklnb2dJQ0FnSUNBZ0lDQWdJ"
            + "Q0I5Q2lBZ0lDQWdJQ0FnZlFvZ0lDQWdJQ0FnSUEiLCJ4LW1zLXNneC1pcy1kZWJ1Z2dhYmxlIjpmYWx"
            + "zZSwieC1tcy1zZ3gtbXJlbmNsYXZlIjoiYjc3OTIwMjYxOGJlY2I2MzY3NjhlNWQ5MDJmMjhkNTI0Mj"
            + "IyNzM1ZmFmYjUzOWVhOWJhMDdjM2ZjMTcxYTFjMiIsIngtbXMtc2d4LW1yc2lnbmVyIjoiMmMxYTQ0O"
            + "TUyYWU4MjA3MTM1YzZjMjliNzViOGMwMjkzNzJlZTk0YjY3N2UxNWMyMGJkNDIzNDBmMTBkNDFhYSIs"
            + "IngtbXMtc2d4LXByb2R1Y3QtaWQiOjEsIngtbXMtc2d4LXJlcG9ydC1kYXRhIjoiZWQxMWE5ZWI5N2R"
            + "mYzE3MDUzYTMyYTY5OTJlNzFkMDRmN2VjOTk2ZTQ1NTE5YWI3MTA5MzZhZmI0OGY5MDQxZDAwMDAwMD"
            + "AwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAiL"
            + "CJ4LW1zLXNneC1zdm4iOjEsIngtbXMtdmVyIjoiMS4wIn0.rtIZSNTFYEohw0_EKHzUvizqmoPgxyjI"
            + "OsEUfnds5jry3dLNrPEvmrvZqEpAxwOgY0vdBCftMqlAt1dZ4rHRwyTi0OLGIFpwPtzJ9S1Na3sSfaG"
            + "xwL68gz1WFXe7rglkC4i2ehQFFVgoGvj2c_VF-7p0r3R6FP3lyk5xxXXqhujbnWVVxEpBUTWL-5mku1"
            + "u9TXGoYFaJlsnuSl85qdWuDmqxjzMDZ9co36xhimUeCSbvvBkokS2NqWmlrMXTDyhIxQZc7nporYyUx"
            + "z0DESiy9PLZQ8M9zDLGY0sheCvWc4dsSQcq2j6HSh85W7gEGLck5aJaMEyQ9nGsVGSIwJu1Yg";

        AttestationToken token = new AttestationTokenImpl(tokenToTest);

        assertNotEquals("none", token.getAlgorithm());
        assertEquals("https://jalarryoattestationaad.wus.attest.azure.net/certs", token.getJsonWebKeyUrl().toString());
        assertEquals("rPtBGRWTlPmzs5u35L0QRD9WdymeKPRxteTeTlwN0Ec=", token.getKeyId());
        assertEquals("JWT", token.getType());

        com.azure.security.attestation.implementation.models.AttestationResult attestResult;

        attestResult = token.getBody(AttestationResult.class);
        assertEquals("1.0", attestResult.getVersion());
        assertEquals("sgx", attestResult.getVerifierType());

    }


    KeyPair createKeyPair(String algorithm) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
        if (algorithm.equals("RSA")) {
            keyGen.initialize(1024); // Generate a reasonably strong key.
        }
        return keyGen.generateKeyPair();
    }

    X509Certificate createSelfSignedCertificate(String subjectName, KeyPair certificateKey) throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        final X509V3CertificateGenerator generator = new X509V3CertificateGenerator();
        generator.setIssuerDN(new X500Principal("CN=" + subjectName));
        generator.setSubjectDN(new X500Principal("CN=" + subjectName));
        generator.setPublicKey(certificateKey.getPublic());
        if (certificateKey.getPublic().getAlgorithm().equals("EC")) {
            generator.setSignatureAlgorithm("SHA256WITHECDSA");
        } else {
            generator.setSignatureAlgorithm("SHA256WITHRSA");
        }
        generator.setSerialNumber(BigInteger.valueOf(Math.abs(new Random().nextInt())));
        // Valid from now to 1 day from now.
        generator.setNotBefore(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        generator.setNotAfter(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().plus(1, ChronoUnit.DAYS)));

        generator.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
        return generator.generate(certificateKey.getPrivate());

    }

}

