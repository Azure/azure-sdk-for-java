// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import junitparams.FileParameters;
import junitparams.JUnitParamsRunner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitParamsRunner.class)
public class UserPrincipalManagerTest {

    /** Token from https://docs.microsoft.com/azure/active-directory/develop/v2-id-and-access-tokens */
    private static final String JWT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik1uQ19WWmNBVGZNNXBPWWlKSE1"
        + "iYTlnb0VLWSJ9.eyJhdWQiOiI2NzMxZGU3Ni0xNGE2LTQ5YWUtOTdiYy02ZWJhNjkxNDM5MWUiLCJpc3MiOiJodHRwczovL2xvZ2lu"
        + "Lm1pY3Jvc29mdG9ubGluZS5jb20vYjk0MTk4MTgtMDlhZi00OWMyLWIwYzMtNjUzYWRjMWYzNzZlL3YyLjAiLCJpYXQiOjE0NTIyOD"
        + "UzMzEsIm5iZiI6MTQ1MjI4NTMzMSwiZXhwIjoxNDUyMjg5MjMxLCJuYW1lIjoiQmFiZSBSdXRoIiwibm9uY2UiOiIxMjM0NSIsIm9p"
        + "ZCI6ImExZGJkZGU4LWU0ZjktNDU3MS1hZDkzLTMwNTllMzc1MGQyMyIsInByZWZlcnJlZF91c2VybmFtZSI6InRoZWdyZWF0YmFtYm"
        + "lub0BueXkub25taWNyb3NvZnQuY29tIiwic3ViIjoiTUY0Zi1nZ1dNRWppMTJLeW5KVU5RWnBoYVVUdkxjUXVnNWpkRjJubDAxUSIs"
        + "InRpZCI6ImI5NDE5ODE4LTA5YWYtNDljMi1iMGMzLTY1M2FkYzFmMzc2ZSIsInZlciI6IjIuMCJ9.p_rYdrtJ1oCmgDBggNHB9O38K"
        + "TnLCMGbMDODdirdmZbmJcTHiZDdtTc-hguu3krhbtOsoYM2HJeZM3Wsbp_YcfSKDY--X_NobMNsxbT7bqZHxDnA2jTMyrmt5v2EKUn"
        + "EeVtSiJXyO3JWUq9R0dO-m4o9_8jGP6zHtR62zLaotTBYHmgeKpZgTFB9WtUq8DVdyMn_HSvQEfz-LWqckbcTwM_9RNKoGRVk38KCh"
        + "VJo4z5LkksYRarDo8QgQ7xEKmYmPvRr_I7gvM2bmlZQds2OeqWLB1NSNbFZqyFOCgYn3bAQ-nEQSKwBaA36jYGPOVG2r2Qv1uKcpSO"
        + "xzxaQybzYpQ";

    private static ImmutableJWKSet<SecurityContext> immutableJWKSet;

    @BeforeClass
    public static void setupClass() throws Exception {
        final X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(Files.newInputStream(Paths.get("src/test/resources/test-public-key.txt")));
        immutableJWKSet = new ImmutableJWKSet<>(new JWKSet(JWK.parse(cert)));
    }

    private UserPrincipalManager userPrincipalManager;


    @Test
    public void testAlgIsTakenFromJWT() throws Exception {
        userPrincipalManager = new UserPrincipalManager(immutableJWKSet);
        final UserPrincipal userPrincipal = userPrincipalManager.buildUserPrincipal(
            Files.readString(Paths.get("src/test/resources/jwt-signed.txt"))
        );
        assertThat(userPrincipal)
            .isNotNull()
            .extracting(UserPrincipal::getIssuer, UserPrincipal::getSubject)
            .containsExactly("https://sts.windows.net/test", "test@example.com");
    }

    @Test
    public void invalidIssuer() {
        userPrincipalManager = new UserPrincipalManager(immutableJWKSet);
        assertThatCode(() ->
            userPrincipalManager.buildUserPrincipal(
                Files.readString(Paths.get("src/test/resources/jwt-bad-issuer.txt"))
            )
        ).isInstanceOf(BadJWTException.class);
    }

    @Test
    //TODO: add more generated tokens with other valid issuers to this file. Didn't manage to generate them
    @FileParameters("src/test/resources/jwt-valid-issuer.txt")
    public void validIssuer(final String token) {
        userPrincipalManager = new UserPrincipalManager(immutableJWKSet);
        assertThatCode(() -> userPrincipalManager.buildUserPrincipal(token))
                .doesNotThrowAnyException();
    }

    @Test
    public void nullIssuer() {
        userPrincipalManager = new UserPrincipalManager(immutableJWKSet);
        assertThatCode(() ->
            userPrincipalManager.buildUserPrincipal(
                Files.readString(Paths.get("src/test/resources/jwt-null-issuer.txt"))
            )
        ).isInstanceOf(BadJWTException.class);
    }



    @Test
    public void userPrincipalIsSerializable() throws ParseException, IOException, ClassNotFoundException {
        final File tmpOutputFile = File.createTempFile("test-user-principal", "txt");

        try (FileOutputStream fileOutputStream = new FileOutputStream(tmpOutputFile);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
             FileInputStream fileInputStream = new FileInputStream(tmpOutputFile);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            final JWSObject jwsObject = JWSObject.parse(JWT_TOKEN);
            final JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject("fake-subject").build();
            final UserPrincipal principal = new UserPrincipal("", jwsObject, jwtClaimsSet);

            objectOutputStream.writeObject(principal);

            final UserPrincipal serializedPrincipal = (UserPrincipal) objectInputStream.readObject();

            Assert.assertNotNull("Serialized UserPrincipal not null", serializedPrincipal);
            Assert.assertFalse(
                "Serialized UserPrincipal kid not empty",
                StringUtils.isEmpty(serializedPrincipal.getKid())
            );
            Assert.assertNotNull("Serialized UserPrincipal claims not null.", serializedPrincipal.getClaims());
            assertTrue(
                "Serialized UserPrincipal claims not empty.",
                serializedPrincipal.getClaims().size() > 0
            );
        } finally {
            Files.deleteIfExists(tmpOutputFile.toPath());
        }
    }
}
