// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import java.io.Closeable;
import java.io.IOException;
import java.security.SecureRandom;

class RNGCryptoServiceProvider implements Closeable {
    // TODO: is this thread safe? efficient, etc?
    private SecureRandom random = new SecureRandom();

    public void getBytes(byte[] randomBytes) {
        random.nextBytes(randomBytes);
    }

    @Override
    public void close() {

    }
}
