// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import reactor.core.publisher.Mono;

interface Decryptor {
    Mono<byte[]> decrypt(byte[] cipherText);
}
