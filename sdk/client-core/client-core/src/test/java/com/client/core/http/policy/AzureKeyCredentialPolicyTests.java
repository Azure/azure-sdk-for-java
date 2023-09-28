// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.http.policy;

import com.client.core.credential.ClientKeyCredential;
import com.client.core.http.HttpHeaderName;
import com.client.core.http.HttpHeaders;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientKeyCredentialPolicyTests {
    @ParameterizedTest
    @MethodSource("setCredentialSupplier")
    public void setCredential(ClientKeyCredentialPolicy policy, String expectedHeader) {
        HttpHeaders headers = new HttpHeaders();
        policy.setCredential(headers);
        assertEquals(expectedHeader, headers.getValue(HttpHeaderName.AUTHORIZATION));
    }

    private static Stream<Arguments> setCredentialSupplier() {
        ClientKeyCredential credential = new ClientKeyCredential("asecret");
        return Stream.of(
            Arguments.of(new ClientKeyCredentialPolicy(HttpHeaderName.AUTHORIZATION.toString(), credential, null), "asecret"),
            Arguments.of(new ClientKeyCredentialPolicy(HttpHeaderName.AUTHORIZATION.toString(), credential, "SharedKeyCredential"),
                "SharedKeyCredential asecret"),
            Arguments.of(new ClientKeyCredentialPolicy(HttpHeaderName.AUTHORIZATION.toString(), credential, "SharedKeyCredential "),
                "SharedKeyCredential asecret")
        );
    }
}
