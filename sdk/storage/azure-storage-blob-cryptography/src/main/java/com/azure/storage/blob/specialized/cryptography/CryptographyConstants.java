// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.util.CoreUtils;
import com.azure.storage.common.implementation.Constants;

import java.util.Map;

final class CryptographyConstants {
    static final String ENCRYPTION_PROTOCOL_V1 = "1.0";

    static final String ENCRYPTION_PROTOCOL_V2 = "2.0";

    static final String AGENT_METADATA_KEY = "EncryptionLibrary";

    static final String AES_CBC_PKCS5PADDING = "AES/CBC/PKCS5Padding";

    static final String AES_CBC_NO_PADDING = "AES/CBC/NoPadding";

    static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";

    static final String AES = "AES";

    static final Map<String, String> USER_AGENT_PROPERTIES = CoreUtils.getProperties("azure-storage-blob.properties");

    static final String AGENT_METADATA_VALUE = "JavaTrack2" + USER_AGENT_PROPERTIES.get("version");

    static final String ENCRYPTION_DATA_KEY = "encryptiondata";

    static final String ENCRYPTION_METADATA_HEADER = Constants.HeaderConstants.X_MS_META + "-"
        + ENCRYPTION_DATA_KEY;

    static final String ENCRYPTION_MODE = "FullBlob";

    static final int ENCRYPTION_BLOCK_SIZE = 16;

    static final String RANGE_HEADER = "x-ms-range";

    static final String CONTENT_RANGE = "Content-Range";

    static final String CONTENT_LENGTH = "Content-Length";

    static final int NONCE_LENGTH = 12;

    static final int TAG_LENGTH = 16;

    static final int GCM_ENCRYPTION_REGION_LENGTH = 4 * Constants.MB;

    static final String DECRYPT_UNENCRYPTED_BLOB = "Encryption client is being used but the blob metadata indicates "
        + "that it is not encrypted.";


    private CryptographyConstants() {
    }

}
