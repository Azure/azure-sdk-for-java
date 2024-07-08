// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.v2.credential.AzureKeyCredential;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AzureKeyCredentialPolicyTests {
    @ParameterizedTest
    @MethodSource("setCredentialSupplier")
    public void setCredential(AzureKeyCredentialPolicy policy, String expectedHeader) {
        HttpHeaders headers = new HttpHeaders();
        policy.setCredential(headers);
        assertEquals(expectedHeader, headers.getValue(HttpHeaderName.AUTHORIZATION));
    }

    private static Stream<Arguments> setCredentialSupplier() {
        AzureKeyCredential credential = new AzureKeyCredential("asecret");
        return Stream.of(
            Arguments.of(new AzureKeyCredentialPolicy(HttpHeaderName.AUTHORIZATION.toString(), credential, null),
                "asecret"),
            Arguments.of(new AzureKeyCredentialPolicy(HttpHeaderName.AUTHORIZATION.toString(), credential,
                "SharedKeyCredential"), "SharedKeyCredential asecret"),
            Arguments.of(new AzureKeyCredentialPolicy(HttpHeaderName.AUTHORIZATION.toString(), credential,
                "SharedKeyCredential "), "SharedKeyCredential asecret"));
    }
}
