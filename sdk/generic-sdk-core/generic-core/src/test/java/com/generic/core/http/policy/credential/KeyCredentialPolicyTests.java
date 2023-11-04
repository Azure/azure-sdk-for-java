// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy.credential;

import com.generic.core.credential.KeyCredential;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.models.Headers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyCredentialPolicyTests {
    @ParameterizedTest
    @MethodSource("setCredentialSupplier")
    public void setCredential(KeyCredentialPolicy policy, String expectedHeader) {
        Headers headers = new Headers();
        policy.setCredential(headers);
        assertEquals(expectedHeader, headers.getValue(HttpHeaderName.AUTHORIZATION));
    }

    private static Stream<Arguments> setCredentialSupplier() {
        String fakeKey = "fakeKeyPlaceholder";
        KeyCredential credential = new KeyCredential(fakeKey);
        return Stream.of(
            Arguments.of(new KeyCredentialPolicy(HttpHeaderName.AUTHORIZATION.toString(), credential, null), fakeKey),
            Arguments.of(new KeyCredentialPolicy(HttpHeaderName.AUTHORIZATION.toString(), credential, "Bearer"),
                "Bearer " + fakeKey),
            Arguments.of(new KeyCredentialPolicy(HttpHeaderName.AUTHORIZATION.toString(), credential, "Bearer "),
                "Bearer " + fakeKey)
        );
    }
}
