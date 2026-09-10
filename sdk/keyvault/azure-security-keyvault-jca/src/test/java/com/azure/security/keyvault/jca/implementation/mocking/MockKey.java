// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.mocking;

import java.security.Key;

public class MockKey implements Key {
    @Override
    public String getAlgorithm() {
        return "";
    }

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public byte[] getEncoded() {
        return new byte[0];
    }
}
