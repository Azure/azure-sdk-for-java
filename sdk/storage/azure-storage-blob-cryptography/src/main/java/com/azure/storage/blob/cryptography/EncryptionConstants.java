// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.cryptography;

import com.azure.storage.blob.BlobConfiguration;

public final class EncryptionConstants {
    static final String ENCRYPTION_PROTOCOL_V1 = "1.0";

    static final String AGENT_METADATA_KEY = "EncryptionLibrary";

    static final String AES_CBC_PKCS5PADDING = "AES/CBC/PKCS5Padding";

    static final String AES_CBC_NO_PADDING = "AES/CBC/NoPadding";

    static final String AES = "AES";

    static final String AGENT_METADATA_VALUE = "JavaTrack2" + BlobConfiguration.VERSION;

    public static final String ENCRYPTION_DATA_KEY = "encryptiondata";

    static final String ENCRYPTION_MODE = "FullBlob";

    static final int ENCRYPTION_BLOCK_SIZE = 16;

    private EncryptionConstants() {
    }
}
