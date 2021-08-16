// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class UserPrincipalAzureADGraphTest {

    private WireMockRule wireMockRule;

    @BeforeEach
    void setup() {
        wireMockRule = new WireMockRule(9519);
        wireMockRule.start();
    }

    @AfterEach
    void close() {
        if (wireMockRule.isRunning()) {
            wireMockRule.stop();
        }
    }

    @Test
    public void userPrincipalIsSerializable() throws ParseException, IOException, ClassNotFoundException {
        final File tmpOutputFile = File.createTempFile("test-user-principal", "txt");

        try (FileOutputStream fileOutputStream = new FileOutputStream(tmpOutputFile);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
             FileInputStream fileInputStream = new FileInputStream(tmpOutputFile);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            final JWSObject jwsObject = JWSObject.parse(TestConstants.JWT_TOKEN);
            final JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject("fake-subject").build();
            final UserPrincipal principal = new UserPrincipal("", jwsObject, jwtClaimsSet);

            objectOutputStream.writeObject(principal);

            final UserPrincipal serializedPrincipal = (UserPrincipal) objectInputStream.readObject();

            assertNotNull(serializedPrincipal, "Serialized UserPrincipal not null");
            assertTrue(StringUtils.hasText(serializedPrincipal.getKid()), "Serialized UserPrincipal kid not empty");
            assertNotNull(serializedPrincipal.getClaims(), "Serialized UserPrincipal claims not null.");
            assertTrue(serializedPrincipal.getClaims().size() > 0, "Serialized UserPrincipal claims not empty.");
        } finally {
            Files.deleteIfExists(tmpOutputFile.toPath());
        }
    }
}
