// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation.models;

import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.security.attestation.AttestationClientTestBase;
import com.azure.security.attestation.implementation.models.AttestationResult;
import com.azure.security.attestation.implementation.models.AttestationResultImpl;
import com.azure.security.attestation.implementation.models.AttestationTokenImpl;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test for Attestation Signing Certificates APIs.
 */
public class AttestationTokenTests extends AttestationClientTestBase {
    @Test
    void testCreateSigningKeyRSA() {
        KeyPair rsaKey = assertDoesNotThrow(() -> createKeyPair("RSA"));
        X509Certificate cert = assertDoesNotThrow(() -> createSelfSignedCertificate("Test Certificate", rsaKey));

        AttestationSigningKey signingKey = new AttestationSigningKey(cert, rsaKey.getPrivate());

        assertDoesNotThrow(signingKey::verify);

    }


    /* Commented out until issue https://github.com/Azure/azure-sdk-for-java/issues/21776 is fixed
    @Test
    void testCreateSigningKeyECDS() {
        KeyPair rsaKey = assertDoesNotThrow(() -> createKeyPair("EC"));
        X509Certificate cert = assertDoesNotThrow(() -> createSelfSignedCertificate("Test Certificate", rsaKey));

        AttestationSigningKey signingKey = new AttestationSigningKey()
            .setPrivateKey(rsaKey.getPrivate())
            .setCertificate(cert);

        assertDoesNotThrow(() -> signingKey.verify());
    }
*/

    @Test
    void testCreateSigningKeyWrongKey() {

        KeyPair rsaKey = assertDoesNotThrow(() -> createKeyPair("RSA"));
        X509Certificate cert = assertDoesNotThrow(() -> createSelfSignedCertificate("Test Certificate", rsaKey));

        /* https://github.com/Azure/azure-sdk-for-java/issues/21776
        // Wrong key family
        KeyPair ecKeyWrongFamily = assertDoesNotThrow(() -> createKeyPair("EC"));
*/
        // Wrong key family
        KeyPair rsaKeyWrongKey = assertDoesNotThrow(() -> createKeyPair("RSA"));

        /* https://github.com/Azure/azure-sdk-for-java/issues/21776
        // Create a signing key with a private key that does not match the certificate.
        AttestationSigningKey signingKey1 = new AttestationSigningKey()
            .setPrivateKey(ecKeyWrongFamily.getPrivate())
            .setCertificate(cert);

        // The wrong key family should result in an InvalidKey exception.
        assertThrows(InvalidKeyException.class, () -> signingKey1.verify());
*/

        // And make sure that the wrong key also throws a reasonable exception.
        AttestationSigningKey signingKey2 = new AttestationSigningKey(cert, rsaKeyWrongKey.getPrivate())
                .setWeakKeyAllowed(true);
        assertThrows(IllegalArgumentException.class, signingKey2::verify);
    }

    @Test
    void testCreateUnsecuredAttestationToken() {
        String sourceObject = "{\"foo\": \"foo\", \"bar\": 10 }";
        AttestationToken newToken = AttestationTokenImpl.createUnsecuredToken(sourceObject);

        // Verify that this is an unsecured attestation token.
        assertEquals("none", newToken.getAlgorithm());
        assertNull(newToken.getKeyId());

        Object jsonValue = newToken.getBody(Object.class);
        assertInstanceOf(LinkedHashMap.class, jsonValue);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> jsonMap = (LinkedHashMap<String, Object>) jsonValue;
        assertNotNull(jsonMap);
        assertEquals(2, jsonMap.size());
        assertEquals("foo", jsonMap.get("foo"));
        assertEquals(10, jsonMap.get("bar"));
    }

    @Test
    void testCreateUnsecuredAttestationTokenFromObject() {
        TestObject testObject = new TestObject()
            .setAlg("Test Algorithm")
            .setInteger(31415926)
            .setIntegerArray(new int[]{123, 456, 789});
        String objectString = assertDoesNotThrow(() -> ADAPTER.serialize(testObject, SerializerEncoding.JSON));

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

        AttestationSigningKey signingKey = new AttestationSigningKey(cert, rsaKey.getPrivate())
            .setWeakKeyAllowed(true);

        String sourceObject = "{\"foo\": \"foo\", \"bar\": 10 }";

        AttestationToken newToken = AttestationTokenImpl.createSecuredToken(sourceObject, signingKey);

        // Verify that this is a secured attestation token.
        assertNotEquals("none", newToken.getAlgorithm());
        assertNull(newToken.getKeyId());
        assertNotNull(newToken.getCertificateChain());
        assertArrayEquals(assertDoesNotThrow(cert::getEncoded),
            assertDoesNotThrow(() -> newToken.getCertificateChain().getCertificates().get(0).getEncoded()));

        Object jsonValue = newToken.getBody(Object.class);
        assertInstanceOf(LinkedHashMap.class, jsonValue);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> jsonMap = assertDoesNotThrow(() -> (LinkedHashMap<String, Object>) jsonValue);
        assertNotNull(jsonMap);
        assertEquals(2, jsonMap.size());
        assertEquals("foo", jsonMap.get("foo"));
        assertEquals(10, jsonMap.get("bar"));
    }

    @Test
    void testVerifySignerWithPredefinedPayload() {
        PrivateKey key = getIsolatedSigningKey();
        X509Certificate cert = getIsolatedSigningCertificate();

        AttestationSigningKey signingKey = new AttestationSigningKey(cert, key);
        assertDoesNotThrow(signingKey::verify);

        String sourceObject = "{\"foo\": \"foo\", \"bar\": 10 }";

        AttestationToken newToken = AttestationTokenImpl.createSecuredToken(sourceObject, signingKey);

        // Verify that this is a secured attestation token.
        assertNotEquals("none", newToken.getAlgorithm());
        assertNull(newToken.getKeyId());
        assertNotNull(newToken.getCertificateChain());
        assertArrayEquals(assertDoesNotThrow(cert::getEncoded),
            assertDoesNotThrow(() -> newToken.getCertificateChain().getCertificates().get(0).getEncoded()));

    }


    @Test
    void testVerifySignerWithPredefinedPayloadFail() {
        PrivateKey key = getIsolatedSigningKey();
        X509Certificate cert = getPolicySigningCertificate0();

        AttestationSigningKey signingKey = new AttestationSigningKey(cert, key);

        assertThrows(RuntimeException.class, signingKey::verify);

    }

    @Test
    void testCreateSecuredAttestationTokenFromObject() {
        KeyPair rsaKey = assertDoesNotThrow(() -> createKeyPair("RSA"));
        X509Certificate cert = assertDoesNotThrow(() ->
            createSelfSignedCertificate("Test Certificate Secured 2", rsaKey));
        AttestationSigningKey signingKey = new AttestationSigningKey(cert, rsaKey.getPrivate())
            .setWeakKeyAllowed(true);

        OffsetDateTime timeNow = OffsetDateTime.now();
        timeNow = timeNow.minusNanos(timeNow.getNano());

        TestObject testObject = new TestObject()
            .setAlg("Test Algorithm")
            .setInteger(31415926)
            .setIntegerArray(new int[]{123, 456, 789})
            .setIssuedOn(timeNow)
            .setNotBefore(timeNow)
            .setExpiresOn(timeNow.plusSeconds(30))
            .setIssuer("Fred");
        String objectString = assertDoesNotThrow(() -> ADAPTER.serialize(testObject, SerializerEncoding.JSON));

        AttestationToken newToken = AttestationTokenImpl.createSecuredToken(objectString, signingKey);

        assertNotEquals("none", newToken.getAlgorithm());
        TestObject object = newToken.getBody(TestObject.class);

        assertEquals("Test Algorithm", object.getAlg());
        assertEquals(31415926, object.getInteger());
        assertArrayEquals(new int[]{123, 456, 789}, object.getIntegerArray());
        // Times in an attestation token
        assertTrue(timeNow.isEqual(newToken.getIssuedAt()));
        assertTrue(timeNow.isEqual(newToken.getNotBefore()));
        assertTrue(timeNow.plusSeconds(30).isEqual(newToken.getExpiresOn()));
        assertEquals("Fred", newToken.getIssuer());

        assertDoesNotThrow(() -> ((AttestationTokenImpl) newToken).validate(null, new AttestationTokenValidationOptions()));
    }

    @Test
    void verifyAttestationTokenIssuer() {
        OffsetDateTime timeNow = OffsetDateTime.now();
        timeNow = timeNow.minusNanos(timeNow.getNano());

        TestObject testObject = new TestObject()
            .setAlg("Test Algorithm")
            .setInteger(31415926)
            .setIntegerArray(new int[]{123, 456, 789})
            .setIssuedOn(timeNow)
            .setNotBefore(timeNow)
            .setExpiresOn(timeNow.plusSeconds(30))
            .setIssuer("Fred");

        String objectString = assertDoesNotThrow(() -> ADAPTER.serialize(testObject, SerializerEncoding.JSON));

        AttestationToken newToken = AttestationTokenImpl.createUnsecuredToken(objectString);

        assertEquals("none", newToken.getAlgorithm());
        TestObject object = newToken.getBody(TestObject.class);

        assertEquals("Test Algorithm", object.getAlg());
        assertEquals(31415926, object.getInteger());
        assertArrayEquals(new int[]{123, 456, 789}, object.getIntegerArray());
        assertEquals("Fred", newToken.getIssuer());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            ((AttestationTokenImpl) newToken).validate(null,
                new AttestationTokenValidationOptions()
                    .setExpectedIssuer("Joe")));
        // Both the actual and expected issuers should be in the exception message.
        assertTrue(ex.getMessage().contains("Fred"));
        assertTrue(ex.getMessage().contains("Joe"));
    }

    @Test
    void verifyAttestationTokenExpireTimeout() {
        final OffsetDateTime timeNow = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);

        TestObject testObjectExpired30SecondsAgo = new TestObject()
            .setAlg("Test Algorithm")
            .setInteger(31415926)
            .setIntegerArray(new int[]{123, 456, 789})
            .setIssuedOn(timeNow.minusSeconds(60))
            .setNotBefore(timeNow.minusSeconds(60))
            .setExpiresOn(timeNow.minusSeconds(30))
            .setIssuer("Fred");

        String objectString = assertDoesNotThrow(() ->
            ADAPTER.serialize(testObjectExpired30SecondsAgo, SerializerEncoding.JSON));

        AttestationToken newToken = AttestationTokenImpl.createUnsecuredToken(objectString);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            ((AttestationTokenImpl) newToken).validate(null,
                new AttestationTokenValidationOptions()));
        // Both the current time and the expiration time should be in the exception message.
        String exceptionMessage = ex.getMessage();
        assertTrue(exceptionMessage.contains("expiration"), () ->
            "Expected exception message to contain 'expiration' but it didn't. Actual exception message: "
            + exceptionMessage);
        // Because the TestObject round-trips times through Epoch times, they are in UTC time.
        // Adjust the target time to be in UTC rather than the current time zone, since we're checking to ensure
        // that the time is reflected in the exception message.
        OffsetDateTime expTime = timeNow.minusSeconds(30).withOffsetSameInstant(ZoneOffset.UTC);

        // Format of the exception message is "Current time: <current time> Expiration time: <expiration time>"
        // Since the test could take a while and the current time is based on the time when the exception is thrown
        // this can cause it to be different than 'timeNow' when the test started.
        // To make sure this test isn't flaky capture the datetime string in the exception message, turn it into an
        // OffsetDateTime and compare it to 'timeNow' allowing for some skew.
        // Date format is 'Wed Sep 27 12:48:15 -04:00 2023'
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss XXX yyyy");

        int currentTimeIndex = exceptionMessage.indexOf("Current time: ");
        int expirationTimeIndex = exceptionMessage.indexOf("Expiration time: ");
        String currentTimeInExceptionString = exceptionMessage.substring(currentTimeIndex + 14, expirationTimeIndex - 1);
        OffsetDateTime currentTimeInException = OffsetDateTime.parse(currentTimeInExceptionString, formatter);
        long skew = timeNow.until(currentTimeInException, ChronoUnit.SECONDS);
        if (skew > 5 || skew < 0) {
            fail(String.format("Expected exception message to contain 'Current Time' within 5 seconds, but not before, "
                               + "of %tc but it was greater. Actual exception message: %s", timeNow, exceptionMessage));
        }

        assertTrue(exceptionMessage.contains(String.format("%tc", expTime)), () -> String.format(
            "Expected exception message to contain '%tc' but it didn't. Actual exception message: %s", expTime,
            exceptionMessage));
    }

    @Test
    void verifyAttestationTokenNotBeforeTimeout() {
        OffsetDateTime timeNow = OffsetDateTime.now();
        timeNow = timeNow.minusNanos(timeNow.getNano());

        TestObject testObjectExpired30SecondsAgo = new TestObject()
            .setAlg("Test Algorithm")
            .setInteger(31415926)
            .setIntegerArray(new int[]{123, 456, 789})
            .setIssuedOn(timeNow.plusSeconds(30))
            .setNotBefore(timeNow.plusSeconds(30))
            .setExpiresOn(timeNow.plusSeconds(60))
            .setIssuer("Fred");

        String objectString = assertDoesNotThrow(() ->
            ADAPTER.serialize(testObjectExpired30SecondsAgo, SerializerEncoding.JSON));

        AttestationToken newToken = AttestationTokenImpl.createUnsecuredToken(objectString);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            ((AttestationTokenImpl) newToken).validate(null, new AttestationTokenValidationOptions()));
        // Both the current time and the expiration time should be in the exception message.
        assertTrue(ex.getMessage().contains("NotBefore"));

        // Because the TestObject round-trips times through Epoch times, they are in UTC time.
        // Adjust the target time to be in UTC rather than the current time zone, since we're checking to ensure
        // that the time is reflected in the exception message.
        OffsetDateTime iatTime = timeNow.plusSeconds(30).withOffsetSameInstant(ZoneOffset.UTC);

        assertTrue(ex.getMessage().contains(String.format("%tc", timeNow)));
        assertTrue(ex.getMessage().contains(String.format("%tc", iatTime)));
    }

    @Test
    void verifyAttestationTokenVerifyCallback() {
        OffsetDateTime timeNow = OffsetDateTime.now();
        timeNow = timeNow.minusNanos(timeNow.getNano());

        TestObject testObjectExpired30SecondsAgo = new TestObject()
            .setAlg("Test Algorithm")
            .setInteger(31415926)
            .setIntegerArray(new int[]{123, 456, 789})
            .setIssuedOn(timeNow)
            .setNotBefore(timeNow)
            .setExpiresOn(timeNow.plusSeconds(60))
            .setIssuer("Fred");

        String objectString = assertDoesNotThrow(() ->
            ADAPTER.serialize(testObjectExpired30SecondsAgo, SerializerEncoding.JSON));

        AttestationToken newToken = AttestationTokenImpl.createUnsecuredToken(objectString);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            ((AttestationTokenImpl) newToken).validate(null,
                new AttestationTokenValidationOptions()
                    .setValidationCallback((token, signer) -> {
                        throw new RuntimeException("I was interrupted");
                    })));
        // Both the current time and the expiration time should be in the exception message.
        assertTrue(ex.getMessage().contains("I was interrupted"));
    }


    @Test
    void testCreateSecuredEmptyAttestationToken() {
        KeyPair rsaKey = assertDoesNotThrow(() -> createKeyPair("RSA"));
        X509Certificate cert = assertDoesNotThrow(() ->
            createSelfSignedCertificate("Test Certificate Secured 2", rsaKey));
        AttestationSigningKey signingKey = new AttestationSigningKey(cert, rsaKey.getPrivate())
            .setWeakKeyAllowed(true);

        AttestationToken newToken = AttestationTokenImpl.createSecuredToken(signingKey);

        // This had better be a signed token.
        assertNotEquals("none", newToken.getAlgorithm());
        assertNull(newToken.getKeyId());
        assertNotNull(newToken.getCertificateChain());
        assertArrayEquals(assertDoesNotThrow(cert::getEncoded),
            assertDoesNotThrow(() -> newToken.getCertificateChain().getCertificates().get(0).getEncoded()));

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
        assertEquals("https://jalarryoattestationaad.wus.attest.azure.net/certs", token.getJsonWebKeyUrl());
        assertEquals("rPtBGRWTlPmzs5u35L0QRD9WdymeKPRxteTeTlwN0Ec=", token.getKeyId());
        assertEquals("JWT", token.getType());

        // Because this is a pre-canned token, we know exactly when the token was issued and expires.
        assertEquals("2021-07-21T21:08:16Z", token.getNotBefore().toString());
        assertEquals("2021-07-21T21:08:16Z", token.getIssuedAt().toString());
        assertEquals("2021-07-22T05:08:16Z", token.getExpiresOn().toString());

        com.azure.security.attestation.implementation.models.AttestationResult generatedAttestResult;

        generatedAttestResult = token.getBody(AttestationResult.class);
        assertEquals("1.0", generatedAttestResult.getVersion());
        assertEquals("sgx", generatedAttestResult.getVerifierType());

        com.azure.security.attestation.models.AttestationResult result =
            AttestationResultImpl.fromGeneratedAttestationResult(generatedAttestResult);

        assertEquals("sgx", result.getVerifierType());
        assertEquals("1.0", result.getVersion());
        assertEquals("05487b3353172da9013314f1e9eb1cc7dc2b3bccc054291df4c344ef39f9bcd4", result.getUniqueIdentifier());

    }


}

