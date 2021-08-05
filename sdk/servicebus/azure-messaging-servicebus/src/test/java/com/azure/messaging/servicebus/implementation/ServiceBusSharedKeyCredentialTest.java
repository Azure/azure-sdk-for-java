// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.credential.TokenRequestContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ServiceBusSharedKeyCredential}.
 */
public class ServiceBusSharedKeyCredentialTest {

    @ParameterizedTest
    @MethodSource("getSas")
    public void testSharedAccessSignatureCredential(String sas, OffsetDateTime expectedExpirationTime) {
        ServiceBusSharedKeyCredential serviceBusSharedKeyCredential = new ServiceBusSharedKeyCredential(sas);
        StepVerifier.create(serviceBusSharedKeyCredential.getToken(new TokenRequestContext().addScopes("sb://test"
            + "-entity.servicebus.windows.net/.default")))
            .assertNext(token -> {
                assertNotNull(token.getToken());
                assertEquals(sas, token.getToken());
                assertEquals(expectedExpirationTime, token.getExpiresAt());
            })
            .verifyComplete();
    }

    private static Stream<Arguments> getSas() {
        String validSas = "SharedAccessSignature "
            + "sr=https%3A%2F%2Fentity-name.servicebus.windows.net%2F"
            + "&sig=encodedsignature%3D"
            + "&se=1599537084"
            + "&skn=test-sas-key";
        String validSasWithNoExpirationTime = "SharedAccessSignature "
            + "sr=https%3A%2F%2Fentity-name.servicebus.windows.net%2F"
            + "&sig=encodedsignature%3D"
            + "&skn=test-sas-key";
        String validSasInvalidExpirationTimeFormat = "SharedAccessSignature "
            + "sr=https%3A%2F%2Fentity-name.servicebus.windows.net%2F"
            + "&sig=encodedsignature%3D"
            + "&se=se=2020-12-31T13:37:45Z"
            + "&skn=test-sas-key";

        return Stream.of(
            Arguments.of(validSas, OffsetDateTime.parse("2020-09-08T03:51:24Z")),
            Arguments.of(validSasWithNoExpirationTime, OffsetDateTime.MAX),
            Arguments.of(validSasInvalidExpirationTimeFormat, OffsetDateTime.MAX)
        );
    }
}
