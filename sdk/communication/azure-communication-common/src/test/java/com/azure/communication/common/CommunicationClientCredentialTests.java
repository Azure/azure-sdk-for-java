// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommunicationClientCredentialTests {

    @Test
    public void constructionWithNullTest() {
        assertThrows(NullPointerException.class, () -> {
            new CommunicationClientCredential(null);
        });
    }

    @Test
    public void constructionWithNonbase64Test() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CommunicationClientCredential("I have got the key");
        });
    }
}
