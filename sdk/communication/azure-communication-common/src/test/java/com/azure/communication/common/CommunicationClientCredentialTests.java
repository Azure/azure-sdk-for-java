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

}
